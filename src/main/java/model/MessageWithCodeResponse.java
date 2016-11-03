package model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class MessageWithCodeResponse {

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