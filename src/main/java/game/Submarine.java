package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import model.MapConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strategy.Captain;
import strategy.Strategy;

import com.sun.javafx.util.Utils;

public class Submarine extends PlayerObject {

	static Logger log = LoggerFactory.getLogger(Submarine.class);
	private static Integer SONAR_COOLDOWN;
	// private static Integer SONAR_RANGE;
	private static Integer SONAR_DURATION;
	private static Integer MAX_ACCELERATION;
	private static Integer MAX_STEERING;
	private static Integer MAX_SPEED;
	private static int TORPEDO_COOLDOWN;
	private static Integer SUBMARINE_RADIUS;
	private static int TORPEDO_RANGE;
	private final GameMap map;

	public Queue<Action> actionQueue = new LinkedBlockingQueue<>();

	//	private boolean torpedosShotInRound;
	public List<XVector> nextPositions = new ArrayList<>();
	private Strategy myStrategy;
	
	// mikor lottunk utoljara
	private int torpedosShotInRound = -1;
	
	// hanyadik korben vagyunk
	private int currentRoundNum = 0;
	
	public static void setBounds(MapConfiguration rules) {
		SONAR_COOLDOWN = rules.extendedSonarCooldown;
		// SONAR_RANGE = rules.extendedSonarRange;
		SONAR_DURATION = rules.extendedSonarRounds;

		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;
		TORPEDO_RANGE = rules.torpedoRange;
		TORPEDO_COOLDOWN = rules.torpedoCooldown;
		SUBMARINE_RADIUS = rules.submarineSize;

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

			// if I'm close enough, pop the Position out of the List AND add it
			// to the end
			if (current.distance(nextTargetPosition) < 100.0) {
				nextPositions.remove(0);
				nextPositions.add(nextTargetPosition);
				nextTargetPosition = nextPositions.get(1);
			}

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

			break;

		case CAMP: // a.k.a. kempele's
			/*
			 * Ez arrol szol, hogy jon az ellen, mi meg szejjel lojjuk
			 */

			if (!map.enemyShips.isEmpty()) {
				
				if (wouldTorpedoReachTarget(map.enemyShips.get(0))) {

					// TODO: a legmesszebbit kene ?

					ProjectileLike enemy = map.enemyShips.get(0);
					double targetVectorAngle2 = whatAngleToShootMovingTarget(enemy);

					// if (this.torpedoCooldown <= 0) {
					// this.actionQueue.add(Action.shoot(targetVectorAngle2));
					// }
					if (canShootTorpedo()) {
						// conn.shoot(map.gameId, this.id, targetVectorAngle2);
						this.actionQueue.add(Action.shoot(targetVectorAngle2));
						this.torpedosShotInRound = this.currentRoundNum;
						log.info("torpedosShotInRound={}", torpedosShotInRound);
					}
				}
				else {
					// TODO: chase enemy ship
				}
			}

			break;
		}
	}
	
	public void updateRounds(){
		this.currentRoundNum++;
	}

	public void setStrategy(Strategy strategy) {
		myStrategy = strategy;
	}
	

	 private boolean canShootTorpedo() {
	 		if(this.torpedosShotInRound < 0) return true;
	 		if(this.currentRoundNum - torpedosShotInRound >= this.TORPEDO_COOLDOWN) return true;
	 		return false;
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

}
