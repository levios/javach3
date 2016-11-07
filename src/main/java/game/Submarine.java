package game;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.MapConfiguration;

import com.sun.tools.javac.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strategy.Captain;
import strategy.Strategy;

import com.sun.javafx.util.Utils;

public class Submarine extends PlayerObject {

	private static double SHOOT_PREDICTION_EPSILON;
	private static int MAX_SHOOT_STEPS_PREDICT;
	//	private static Integer SONAR_COOLDOWN;
	// private static Integer SONAR_RANGE;
//	private static Integer SONAR_DURATION;
	public static Integer MAX_ACCELERATION;
	public static Integer MAX_STEERING;
	public static Integer MAX_SPEED;
	public static int TORPEDO_COOLDOWN;
	public static Integer SUBMARINE_RADIUS;

	static Logger log = LoggerFactory.getLogger(Submarine.class);
	private final GameMap map;

	public Queue<Action> actionQueue = new LinkedBlockingQueue<>();

	//	private boolean torpedosShotInRound;
	public List<XVector> nextPositions = new ArrayList<>();
	private Strategy myStrategy;

	// mikor lottunk utoljara
	private int torpedosShotInRound = -1;

	// hanyadik korben vagyunk
	private int currentRoundNum = 0;

	public List<XVector> futurePositions = new ArrayList<>();
	public List<XVector> futureTorpedoPositions = new ArrayList<>();

	public static void setBounds(MapConfiguration rules) {
//		SONAR_COOLDOWN = rules.extendedSonarCooldown;
//		SONAR_RANGE = rules.extendedSonarRange;
//		SONAR_DURATION = rules.extendedSonarRounds;

		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;

		TORPEDO_COOLDOWN = rules.torpedoCooldown;
		SUBMARINE_RADIUS = rules.submarineSize;


		MAX_SHOOT_STEPS_PREDICT = rules.torpedoRange;
		SHOOT_PREDICTION_EPSILON = 0.3;
	}

	public int hp;
	public int sonarCooldown = 0;
	public int sonarDuration = 0;
	public int torpedoCooldown = 0;
	private final Captain captain;

	public Submarine(long id, String owner, double x, double y, double speed, double rotation, GameMap map,
					 Captain captain) {
		super(id, owner, PlayerObjectType.SUBMARINE, x, y, SUBMARINE_RADIUS, speed, rotation);
		this.map = map;
		this.hp = 100;
		this.captain = captain;
	}

	public void validatePosition(double x, double y, double angle, double speed) {
		double predictionError = 0.0;
		predictionError += Math.abs(this.rotation - angle) + Math.abs(this.speed - speed);
		predictionError += Math.abs(this.position.getX() - x) + Math.abs(this.position.getY() - y);
		GameThread.log.info("Prediction error is " + predictionError);

		this.updatePosition(x, y, angle, speed);
	}

	/**
	 * executes current strategy
	 */
	public void executeStrategy() {
//		this.torpedoCooldown = Math.max(0, this.torpedoCooldown - 1);
		this.sonarCooldown = Math.max(0, this.sonarCooldown - 1);

		log.info("Torpedo cooldown: [{}: {}] ", this.id, this.torpedoCooldown);

		switch (this.myStrategy) {

			case MOVEAROUND:
				// get current coordinate
				XVector current = this.position;
				// get next coordinate I want to go
				if (nextPositions.isEmpty()) {
					// this will fill the nextPositions
					nextPositions = captain.planNextMoves(this);
				}
				XVector nextTargetPosition = nextPositions.get(0);
				log.info("Ship[{}]: NextTargetPosition calculated: {}", this.id, nextTargetPosition.getAngleInDegrees());

				if (this.actionQueue.size() < 10) {
					this.gotoXY(nextTargetPosition);
				}

				break;

			case CAMP: // a.k.a. kempele's
			/*
			 * Ez arrol szol, hogy jon az ellen, mi meg szejjel lojjuk
			 */

				if (!this.map.enemyShips.isEmpty()) {
					PlayerObject enemyShip = this.map.enemyShips.get(0);
					boolean willLikelyHit = this.shootAtTarget(enemyShip);
					if (!willLikelyHit && this.map.enemyShips.size() >= 2) {
						PlayerObject otherShip = this.map.enemyShips.get(1);
						boolean willProablyHitTheOtherOne = this.shootAtTarget(otherShip);
					}
				}

				break;
		}
	}

