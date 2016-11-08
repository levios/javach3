package game;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import model.MapConfiguration;

import com.sun.tools.javac.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strategy.Captain;
import strategy.Strategy;

import com.sun.javafx.util.Utils;

public class Submarine extends PlayerObject {

	public static double SHOOT_PREDICTION_EPSILON;
	public static int MAX_SHOOT_STEPS_PREDICT;
	public static Integer SONAR_COOLDOWN;
	// private static Integer SONAR_RANGE;
	public static Integer SONAR_DURATION;
	public static Integer MAX_ACCELERATION;
	public static Integer MAX_STEERING;
	public static Integer MAX_SPEED;
	public static int TORPEDO_COOLDOWN;
	public static Integer SUBMARINE_RADIUS;
	public static int TORPEDO_RANGE;
	private static Logger log = LoggerFactory.getLogger(Submarine.class);
	public final GameMap map;

	public Deque<Action> actionQueue = new LinkedBlockingDeque<>();

	public List<XVector> nextPositions = new ArrayList<>();
	
	public List<XVector> chasePositions = new ArrayList<>();
	
	private Strategy myStrategy;

	// mikor lottunk utoljara
	public int torpedosShotInRound = -1;
	// mikor hasznaltunk sonart utoljara
	public int extendedSonarUsedInRound = -1;
	// hanyadik korben vagyunk
	public int currentRoundNum = 0;

	public List<XVector> futurePositions = new ArrayList<>();
	public List<XVector> futureTorpedoPositions = new ArrayList<>();

	public static void setBounds(MapConfiguration rules) {
//		SONAR_COOLDOWN = rules.extendedSonarCooldown;
//		SONAR_RANGE = rules.extendedSonarRange;
//		SONAR_DURATION = rules.extendedSonarRounds;

		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;
		TORPEDO_RANGE = rules.torpedoRange;
		TORPEDO_COOLDOWN = rules.torpedoCooldown;
		SUBMARINE_RADIUS = rules.submarineSize;
		SONAR_COOLDOWN = rules.extendedSonarCooldown;
		SONAR_DURATION = rules.extendedSonarRounds;
		MAX_SHOOT_STEPS_PREDICT = rules.torpedoRange;
		SHOOT_PREDICTION_EPSILON = 0.3;
	}

	public int hp;
	public int sonarCooldown = 0;
	public int sonarDuration = 0;
	public int torpedoCooldown = 0;
	private final Captain captain;
	public boolean usingExtendedSonar = false;

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
	
	public int getNumberOfRoundsToReachTarget(double distanceFromTarget){
		return (int) Math.ceil(distanceFromTarget / this.map.mapConfig.torpedoSpeed);
	}
	
