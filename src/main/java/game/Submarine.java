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

	public static void setBounds(MapConfiguration rules){
		SONAR_COOLDOWN = rules.extendedSonarCooldown;
		// SONAR_RANGE = rules.extendedSonarRange;
		SONAR_DURATION = rules.extendedSonarRounds;
		
		MAX_ACCELERATION = rules.maxAccelerationPerRound;
		MAX_STEERING = rules.maxSteeringPerRound;
		MAX_SPEED = rules.maxSpeed;
		
		TORPEDO_COOLDOWN = rules.torpedoCooldown;
	}
	
	public String type;
	public Integer id;
	public Owner owner;

	public Integer hp;
	public Integer sonarCooldown;
	public Integer sonarDuration;
	public Integer torpedoCooldown;

	public Submarine(int id, double x, double y, double radius, double speed, double rotation) {
		super(x, y, radius, speed, rotation);
		
	}

}
