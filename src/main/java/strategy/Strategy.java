package strategy;

import java.util.ArrayList;
import java.util.List;

import model.*;
import connection.Connection;

public class Strategy extends Thread{
	
	public static volatile MyObjectType[][] BOARD = null;
	
	public static Connection CONN;
	
	public static GameInfoResponse Game_Info_Response;
	
	public static MapConfiguration Map_Configuration;
	
	public static int islandSize;
	
	public static int submarineSize;
	
	public static List<Position> islands = new ArrayList<>();
	
	public static List<Submarine> submarines = new ArrayList<>();
	
	public static void initialze(MapConfiguration mapConfig, SubmarineResponse submarineResponse){
		initializeIslands(mapConfig);
		initializeSubmarines(mapConfig, submarineResponse);
	}
	
	private static void initializeIslands(MapConfiguration mapConfig){
		islandSize = mapConfig.islandSize;
		for(Position islandPosition : mapConfig.islandPositions){
			islands.add(new Position(islandPosition.x, islandPosition.y));
		}
	}
	
	public static void initializeSubmarines(MapConfiguration mapConfig, SubmarineResponse submarineResponse){
		submarineSize = mapConfig.submarineSize;
		submarines.addAll(submarineResponse.submarines);
	}
	
}
