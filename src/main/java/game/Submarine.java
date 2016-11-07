package game;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.javafx.util.Utils;

import com.sun.tools.javac.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strategy.Captain;
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
	public List<XVector> nextPositions = new ArrayList<>();
	private Strategy myStrategy;
	private int torpedosShotInRound;

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
		this.torpedoCooldown = Math.max(0, this.torpedoCooldown - 1);
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

					// TODO: a legmesszebbit kene ?

					ProjectileLike enemy = map.enemyShips.get(0);
					XVector enemyShip = new XVector(enemy.x(), enemy.y());

					// current vector
					XVector currentVector = new XVector(this.position.getX(), this.position.getY());

					// target Vector
					XVector targetVector2 = XVector.subtract(enemyShip, currentVector);

					double distanceFromTarget = currentVector.distance(targetVector2);
					int numberOfRoundsToReachTarget = (int) Math.ceil(distanceFromTarget / this.map.mapConfig.torpedoSpeed);

					// find out where enemy ship will be in
					// numberOfRoundsToReachTarget rounds
					XVector v = XVector.unit(enemy.rotation);
					v = v.scale(enemy.speed);

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
						targetVector2 = targetVector2.add(new XVector(v.x, v.y));
					}

					double targetVectorAngle2 = targetVector2.getAngleInDegrees();
					if (targetVectorAngle2 < 0) {
						targetVectorAngle2 += 360;
					}
					log.info("Ship[{}]: TargetVectorAngle calculated: {}", this.id, targetVectorAngle2);

//				if (this.torpedoCooldown <= 0) {
//					this.actionQueue.add(Action.shoot(targetVectorAngle2));
//				}
					if (canShootTorpedo()) {
						// conn.shoot(map.gameId, this.id, targetVectorAngle2);
						this.actionQueue.add(Action.shoot(targetVectorAngle2));
						torpedosShotInRound = this.map.mapConfig.rounds;

					}
				}

				break;
		}
	}

	public void setStrategy(Strategy strategy) {
		myStrategy = strategy;
	}


	private boolean canShootTorpedo() {
		if (torpedosShotInRound < 0) return true;
		if (map.mapConfig.rounds - torpedosShotInRound >= map.mapConfig.torpedoCooldown) return true;
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
			this.torpedoCooldown = TORPEDO_COOLDOWN + 1;
			log.info("Shoot successful for {}, updating cooldown to: {}", this.id, TORPEDO_COOLDOWN);
		}
	}

	public void gotoXY(XVector target){
		Pair<ProjectileLike, List<Action.MoveAction>> route = calculateSteps(this, this.position, target, this.map.islands);

		this.actionQueue.addAll(route.snd);
	}

	public static Pair<ProjectileLike, List<Action.MoveAction>> calculateSteps(ProjectileLike projectile, XVector startingPoint, XVector target, List<Circular> islands) {

		ProjectileLike ghostShip = projectile.clone();
		ghostShip.position = startingPoint;

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
				XVector detourStep = collisionPoint.add(collisionPoint.subtract(island.position)).unit().scale(ghostShip.r);
				Pair<ProjectileLike, List<Action.MoveAction>> firstSegment = calculateSteps(projectile, startingPoint, detourStep, islands);
				ProjectileLike lastStep = firstSegment.fst;
				Pair<ProjectileLike, List<Action.MoveAction>> lastSegment = calculateSteps(lastStep, detourStep, target, islands);

				List<Action.MoveAction> steps = Stream.concat(firstSegment.snd.stream(), lastSegment.snd.stream()).collect(Collectors.toList());

				return new Pair<>(lastSegment.fst, steps);
			}

			moveActions.add(new Action.MoveAction(angleDiff, accelerationDiff));
		}

		return new Pair<>(ghostShip, moveActions);
	}

}
