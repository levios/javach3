package main;

import game.GameThread;
import connection.*;

public class Main {

	public static void main(String[] args) {
		try {
			//Thread game = new LeviThread(args[0], args[1], args[2]);
			//System.out.println("Starting game with parameters:" + args[0] + ", " + args[1] + ", " +args[2]);
			Thread game = new GameThread(false,args[0],args[1]);
			game.run();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
