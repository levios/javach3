package game;

/**
 * Created by alexszabo on 06/11/16.
 */

public enum PlayerObjectType {
	SUBMARINE,
	TORPEDO,
	UNIDENTIFIED;

	public static PlayerObjectType fromString(String s){
		switch (s.toLowerCase()){
			case "submarine":
				return SUBMARINE;
			case "torpedo:":
				return TORPEDO;
			default: return UNIDENTIFIED;
		}
	}
}
