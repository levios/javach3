package game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.*;

import org.apache.log4j.Logger;

import connection.Connection;

public class GameThread extends Thread {


	static Logger log = Logger.getLogger(GameThread.class.getName());

	private Connection conn;
	
	public static int POINTS = 0;
	private long startTime;
	private long cycleStartTime;

	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy MMM dd - HH:mm:ss");

	private boolean isDebug;
	
	private static final long TEST_LENGTH_IN_NANOSEC = 60*60*1000 *1000000L;
	private static final long TIME_TO_PRECALC = (TEST_LENGTH_IN_NANOSEC/1000000)/2;
	private static final boolean INSTA_EXPRESS_ACTIONS = true;
	public static final Integer[] VELOCITY = (Integer[])Arrays.asList(170,150,130,110).toArray();

	public GameThread(boolean isDebug, String server, String token) {
		conn = new Connection(server,token);
		this.isDebug = isDebug;

		log.info("**********************************************");
		log.info("**********************************************");
		log.info("******           GAME STARTED           ******");
		log.info("*Parameters:                                 *");
		log.info("*Test Length:  " + TEST_LENGTH_IN_NANOSEC/1000000000 + " seconds       *");
		log.info("*Precalc Length:  " + TIME_TO_PRECALC/1000 + " seconds           *");
		log.info("**********************************************");
		Calendar cal = Calendar.getInstance();
		log.info("********    " + sdf.format(cal.getTime()) + "    *******");
		log.info("**********************************************");
	}

	@Override
	public void run() {
		
		try{
			doActuallyRun();
		} catch (RuntimeException e){
			e.printStackTrace();
		} finally {
		
			log.info("**********************************************");
			log.info("******           GAME ENDED             ******");
			log.info("**********************************************");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd - HH:mm:ss");
			log.info("********    " + sdf.format(cal.getTime()) + "    *******");
			log.info("**********************************************");
			
		}
	}

	private void doActuallyRun() {
		startTime = System.nanoTime();

		CreateGameResponse createGameResponse = conn.createGame();
		
		conn.gameInfo(createGameResponse.getId());
		
		/************ FOCIKLUS ************/
		while ( (System.nanoTime() - startTime) < TEST_LENGTH_IN_NANOSEC) {
			
			
			cycleStartTime = System.nanoTime();
			
		}
	}
}
