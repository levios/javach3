package model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class SonarResponse {

	@SerializedName("entities")
	@Expose
	public List<Entity> entities = new ArrayList<Entity>();
	@SerializedName("message")
	@Expose
	public String message;
	@SerializedName("code")
	@Expose
	public Integer code;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
