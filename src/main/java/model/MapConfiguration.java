package model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class MapConfiguration {

	@SerializedName("width")
	@Expose
	public Integer width;
	@SerializedName("height")
	@Expose
	public Integer height;
	@SerializedName("islandPositions")
	@Expose
	public List<IslandPosition> islandPositions = new ArrayList<IslandPosition>();
	@SerializedName("teamCount")
	@Expose
	public Integer teamCount;
	@SerializedName("submarinesPerTeam")
	@Expose
	public Integer submarinesPerTeam;
	@SerializedName("torpedoDamage")
	@Expose
	public Integer torpedoDamage;
	@SerializedName("torpedoHitScore")
	@Expose
	public Integer torpedoHitScore;
	@SerializedName("torpedoDestroyScore")
	@Expose
	public Integer torpedoDestroyScore;
	@SerializedName("torpedoHitPenalty")
	@Expose
	public Integer torpedoHitPenalty;
	@SerializedName("torpedoCooldown")
	@Expose
	public Integer torpedoCooldown;
	@SerializedName("sonarRange")
	@Expose
	public Integer sonarRange;
	@SerializedName("extendedSonarRange")
	@Expose
	public Integer extendedSonarRange;
	@SerializedName("extendedSonarRounds")
	@Expose
	public Integer extendedSonarRounds;
	@SerializedName("extendedSonarCooldown")
	@Expose
	public Integer extendedSonarCooldown;
	@SerializedName("torpedoSpeed")
	@Expose
	public Integer torpedoSpeed;
	@SerializedName("torpedoExplosionRadius")
	@Expose
	public Integer torpedoExplosionRadius;
	@SerializedName("roundLength")
	@Expose
	public Integer roundLength;
	@SerializedName("islandSize")
	@Expose
	public Integer islandSize;
	@SerializedName("submarineSize")
	@Expose
	public Integer submarineSize;
	@SerializedName("rounds")
	@Expose
	public Integer rounds;
	@SerializedName("maxSteeringPerRound")
	@Expose
	public Integer maxSteeringPerRound;
	@SerializedName("maxAccelerationPerRound")
	@Expose
	public Integer maxAccelerationPerRound;
	@SerializedName("maxSpeed")
	@Expose
	public Integer maxSpeed;
	@SerializedName("torpedoRange")
	@Expose
	public Integer torpedoRange;
	@SerializedName("rateLimitedPenalty")
	@Expose
	public Integer rateLimitedPenalty;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}