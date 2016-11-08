package game;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.MapConfiguration;

import com.sun.tools.javac.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.util.Utils;

public class Submarine extends PlayerObject {

	private static final int ELASTICITY_TRIES_MAX = 4;
	private static final int ELASTICITY_STEP = 1;
	private static double SHOOT_PREDICTION_EPSILON;
	private static int MAX_SHOOT_STEPS_PREDICT;
	private static Integer SONAR_COOLDOWN;
	private static double SONAR_RANGE;
	private static Integer SONAR_DURATION;
	public static double MAX_ACCELERATION;
	public static double MAX_STEERING;
	public static double MAX_SPEED;
	public static int TORPEDO_COOLDOWN;
	public static double SUBMARINE_RADIUS;

	private final GameMap map;

	private static Logger log = LoggerFactory.getLogger(Submarine.class);

	public Deque<Action> actionQueue = new LinkedBlockingDeque<>();

	private int torpedosShotInRound = -1;
	private int currentRoundNum = 0;

	public List<XVector> futurePositions = new ArrayList<>();
	public List<XVector> futureTorpedoPositions = new ArrayList<>();

	public static void setBounds(MapConfiguration rules) {
		SONAR_COOLDOWN = rules.extendedSonarCooldown;
		SONAR_RANGE = rules.extendedSonarRange;
		SONAR_DURATION = rules.extendedSonarRounds;

		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;

		TORPEDO_COOLDOWN = rules.torpedoCooldown;
		SUBMARINE_RADIUS = rules.submarineSize;

		MAX_SHOOT_STEPS_PREDICT = rules.torpedoRange;
		SHOOT_PREDICTION_EPSILON = 0.3;
	}

	public int hp = 0;
	public int sonarCooldown = 0;
	public int sonarDuration = 0;
	public int torpedoCooldown = 0;

	public Submarine(long id, String owner, double x, double y, double speed, double rotation, GameMap map) {
		super(id, owner, PlayerObjectType.SUBMARINE, x, y, SUBMARINE_RADIUS, speed, rotation);
		this.map = map;
		this.hp = (int)(map.mapConfig.torpedoDamage * 3);
	}

	public Long getId(){
		return this.id;
	}

	public void nextRound() {
		log.info("Starting next round ({})", this.currentRoundNum + 1);
		this.currentRoundNum++;
		this.torpedoCooldown = Math.max(0, this.torpedoCooldown - 1);
		this.sonarCooldown = Math.max(0, this.sonarCooldown - 1);
		this.sonarDuration = Math.max(0, this.sonarDuration - 1);
		log.info("Cooldowns for {}: [{}, {}]", this.id, this.torpedoCooldown, this.sonarCooldown);
	}

	public void validatePosition(double x, double y, double angle, double speed) {
		double predictionError = 0.0;
		predictionError += Math.abs(this.rotation - angle) + Math.abs(this.speed - speed);
		predictionError += Math.abs(this.position.getX() - x) + Math.abs(this.position.getY() - y);
		log.info("Prediction error for {} is {}", this.id, predictionError);

		this.updatePosition(x, y, angle, speed);
	}

	public void actionExecuted(Action a) {
		if (this.actionQueue.peek() == a) {
			this.actionQueue.remove();
			log.info("Ship {}: successfully executed {}", this.id, a.toString());
		} else {
			log.info("Unwanted action executed, top {} <-> executed {}", this.actionQueue.peek(), a);
		}

		if (a instanceof Action.MoveAction) {
			Action.MoveAction ma = (Action.MoveAction) a;
			this.steer(ma.steering);
			this.accelerate(ma.acceleration);
			this.step();
			log.info("Ship {} moved to {}", this.id, this.position);
		} else if (a instanceof Action.ShootAction) {
			Action.ShootAction sa = (Action.ShootAction) a;
			this.torpedoCooldown = TORPEDO_COOLDOWN;
			log.info("Ship {} shot torpedo. Cooldown is now {}", this.id, this.torpedoCooldown);
		} else if (a instanceof Action.SonarAction) {
			this.sonarCooldown = SONAR_COOLDOWN;
			this.sonarDuration = SONAR_DURATION;
			log.info("Ship {} activated sonar. Cooldown is now {}", this.id, this.sonarCooldown);
		}
	}

	public boolean shootAtTarget(ProjectileLike originalTarget) {
		if (!this.canShootTorpedo()) return false;

		ProjectileLike target = originalTarget.clone();

		for (int i = 1; i <= MAX_SHOOT_STEPS_PREDICT; i++) {
			// TODO: ugy nez ki meg talan 1 korrel kesobb kezd loni mint ahogy lohetne - REVISE this part
			target.step();
			double distance = this.position.distance(target.position);
			double distanceInSteps = distance / Torpedo.TORPEDO_SPEED;
			double error = distanceInSteps - i;

			if (Math.abs(error) < SHOOT_PREDICTION_EPSILON) {
				XVector enemyPositionAfterISteps = target.position;
				XVector direction = enemyPositionAfterISteps.subtract(this.position);
				double angle = direction.getAngleInDegrees();
				Torpedo torpedo = new Torpedo(-1L, this.owner, this.x(), this.y(), angle);
				//torpedo.nextRound(i);
				this.futureTorpedoPositions.clear();
				for (int j = 0; j < i; j++) {
					torpedo.step();
					this.futureTorpedoPositions.add(torpedo.position);
				}
				XVector torpedoPositionAfterISteps = torpedo.position;
				double distanceFromShipCenter = torpedoPositionAfterISteps.distance(target.position);
				log.info("Shooting torpedo, ETA: {}, distance from ship center when landing: {}", i, distanceFromShipCenter);
				this.actionQueue.add(Action.shoot(angle));
				this.torpedosShotInRound = this.currentRoundNum;
				return true;
			}
		}
		return false;
	}

