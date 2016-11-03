package model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class CreateGameResponse {

	@SerializedName("message")
	@Expose
	private String message;
	@SerializedName("code")
	@Expose
	private Integer code;
	@SerializedName("id")
	@Expose
	private Integer id;

	/**
	 * 
	 * @return The message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 
	 * @param message
	 *            The message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 
	 * @return The code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * 
	 * @param code
	 *            The code
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * 
	 * @return The id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 *            The id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
	return ToStringBuilder.reflectionToString(this);
	}

}