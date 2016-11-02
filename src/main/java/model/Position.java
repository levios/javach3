package model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Position {

	@SerializedName("x")
	@Expose
	public Integer x;
	@SerializedName("y")
	@Expose
	public Integer y;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}