	public void updateRounds() {
		this.currentRoundNum++;
	}

	public void setStrategy(Strategy strategy) {
		myStrategy = strategy;
	}


	private boolean canShootTorpedo() {
		return this.torpedosShotInRound < 0 || this.currentRoundNum - torpedosShotInRound >= TORPEDO_COOLDOWN;
	}

	public void actionExecuted(Action a) {
		if (a instanceof Action.MoveAction) {
			Action.MoveAction ma = (Action.MoveAction) a;
			this.steer(ma.steering);
			this.accelerate(ma.acceleration);
			this.step();
		} else if (a instanceof Action.ShootAction) {
//			Action.ShootAction sa = (Action.ShootAction) a;
//			this.torpedoCooldown = TORPEDO_COOLDOWN +1;
//			log.info("Shoot successful for {}, updating cooldown to: {}", this.id, TORPEDO_COOLDOWN);
		}
	}

	public void gotoXY(XVector target) {
		Pair<ProjectileLike, List<Action.MoveAction>> route = calculateSteps(this, this.position, target, this.map);

		ProjectileLike simulator = this.clone();
		this.futurePositions = route.snd.stream().map(a -> {
			simulator.steer(a.steering);
			simulator.accelerate(a.acceleration);
			simulator.step();
			return simulator.position;
		}).collect(Collectors.toList());

		this.actionQueue.addAll(route.snd);
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
				//torpedo.step(i);
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

	public static Pair<ProjectileLike, List<Action.MoveAction>> calculateSteps(ProjectileLike projectile, XVector startingPoint, XVector target, GameMap map) {

		ProjectileLike ghostShip = projectile.clone();
		ghostShip.position = startingPoint;
		List<Circular> islands = map.islands;

		List<Action.MoveAction> moveActions = new ArrayList<>();

		while (ghostShip.position.distance(target) > ghostShip.speed) {
			XVector direction = target.subtract(ghostShip.position);
			double targetAngle = direction.getAngleInDegrees();
			double angleDiff = Utils.clamp(-MAX_STEERING, targetAngle - ghostShip.rotation, MAX_STEERING);
			double accelerationDiff = 0;
			if (ghostShip.speed <= MAX_SPEED) {
				accelerationDiff = Utils.clamp(-MAX_ACCELERATION, MAX_SPEED - ghostShip.speed, MAX_ACCELERATION);
			}
			ghostShip.accelerate(accelerationDiff);
			ghostShip.steer(angleDiff);
			ghostShip.step();

			Optional<Circular> collidingIsland = islands.stream().filter(i -> i.checkIntersection(ghostShip)).findFirst();
			if (collidingIsland.isPresent()) {
				Circular island = collidingIsland.get();
				XVector collisionPoint = ghostShip.position;
				XVector detourStep = collisionPoint.add(collisionPoint.subtract(island.position).unit().scale(ghostShip.r * 3));
				Pair<ProjectileLike, List<Action.MoveAction>> firstSegment = calculateSteps(projectile, startingPoint, detourStep, map);
				ProjectileLike lastStep = firstSegment.fst;
				Pair<ProjectileLike, List<Action.MoveAction>> lastSegment = calculateSteps(lastStep, detourStep, target, map);

				List<Action.MoveAction> steps = Stream.concat(firstSegment.snd.stream(), lastSegment.snd.stream()).collect(Collectors.toList());

				return new Pair<>(lastSegment.fst, steps);
			}

			moveActions.add(new Action.MoveAction(angleDiff, accelerationDiff));
		}

		return new Pair<>(ghostShip, moveActions);
	}

}
