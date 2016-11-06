package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class Submarine extends ProjectileLike {
	static Logger log = LoggerFactory.getLogger(Submarine.class);
	private static Integer SONAR_COOLDOWN;
	// private static Integer SONAR_RANGE;
	private static Integer SONAR_DURATION;
	private static Integer MAX_ACCELERATION;
	private static Integer MAX_STEERING;
	private static Integer MAX_SPEED;
	private static Integer TORPEDO_COOLDOWN;
	private static Integer SUBMARINE_RADIUS;
	
	private Vector2D startPosition;
	private  GameSession session;
	private final Connection conn;
	public List<Vector2D> nextPositions = new ArrayList<>();
	private int torpedosShotInRound;
	// epp milyen strategiat hajt vegre
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

	public String type;
	public Long id;
	public String owner;

	public Double hp;
	public Double sonarCooldown = 0.0;
	public Double sonarDuration = 0.0;
	public Double torpedoCooldown = 0.0;
	private double angle;
	private int width;
	private int height;

	public Submarine(long id, String owner, double x, double y, double speed, double rotation, int width, int height, GameSession session, Connection conn) {
		super(x, y, SUBMARINE_RADIUS, speed, rotation);
		this.id = id;
		this.owner = owner;
		this.type = "Submarine";
		this.width = width;
		this.height = height;
		this.session = session;
		this.conn = conn;
		startPosition = new Vector2D(x, y);
	}

	public void validatePosition(double x, double y, double angle, double speed) {
		double predictionError = 0.0;
		predictionError += Math.abs(this.rotation - angle) + Math.abs(this.speed - speed);
		predictionError += Math.abs(this.position.x - x) + Math.abs(this.position.y - y);
		GameThread.log.info("Prediction error is " + predictionError);

		this.updatePosition(x, y, angle, speed);
	}
	
	/**
	 * executes current strategy
	 */
	public void executeStrategy() {
		switch (this.myStrategy) {
		
		case MOVEAROUND:
			// get current coordinate
			Vector2D current = new Vector2D(this.position.x, this.position.y);
			// get next coordinate I want to go
			if(nextPositions.isEmpty()){
				// this will fill the nextPositions
				planNextMoves();
			}
			Vector2D nextTargetPosition = nextPositions.get(0);
			
			// if I'm close enough, pop the Position out of the List AND add it to the end
			if(current.distance(nextTargetPosition) < 100.0){
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
			log.info("Current angle: {}", angle);
			
			//calc wood-be-perfect angle
			double angleBetweenTwoVectors = targetVectorAngle - angle;
			if (angleBetweenTwoVectors < -180.0) {
				angleBetweenTwoVectors += 360;
			} else if (angleBetweenTwoVectors > 180.0) {
				angleBetweenTwoVectors -= 360;
			}
			log.info("AngleBetweenTwoVectors calculated: {}", angleBetweenTwoVectors);
			
			//calc turn
			double turn = angleBetweenTwoVectors;
			if(angleBetweenTwoVectors > session.mapConfiguration.maxSteeringPerRound){
				turn = session.mapConfiguration.maxSteeringPerRound;
			}
			if(angleBetweenTwoVectors < -session.mapConfiguration.maxSteeringPerRound){
				turn = -session.mapConfiguration.maxSteeringPerRound;
			}
			log.info("Turn calculated: {}", turn);
			//  calculate speed - TODO
			
			// validate that I wont crash into island or "palya szele" - TODO
			
			// go
//			conn.move(session.gameId, this.id, session.mapConfiguration.maxAccelerationPerRound, session.mapConfiguration.maxSteeringPerRound);
			double acceleration;
			if(this.speed < session.mapConfiguration.maxSpeed){
				acceleration = session.mapConfiguration.maxSpeed - this.speed;
			} else {
				acceleration = 0.0;
			}
			acceleration = Math.min(acceleration, session.mapConfiguration.maxAccelerationPerRound); 
			log.info("Acceleration calculated: {}", acceleration);
			
			conn.move(session.gameId, this.id, acceleration, turn);
			
			break; 
			
		case CAMP: // a.k.a. kempele's
			/* 
			 * Ez arrol szol, hogy jon az ellen, mi meg szejjel lojjuk
			 * */
			
			if(!session.map.enemyShips.isEmpty()){
				
				// TODO: a legmesszebbit kene ?
				Vector2D enemyShip = new Vector2D(session.map.enemyShips.get(0).x(), session.map.enemyShips.get(0).y());
				
				//current vector
				Vector2D currentVector = new Vector2D(this.position.x, this.position.y);
				
				//target Vector
				Vector2D targetVector2 = enemyShip.subtract(currentVector);
				
				double targetVectorAngle2 = getAngleOfVector(targetVector2);
				if(targetVectorAngle2 < 0){
					targetVectorAngle2 += 360;
				}
				log.info("Ship[{}]: TargetVectorAngle calculated: {}", this.id, targetVectorAngle2);
				
				if(canShootTorpedo()) {
					conn.shoot(session.gameId, this.id, targetVectorAngle2);
					torpedosShotInRound = session.gameInfo.round;
				}
			}
			
			
			break;
		}
	}

	/**    /\
	 * |   /
	 * |  /
	 * | /__
	 * |/alfa)
	 * +----------------
	 * Return the angle [alfa] of this vector
	 * @param targetVector2
	 * @return
	 */
	private double getAngleOfVector(Vector2D targetVector2) {
		return Math.atan2(targetVector2.getY(), targetVector2.getX()) * 180 / Math.PI;
	}

	private void planNextMoves() {		
		nextPositions.add(new Vector2D(width - startPosition.getX(), startPosition.getY()));
		nextPositions.add(new Vector2D(width - startPosition.getX(), height - startPosition.getY())); 
		nextPositions.add(new Vector2D(startPosition.getX(), height - startPosition.getY())); 
		nextPositions.add(new Vector2D(startPosition.getX(), startPosition.getY())); 
	}

	public void setStrategy(Strategy strategy) {
		myStrategy = strategy;
	}
	
	private boolean canShootTorpedo() {
		if(torpedosShotInRound < 0) return true;
		if(session.gameInfo.round - torpedosShotInRound >= session.mapConfiguration.torpedoCooldown) return true;
		return false;
	}

}
