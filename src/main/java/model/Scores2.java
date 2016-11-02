package model;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import utils.Constants;

@Generated("org.jsonschema2pojo")
public class Scores2 {

//	@SerializedName("BOT")
//	@Expose
//	public Integer bOT;
	@SerializedName(Constants.TEAM_NAME)
	@Expose
	public Integer myScore;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}