package model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Entity {

	@SerializedName("type")
	@Expose
	public String type;
	@SerializedName("id")
	@Expose
	public Long id;
	@SerializedName("position")
	@Expose
	public Position position;
	@SerializedName("owner")
	@Expose
	public Owner owner;
	@SerializedName("velocity")
	@Expose
	public Double velocity;
	@SerializedName("angle")
	@Expose
	public Double angle;
	@SerializedName("roundsMoved")
	@Expose
	public Integer roundsMoved;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
