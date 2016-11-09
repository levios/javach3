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

@SuppressWarnings("restriction")
public class Submarine extends PlayerObject {

	private static final int ELASTICITY_TRIES_MAX = 3;
	private static final int ELASTICITY_STEP = 1;
	private static double SHOOT_PREDICTION_EPSILON;
	private static int MAX_SHOOT_STEPS_PREDICT;

	public static Integer SONAR_COOLDOWN;
	public static double SONAR_RANGE;
	public static Integer SONAR_DURATION;
	public static double MAX_ACCELERATION;
	public static double MAX_STEERING;
	public static double MAX_SPEED;
	public static int TORPEDO_COOLDOWN;
	public static int TORPEDO_EXPLOSION_RADIUS;
	public static double SUBMARINE_RADIUS;

	private final GameMap map;

	private static Logger log = LoggerFactory.getLogger(Submarine.class);

	public Deque<Action> actionQueue = new LinkedBlockingDeque<>();

	public List<XVector> nextPositions = new ArrayList<>();

	public List<XVector> chasePositions = new ArrayList<>();

	public int currentRoundNum = 0;

	public List<XVector> futurePositions = new ArrayList<>();
	public List<XVector> futureTorpedoPositions = new ArrayList<>();

	public static void setBounds(MapConfiguration rules) {
		SONAR_COOLDOWN = rules.extendedSonarCooldown;
		SONAR_RANGE = rules.extendedSonarRange;
		SONAR_DURATION = rules.extendedSonarRounds;

		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;
		TORPEDO_EXPLOSION_RADIUS = rules.torpedoExplosionRadius;
		TORPEDO_COOLDOWN = rules.torpedoCooldown;
		SUBMARINE_RADIUS = rules.submarineSize;

		MAX_SHOOT_STEPS_PREDICT = rules.torpedoRange;
		SHOOT_PREDICTION_EPSILON = 0.3;
	}

	public int hp = 0;
	public int sonarCooldown = 0;
	public int sonarDuration = 0;
	public int torpedoCooldown = 0;
	public boolean usingExtendedSonar = false;
	public XVector tempTargetPosition = null;

	public Submarine(long id, String owner, double x, double y, double speed, double rotation, GameMap map) {
		super(id, owner, PlayerObjectType.SUBMARINE, x, y, SUBMARINE_RADIUS, speed, rotation);
		this.map = map;
		this.hp = (map.mapConfig.torpedoDamage * 3);
	}

	public Long getId(){
		return this.id;
	}

	public void nextRound() {
		log.info("Starting next round ({})", this.currentRoundNum + 1);
		this.torpedoCooldown = Math.max(0, this.torpedoCooldown - 1);
		this.sonarCooldown = Math.max(0, this.sonarCooldown - 1);
		this.sonarDuration = Math.max(0, this.sonarDuration - 1);
		if (this.sonarDuration == 0) this.usingExtendedSonar = false;
		log.info("Cooldowns for {}: [{}, {}]", this.id, this.torpedoCooldown, this.sonarCooldown);
	}

	public void validatePosition(double x, double y, double angle, double speed) {
		double predictionError = 0.0;
		predictionError += Math.abs(this.rotation - angle) + Math.abs(this.speed - speed);
		predictionError += Math.abs(this.position.getX() - x) + Math.abs(this.position.getY() - y);
		log.info("Prediction error for {} is {}", this.id, predictionError);

		this.updatePosition(x, y, angle, speed);
	}

	public int getNumberOfRoundsToReachTarget(double distanceFromTarget){
		return (int) Math.ceil(distanceFromTarget / this.map.mapConfig.torpedoSpeed);
	}

	public boolean isTargetWithinTorpedoRange(int numberOfRoundsToReachTarget){
		return numberOfRoundsToReachTarget <= Torpedo.TORPEDO_RANGE;
	}


	public double whatAngleToShootMovingTarget(ProjectileLike enemy){
		XVector enemyShipVector = new XVector(enemy.x(), enemy.y());

		// current vector
		XVector currentVector = new XVector(this.position.getX(), this.position.getY());

		// target Vector
		XVector targetVector = XVector.subtract(enemyShipVector, currentVector);

		double distanceFromTarget = currentVector.distance(targetVector);
		int numberOfRoundsToReachTarget = getNumberOfRoundsToReachTarget(distanceFromTarget);

		// find out where enemy ship will be in numberOfRoundsToReachTarget rounds
		targetVector = calculateVectorToShoot(enemy, targetVector, numberOfRoundsToReachTarget);

		double targetVectorAngle2 = targetVector.getAngleInDegrees();
		if (targetVectorAngle2 < 0) {
			targetVectorAngle2 += 360;
		}
		log.info("Ship[{}]: TargetVectorAngle calculated: {}", this.id, targetVectorAngle2);

		return targetVectorAngle2;
	}


