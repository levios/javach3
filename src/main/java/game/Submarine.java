package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.javafx.util.Utils;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strategy.Strategy;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import connection.Connection;
import model.MapConfiguration;
import model.Owner;
import model.Position;

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

	private final GameMap map;

	public Queue<Action> actionQueue = new LinkedBlockingQueue<>();

	//	private boolean torpedosShotInRound;
	public List<Vector2D> nextPositions = new ArrayList<>();
	private Strategy myStrategy;

	public static void setBounds(MapConfiguration rules) {
		SONAR_COOLDOWN = rules.extendedSonarCooldown;
		// SONAR_RANGE = rules.extendedSonarRange;
		SONAR_DURATION = rules.extendedSonarRounds;

		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;

		TORPEDO_COOLDOWN = rules.torpedoCooldown;
		SUBMARINE_RADIUS = rules.submarineSize;

	}

	public int hp;
	public int sonarCooldown = 0;
	public int sonarDuration = 0;
	public int torpedoCooldown = 0;

	public Submarine(long id, String owner, double x, double y, double speed, double rotation, GameMap map) {
		super(id, owner, PlayerObjectType.SUBMARINE, x, y, SUBMARINE_RADIUS, speed, rotation);
		this.map = map;
		this.hp = 100;
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
		this.torpedoCooldown = Math.max(0, this.torpedoCooldown-1);
		this.sonarCooldown = Math.max(0, this.sonarCooldown-1);

		log.info("Torpedo cooldown: [{}: {}] ", this.id, this.torpedoCooldown);

		switch (this.myStrategy) {

			case MOVEAROUND:
				// get current coordinate
				Vector2D current = this.position;
				// get next coordinate I want to go
				if (nextPositions.isEmpty()) {
					// this will fill the nextPositions
					planNextMoves();
				}
				Vector2D nextTargetPosition = nextPositions.get(0);

				// if I'm close enough, pop the Position out of the List AND add it to the end
				if (current.distance(nextTargetPosition) < 100.0) {
					nextPositions.remove(0);
					nextPositions.add(nextTargetPosition);
					nextTargetPosition = nextPositions.get(1);
				}

				//calculate iranyvektor
				Vector2D targetVector = nextTargetPosition.subtract(current);

				// calculate angle of target vector
				double targetVectorAngle = getAngleOfVector(targetVector);
				log.info("TargetVectorAngle calculated: {}", targetVectorAngle);

				//current angle is in "angle"
				log.info("Current angle: {}", this.rotation);

				//calc wood-be-perfect angle
				double angleBetweenTwoVectors = targetVectorAngle - this.rotation;
				if (angleBetweenTwoVectors < -180.0) {
					angleBetweenTwoVectors += 360;
				} else if (angleBetweenTwoVectors > 180.0) {
					angleBetweenTwoVectors -= 360;
				}
				log.info("AngleBetweenTwoVectors calculated: {}", angleBetweenTwoVectors);

				//calc turn
				double turn = Utils.clamp(-MAX_STEERING, angleBetweenTwoVectors, MAX_STEERING);

				log.info("Turn calculated: {}", turn);
				//  calculate speed - TODO

				// validate that I wont crash into island or "palya szele" - TODO

				// go
				double acceleration;
				if (this.speed >= MAX_SPEED) acceleration = 0;
				else acceleration = Utils.clamp(0, MAX_SPEED - this.speed, MAX_ACCELERATION);

				log.info("Acceleration calculated: {}", acceleration);

//				conn.move(session.gameId, this.id, acceleration, turn);
				this.actionQueue.add(Action.move(turn, acceleration));

				break;

			case CAMP: // a.k.a. kempele's
			/* 
			 * Ez arrol szol, hogy jon az ellen, mi meg szejjel lojjuk
			 * */

				if (!map.enemyShips.isEmpty()) {
				
				// TODO: a legmesszebbit kene ?
				ProjectileLike enemy = session.map.enemyShips.get(0);
Vector2D enemyShip = new Vector2D(map.enemyShips.get(0).x(), map.enemyShips.get(0).y());

//current vector
Vector2D currentVector = new Vector2D(this.position.getX(), this.position.getY());

//target Vector
Vector2D targetVector2 = enemyShip.subtract(currentVector);
				
				double distanceFromTarget = currentVector.distance(targetVector2);
				int numberOfRoundsToReachTarget = (int) Math.ceil(distanceFromTarget/this.session.mapConfiguration.torpedoSpeed);
				
				
				// find out where enemy ship will be in numberOfRoundsToReachTarget rounds
				Vector v = Vector.unit(enemy.rotation);
				v = v.scale(enemy.speed);
				
				//ha tul kozel van, mozogjon ellenkezo iranyba
//				if(currentVector.distance(targetVector2) < session.mapConfiguration.torpedoExplosionRadius){
//					conn.move(session.gameId, this.id, session.mapConfiguration.maxAccelerationPerRound, rotationDiff)
//				}
				
				//hol lesz x kor mulva
				for(int i=0; i<numberOfRoundsToReachTarget; i++){
					targetVector2 = targetVector2.add(new Vector2D(v.x, v.y));
				}
				
				double targetVectorAngle2 = getAngleOfVector(targetVector2);
				if(targetVectorAngle2 < 0){
					targetVectorAngle2 += 360;
				}
				log.info("Ship[{}]: TargetVectorAngle calculated: {}", this.id, targetVectorAngle2);
				
				if(canShootTorpedo()) {
					this.actionQueue.add(Action.shoot(targetVectorAngle2));
					torpedosShotInRound = map.mapConfig.round;

				}


				break;
		}
	}

	/**
	 * /\
	 * |   /
	 * |  /
	 * | /__
	 * |/alfa)
	 * +----------------
	 * Return the angle [alfa] of this vector
	 *
	 * @param targetVector2
	 * @return
	 */
	private double getAngleOfVector(Vector2D targetVector2) {
		return Math.atan2(targetVector2.getY(), targetVector2.getX()) * 180 / Math.PI;
	}

	private void planNextMoves() {
		nextPositions.add(new Vector2D(1700, 800));
//		nextPositions.add(new Vector2D(width - startPosition.getX(), startPosition.getY()));
//		nextPositions.add(new Vector2D(width - startPosition.getX(), height - startPosition.getY()));
//		nextPositions.add(new Vector2D(startPosition.getX(), height - startPosition.getY()));
//		nextPositions.add(new Vector2D(startPosition.getX(), startPosition.getY()));
	}

	public void setStrategy(Strategy strategy) {
		myStrategy = strategy;
	}

	public void actionExecuted(Action a) {
		if (a instanceof Action.MoveAction) {
			Action.MoveAction ma = (Action.MoveAction) a;
			this.steer(ma.steering);
			this.accelerate(ma.acceleration);
			this.step();
		} else if (a instanceof Action.ShootAction) {
//			Action.ShootAction sa = (Action.ShootAction) a;
			this.torpedoCooldown = TORPEDO_COOLDOWN +1;
			log.info("Shoot successful for {}, updating cooldown to: {}", this.id, TORPEDO_COOLDOWN);
		}
	}

}
