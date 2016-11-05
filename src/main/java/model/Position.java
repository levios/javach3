package model;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Position {

	public Position(Double x, Double y) {
		super();
		this.x = x;
		this.y = y;
	}

	@SerializedName("x")
	@Expose
	public Double x;
	@SerializedName("y")
	@Expose
	public Double y;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}