	/**
	 * translates targetvector
	 * @targetVector - vector from current position to enemy
	 */
	private XVector calculateVectorToShoot(ProjectileLike enemy, XVector targetVector, int numberOfRoundsToReachTarget) {
		XVector v = XVector.unit(enemy.rotation).scale(enemy.speed);

		// // ha tul kozel van, mozogjon ellenkezo iranyba
		// if(currentVector.distance(targetVector2) <
		// map.mapConfig.torpedoExplosionRadius){
		// this.actionQueue.add(Action.move(rotationDiff,
		// map.mapConfig.maxAccelerationPerRound));
		// // conn.move(session.gameId, this.id,
		// session.mapConfiguration.maxAccelerationPerRound,
		// rotationDiff);
		// }

		// hol lesz x kor mulva
		for (int i = 0; i < numberOfRoundsToReachTarget; i++) {
			targetVector = targetVector.add(new XVector(v.x, v.y));
		}
		return targetVector;
	}

	public boolean wouldTorpedoReachTarget(ProjectileLike enemy){
		XVector enemyShipVector = new XVector(enemy.x(), enemy.y());

		// current vector
		XVector currentVector = new XVector(this.position.getX(), this.position.getY());

		// target Vector
		XVector targetVector2 = XVector.subtract(enemyShipVector, currentVector);

		double distanceFromTarget = targetVector2.getMagnitude();
		int numberOfRoundsToReachTarget = getNumberOfRoundsToReachTarget(distanceFromTarget);
		return isTargetWithinTorpedoRange(numberOfRoundsToReachTarget);
	}

	public boolean wouldIKillMyself(ProjectileLike enemy){
		XVector enemyShipVector = new XVector(enemy.x(), enemy.y());

		// current vector
		XVector currentVector = new XVector(this.position.getX(), this.position.getY());

		// target Vector
		XVector targetVector2 = XVector.subtract(enemyShipVector, currentVector);

		double distanceFromTarget = currentVector.distance(targetVector2);
		int numberOfRoundsToReachTarget = getNumberOfRoundsToReachTarget(distanceFromTarget);
		return isTargetWithinTorpedoRange(numberOfRoundsToReachTarget);
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
			this.usingExtendedSonar = true;
			log.info("Ship {} activated sonar. Cooldown is now {}", this.id, this.sonarCooldown);
		}
	}

	public boolean shootAtTarget(ProjectileLike originalTarget) {
		if (!this.canShootTorpedo()) {
			log.info("Ship[{}] I cant shoot", this.id);
			return false;
		}
		
		log.info("Ship[{}] Entering shootAtTarget #1", this.id);

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
				
				//if "i" is little: we are likely to be damaged
				if (i < 3) {
					//predict my move after i steps
					ProjectileLike ghostShip = (ProjectileLike) this.clone();
					ghostShip.step(i);
					if(ghostShip.position.distance(torpedoPositionAfterISteps) < Torpedo.TORPEDO_EXPLOSION
							&& this.hp < Torpedo.TORPEDO_DAMAGE){
						log.info("I would be too close to Explosion AND I have low life: I dont shoot");
						return false;
					}
				}
				
				log.info("Ship[{}] Entering shootAtTarget #2", this.id);
			
				double distanceFromShipCenter = torpedoPositionAfterISteps.distance(target.position);
				log.info("Shooting torpedo, ETA: {}, distance from ship center when landing: {}", i, distanceFromShipCenter);
				this.actionQueue.addFirst(Action.shoot(angle));
				return true;
			}
			
			log.info("Ship[{}] Entering shootAtTarget #3", this.id);
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
	
