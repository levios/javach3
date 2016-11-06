package game;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import model.MapConfiguration;
import model.Owner;
import model.Position;

public class Submarine extends ProjectileLike {

	private static Integer SONAR_COOLDOWN;
	// private static Integer SONAR_RANGE;
	private static Integer SONAR_DURATION;
	private static Integer MAX_ACCELERATION;
	private static Integer MAX_STEERING;
	private static Integer MAX_SPEED;
	private static Integer TORPEDO_COOLDOWN;
	private static Integer SUBMARINE_RADIUS;

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

	public Submarine(long id, String owner, double x, double y, double speed, double rotation) {
		super(x, y, SUBMARINE_RADIUS, speed, rotation);
		this.id = id;
		this.owner = owner;
		this.type = "Submarine";
	}

	public void validatePosition(double x, double y, double angle, double speed) {
		double predictionError = 0.0;
		predictionError += Math.abs(this.rotation - angle) + Math.abs(this.speed - speed);
		predictionError += Math.abs(this.position.x - x) + Math.abs(this.position.y - y);
		GameThread.log.info("Prediction error is " + predictionError);

		this.updatePosition(x, y, angle, speed);
	}

}
