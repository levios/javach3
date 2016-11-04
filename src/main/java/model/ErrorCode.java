package model;

public enum ErrorCode {
	OK(0, "OK"),
	NOT_INVITED(1, "A csapat nincs meghivva"),
	IN_PROGRESS(2, "Folyamatban levo jatek"),
	BAD_ID(3, "Nemletezo jatek id"),
	NO_RIGHTS(4, "Nincs jogosultsag a tengeralattjarohoz"),
	MYSTERY_5(5, "Nemletezo jatek id"),
	MYSTERY_6(6, "Nemletezo jatek id"),
	TORPEDO_ON_CD(7, "A torpedo cooldownon van"),
	SONAR_ON_CD(8, "A sonar cooldownon van"),
	GAME_ENDED(9, "A jatek nincs folyamatban"),
	ALREADY_MOVED(10, "A megadott hajo mar mozgott ebben a korben"),
	OVER_SPEED(11, "Tul nagy gyorsulas"),
	OVER_STEER(12, "Tul nagy kanyarodas");
	
	public String tagline;
	public Integer code;

	ErrorCode(Integer code, String tagline){
		this.code = code;
		this.tagline = tagline;
	}
	
	public static ErrorCode fromCode(Integer i){
		return ErrorCode.values()[i];
	}
}