	public void gotoXY(XVector target) {
		Pair<ProjectileLike, List<Action.MoveAction>> route = null;
		int minimumRouteLength = Integer.MAX_VALUE;
		for (int i = 1; i < ELASTICITY_TRIES_MAX; i++) {
			Pair<ProjectileLike, List<Action.MoveAction>> testRoute =
					calculateSteps(this, this.position, target, this.map, i * ELASTICITY_STEP);

			if (testRoute.snd.size() < minimumRouteLength){
				route = testRoute;
				minimumRouteLength = testRoute.snd.size();
			}
		}
		assert route != null;

		ProjectileLike simulator = this.clone();
		this.futurePositions = route.snd.stream().map(a -> {
			simulator.steer(a.steering);
			simulator.accelerate(a.acceleration);
			simulator.step();
			return simulator.position;
		}).collect(Collectors.toList());

		this.actionQueue.addAll(route.snd);
	}

	private static Pair<ProjectileLike, List<Action.MoveAction>> calculateSteps(ProjectileLike projectile, XVector startingPoint, XVector target, GameMap map, double elasticity) {
		elasticity = Math.max(elasticity, 1);

		ProjectileLike ghostShip = projectile.clone();
		ghostShip.position = startingPoint;
		List<Circular> islands = map.islands;

//		double distanceToTarget = startingPoint.distance(target);

		List<Action.MoveAction> moveActions = new ArrayList<>();

		while (ghostShip.position.distance(target) > MAX_SPEED) {
			if (moveActions.size() > 100) {
				return new Pair<>(ghostShip, moveActions);
			}

			XVector direction = target.subtract(ghostShip.position);
			double targetSpeed = MAX_SPEED;
			double targetAngle = direction.getAngleInDegrees();
			double angleDiff = (targetAngle - ghostShip.rotation);

			if (angleDiff > 180) {
				angleDiff = -(360 - angleDiff);
			}
			else if (angleDiff < -180) angleDiff = 360 + angleDiff;
			if (angleDiff > MAX_STEERING || angleDiff < -MAX_STEERING){
				angleDiff = Utils.clamp(-MAX_STEERING, angleDiff, MAX_STEERING);
				targetSpeed = MAX_SPEED * (1 - Math.abs(angleDiff) / (MAX_STEERING * elasticity));
			}

			double accelerationDiff = 0;
			if (direction.getMagnitude() < ghostShip.speed + fullStopDistance(ghostShip.speed, MAX_ACCELERATION)) {
				accelerationDiff = -MAX_ACCELERATION;
			} else if (!eql(ghostShip.speed, targetSpeed)) {
				accelerationDiff = Utils.clamp(-MAX_ACCELERATION, targetSpeed - ghostShip.speed, MAX_ACCELERATION);
			}
			ghostShip.accelerate(accelerationDiff);
			ghostShip.steer(angleDiff);
			ghostShip.step();

			Optional<Circular> collidingIsland = islands.stream().filter(i -> i.checkIntersection(ghostShip)).findFirst();
			if (collidingIsland.isPresent()) {
				Circular island = collidingIsland.get();
				XVector collisionPoint = ghostShip.position;
				XVector detourStep = collisionPoint.add(collisionPoint.subtract(island.position).unit().scale(ghostShip.r * 5));
				Pair<ProjectileLike, List<Action.MoveAction>> firstSegment = calculateSteps(projectile, startingPoint, detourStep, map, elasticity);
				ProjectileLike lastStep = firstSegment.fst;
				Pair<ProjectileLike, List<Action.MoveAction>> lastSegment = calculateSteps(lastStep, detourStep, target, map, elasticity);

				List<Action.MoveAction> steps = Stream.concat(firstSegment.snd.stream(), lastSegment.snd.stream()).collect(Collectors.toList());

				return new Pair<>(lastSegment.fst, steps);
			}

			moveActions.add(Action.move(angleDiff, accelerationDiff));

//			if (ghostShip.position.distance(target) > distanceToTarget){
//				return new Pair<>(ghostShip, moveActions);
//			} else {
//				distanceToTarget = ghostShip.position.distance(target);
//			}
		}

		return new Pair<>(ghostShip, moveActions);
	}

	public boolean tryActivateSonar() {
		if (this.isSonarReady()) {
			// Sonar activates instantly, put it in the front of the queue
			this.actionQueue.push(Action.activateSonar());
			return true;
		} else {
			return false;
		}
	}

	private static double fullStopDistance(double speed, double maxDecrement) {
		double d = 0;
		while (speed > 0) {
			d += speed;
			speed -= Math.abs(maxDecrement);
		}
		return d;
	}
	private static boolean eql(double a, double b){
		return Math.abs(a-b) < 0.0000000001;
	}

	public boolean isSonarReady() {
		return this.sonarCooldown == 0;
	}

	public boolean canShootTorpedo() {
		return this.torpedoCooldown == 0;
	}
}
