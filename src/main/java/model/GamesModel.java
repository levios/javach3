package model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class GamesModel {

	@SerializedName("games")
	@Expose
	private List<Integer> games = new ArrayList<Integer>();
	@SerializedName("message")
	@Expose
	private String message;
	@SerializedName("code")
	@Expose
	private Integer code;

	/**
	 * 
	 * @return The games
	 */
	public List<Integer> getGames() {
		return games;
	}

	/**
	 * 
	 * @param games
	 *            The games
	 */
	public void setGames(List<Integer> games) {
		this.games = games;
	}

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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
