//package game;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import model.*;
//
//import org.apache.log4j.Logger;
//
//import connection.Connection;
//
//public class LeviGameThread extends Thread {
//
//	static Logger log = Logger.getLogger(GameThread.class.getName());
//
//	private Connection conn;
//
//	private boolean isDebug;
//
//	public LeviGameThread(boolean isDebug, String server, String token) {
//		conn = new Connection(server, token);
//		this.isDebug = isDebug;
//
//		log.info("**********************************************");
//		log.info("**********************************************");
//		log.info("******           GAME STARTED           ******");
//		log.info("**********************************************");
//	}
//
//	@Override
//	public void run() {
//		try {
//			doActuallyRun();
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//		} finally {
//			log.info("**********************************************");
//			log.info("**********************************************");
//			log.info("******           GAME ENDED             ******");
//			log.info("**********************************************");
//		}
//	}
//
//	private void doActuallyRun() {
//
//		CreateGameResponse createGameResponse = conn.createGame();
//
//		conn.gameInfo(createGameResponse.getId());
//
//		/************ FOCIKLUS ************/
//		while ((System.nanoTime() - startTime) < TEST_LENGTH_IN_NANOSEC) {
//
//			cycleStartTime = System.nanoTime();
//
//		}
//	}
//}
