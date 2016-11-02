package model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class MoveRequest {

	@SerializedName("speed")
	@Expose
	public Double speed;
	@SerializedName("turn")
	@Expose
	public Double turn;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