	public boolean isTargetWithinTorpedoRange(int numberOfRoundsToReachTarget){
		if(numberOfRoundsToReachTarget > this.TORPEDO_RANGE) return false;
		return true;
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

	/**
	 * executes current strategy
	 */
	public void executeStrategy() {
		// this.torpedoCooldown = Math.max(0, this.torpedoCooldown - 1);
		this.sonarCooldown = Math.max(0, this.sonarCooldown - 1);

		log.info("Torpedo cooldown: [{}: {}] ", this.id, this.torpedoCooldown);

		switch (this.myStrategy) {

		case MOVEAROUND:

			captain.executeStrategy(this);

			break;

		case CAMP:

			break;
		default:
			break;
		}
	}

	public void updateRounds() {
		this.currentRoundNum++;
	}

	public void setStrategy(Strategy strategy) {
		myStrategy = strategy;
	}
	
	public boolean canUseExtendedSonar() {
		return this.extendedSonarUsedInRound < 0 || this.currentRoundNum - extendedSonarUsedInRound >= SONAR_COOLDOWN;
	}


	public boolean canShootTorpedo() {
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

	@SuppressWarnings("restriction")
	public void gotoXY(XVector target) {
		if (this.nextPositions.isEmpty()){
			this.nextPositions.add(target);
		} else {
			this.nextPositions.set(0, target);
		}
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

	@SuppressWarnings("restriction")
	public static Pair<ProjectileLike, List<Action.MoveAction>> calculateSteps(ProjectileLike projectile, XVector startingPoint, XVector target, GameMap map) {

		ProjectileLike ghostShip = projectile.clone();
		ghostShip.position = startingPoint;
		List<Circular> islands = map.islands;

//		double distanceToTarget = startingPoint.distance(target);

		List<Action.MoveAction> moveActions = new ArrayList<>();

		while (ghostShip.position.distance(target) > MAX_ACCELERATION*4) {
			if (moveActions.size() > 100){
				return new Pair<>(ghostShip, moveActions);
			}

			XVector direction = target.subtract(ghostShip.position);
			double targetAngle = direction.getAngleInDegrees();
			double angleDiff = Utils.clamp(-MAX_STEERING, targetAngle - ghostShip.rotation, MAX_STEERING);
			double accelerationDiff = 0;
			if (direction.getMagnitude() < ghostShip.speed + fullStopDistance(ghostShip.speed, MAX_ACCELERATION)) {
				accelerationDiff = -MAX_ACCELERATION;
			} else if (ghostShip.speed <= MAX_SPEED) {
				accelerationDiff = Utils.clamp(-MAX_ACCELERATION, MAX_SPEED - ghostShip.speed, MAX_ACCELERATION);
			}
			ghostShip.accelerate(accelerationDiff);
			ghostShip.steer(angleDiff);
			ghostShip.step();

			Optional<Circular> collidingIsland = islands.stream().filter(i -> i.checkIntersection(ghostShip)).findFirst();
			if (collidingIsland.isPresent()) {
				Circular island = collidingIsland.get();
				XVector collisionPoint = ghostShip.position;
				XVector detourStep = collisionPoint.add(collisionPoint.subtract(island.position).unit().scale(ghostShip.r * 5));
				Pair<ProjectileLike, List<Action.MoveAction>> firstSegment = calculateSteps(projectile, startingPoint, detourStep, map);
				ProjectileLike lastStep = firstSegment.fst;
				Pair<ProjectileLike, List<Action.MoveAction>> lastSegment = calculateSteps(lastStep, detourStep, target, map);

				List<Action.MoveAction> steps = Stream.concat(firstSegment.snd.stream(), lastSegment.snd.stream()).collect(Collectors.toList());

				return new Pair<>(lastSegment.fst, steps);
			}

			moveActions.add(new Action.MoveAction(angleDiff, accelerationDiff));

//			if (ghostShip.position.distance(target) > distanceToTarget){
//				return new Pair<>(ghostShip, moveActions);
//			} else {
//				distanceToTarget = ghostShip.position.distance(target);
//			}
		}

		return new Pair<>(ghostShip, moveActions);
	}

	private static double fullStopDistance(double speed, double maxDecrement) {
		double d = 0;
		while (speed > 0) {
			d += speed;
			speed -= Math.abs(maxDecrement);
		}
		return d;
	}
	
	public void useExtendedSonar(){
		log.info("Ship[{}] ExtendedSonar activated", this.id);
		this.actionQueue.add(Action.extendedSonar());
		this.extendedSonarUsedInRound = this.currentRoundNum;
		this.usingExtendedSonar = true;
	}
	
	
	public void gotoXYZ(XVector current, XVector nextTargetPosition) {
		log.info("Ship[{}]: NextTargetPosition calculated: {}", this.id, nextTargetPosition.getAngleInDegrees());

		// calculate iranyvektor
		XVector targetVector = XVector.subtract(nextTargetPosition, current);

		// calculate angle of target vector
		double targetVectorAngle = targetVector.getAngleInDegrees();
		log.info("Ship[{}]: TargetVectorAngle calculated: {}", this.id, targetVectorAngle);

		// current angle is in "angle"
		log.info("Ship[{}]: Current angle: {}", this.id, this.rotation);

		// calc wood-be-perfect angle
		double angleBetweenTwoVectors = targetVectorAngle - this.rotation;
		if (angleBetweenTwoVectors < -180.0) {
			angleBetweenTwoVectors += 360;
		} else if (angleBetweenTwoVectors > 180.0) {
			angleBetweenTwoVectors -= 360;
		}
		log.info("AngleBetweenTwoVectors calculated: {}", angleBetweenTwoVectors);

		// calc turn
		double turn = Utils.clamp(-MAX_STEERING, angleBetweenTwoVectors, MAX_STEERING);

		log.info("Ship[{}]: Turn calculated: {}", this.id, turn);
		// calculate speed - TODO

		// validate that I wont crash into island or "palya szele" - TODO

		// go
		double acceleration;
		if (this.speed >= MAX_SPEED)
			acceleration = 0;
		else
			acceleration = Utils.clamp(0, MAX_SPEED - this.speed, MAX_ACCELERATION);

		log.info("Ship[{}]: Acceleration calculated: {}", this.id, acceleration);

		// conn.move(session.gameId, this.id, acceleration, turn);
		this.actionQueue.add(Action.move(turn, acceleration));
	}
	
	public void chaseTarget(ProjectileLike enemyShip) {
		XVector current = this.position;
		
		ProjectileLike cloneShip = (ProjectileLike) enemyShip.clone();
		
		// project next position of enemy ship
		cloneShip.step();
		
		XVector nextTargetPosition =  cloneShip.position;
		
		// tegyük be az elejére
		this.nextPositions.add(0, nextTargetPosition);
	}
	
	public ProjectileLike getClosestEnemyShip(List<ProjectileLike> enemyShips) {
		XVector current = this.position;
		
		ProjectileLike closestEnemy = enemyShips.get(0);
		
		for(ProjectileLike enemy : enemyShips){
			if(current.distance(enemy.position) < current.distance(closestEnemy.position)){
				closestEnemy = enemy;
			}
		}
		
		//ilyenkor nem adok vissza semmit
		if(current.distance(closestEnemy.position) > 200) 
			return null;
		
		return closestEnemy;
	}

}