//	public void gotoXY(XVector nextTargetPosition) {
//		log.info("Ship[{}]: NextTargetPosition calculated: {}", this.id, nextTargetPosition.getAngleInDegrees());
//
//		// calculate iranyvektor
//		XVector targetVector = XVector.subtract(nextTargetPosition, this.position);
//
//		// calculate angle of target vector
//		double targetVectorAngle = targetVector.getAngleInDegrees();
//		log.info("Ship[{}]: TargetVectorAngle calculated: {}", this.id, targetVectorAngle);
//
//		// current angle is in "angle"
//		log.info("Ship[{}]: Current angle: {}", this.id, this.rotation);
//
//		// calc wood-be-perfect angle
//		double angleBetweenTwoVectors = targetVectorAngle - this.rotation;
//		if (angleBetweenTwoVectors < -180.0) {
//			angleBetweenTwoVectors += 360;
//		} else if (angleBetweenTwoVectors > 180.0) {
//			angleBetweenTwoVectors -= 360;
//		}
//		log.info("AngleBetweenTwoVectors calculated: {}", angleBetweenTwoVectors);
//
//		// calc turn
//		double turn = Utils.clamp(-MAX_STEERING, angleBetweenTwoVectors, MAX_STEERING);
//
//		log.info("Ship[{}]: Turn calculated: {}", this.id, turn);
//		// calculate speed - TODO
//
//		// validate that I wont crash into island or "palya szele" - TODO
//
//		// go
//		double acceleration;
//		if (this.speed >= MAX_SPEED)
//			acceleration = 0;
//		else
//			acceleration = Utils.clamp(0, MAX_SPEED - this.speed, MAX_ACCELERATION);
//
//		log.info("Ship[{}]: Acceleration calculated: {}", this.id, acceleration);
//
//		// conn.move(session.gameId, this.id, acceleration, turn);
//		this.actionQueue.add(Action.move(turn, acceleration));
//	}

	private static Pair<ProjectileLike, List<Action.MoveAction>> calculateSteps(ProjectileLike projectile, XVector startingPoint, XVector target, GameMap map, double elasticity) {
		elasticity = Math.max(elasticity, 1);

		ProjectileLike ghostShip = projectile.clone();
		ghostShip.position = startingPoint;
		List<Circular> islands = map.islands;

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
			// CURRENTLY: this shit can only accept -maxacceleration or +maxacceleration, otherwise 400., so round it to the closest 5
			accelerationDiff = Math.round(accelerationDiff / MAX_ACCELERATION)*MAX_ACCELERATION;

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
		}

		return new Pair<>(ghostShip, moveActions);
	}

	public boolean tryActivateSonar() {
		if (this.canUseExtendedSonar()) {
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

	public void chaseOrFleeTarget(ProjectileLike enemyShip) {
		ProjectileLike cloneShip = enemyShip.clone();

		// project next position of enemy ship
		cloneShip.step();
		
		// ha kozel van es  hamarosan lohetek
		XVector nextTargetPosition = null;
		if(this.position.distance(cloneShip.position) < TORPEDO_EXPLOSION_RADIUS && this.torpedoCooldown < 3){ // TODO: 50 ?
			log.info("elkerulo taktika");
			XVector oppositeDirection = cloneShip.position.subtract(this.position).negate();
			nextTargetPosition = this.position.add(oppositeDirection);
		} else {
			nextTargetPosition =  cloneShip.position;
		}

		this.tempTargetPosition  = nextTargetPosition; // this.nextPositions.add(0, nextTargetPosition);
	}

	/**
	 * This should return the closest ship not by distance BUT but number of steps
	 */
	public ProjectileLike getClosestEnemyShip(List<ProjectileLike> enemyShips) {
		if(enemyShips.isEmpty()) return null;
		
		XVector current = this.position;

		ProjectileLike closestEnemy = enemyShips.get(0);

		for(ProjectileLike enemy : enemyShips){
			if (numberOfStepsToReach(current, enemy.position) < numberOfStepsToReach(current, closestEnemy.position)) {
				closestEnemy = enemy;
			}
		}

		//ilyenkor nem adok vissza semmit
		if(current.distance(closestEnemy.position) > 250){
			return null;
		}

		return closestEnemy;
	}
	
	public int numberOfStepsToReach(XVector current, XVector target) {
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

		return route.snd.size();
	}

	public boolean canUseExtendedSonar() {
		return this.sonarCooldown == 0;
	}

	public boolean canShootTorpedo() {
		return this.torpedoCooldown == 0;
	}

	public boolean wouldIHitMyAllyShip(Submarine ship, Optional<Submarine> optional, ProjectileLike enemyShip) {
		if (!this.canShootTorpedo()) {
			return false;
		}
		
		if(!optional.isPresent()){
			return false;
		}
		
		// safe to call get
		ProjectileLike allyShip = optional.get().clone();
		for (int i = 1; i <= MAX_SHOOT_STEPS_PREDICT; i++) {
			allyShip.step();
			double distance = ship.position.distance(allyShip.position);
			double distanceInSteps = distance / Torpedo.TORPEDO_SPEED;
			double error = distanceInSteps - i;

			if (Math.abs(error) < SHOOT_PREDICTION_EPSILON) {
				//ha koztunk van egy enemy ?
				if(distance < ship.position.distance(enemyShip.position)){
					return true;
				}
			}
		}
		return false;
	}
}
