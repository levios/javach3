package model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Game {

	@SerializedName("id")
	@Expose
	public Integer id;
	@SerializedName("round")
	@Expose
	public Integer round;
	@SerializedName("scores")
	@Expose
	public Scores scores;
	@SerializedName("connectionStatus")
	@Expose
	public ConnectionStatus connectionStatus;
	@SerializedName("mapConfiguration")
	@Expose
	public MapConfiguration mapConfiguration;
	@SerializedName("status")
	@Expose
	public String status;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}