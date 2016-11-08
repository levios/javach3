//package game;
//
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Calendar;
//
//import org.apache.log4j.Logger;
//
//import ui.MainPaint;
//import connection.Connection;
//
//public class LeviGameThread extends Thread {
//
//
//	static Logger log = Logger.getLogger(GameThread.class.getName());
//
//	private Connection conn;
//
//	public static int POINTS = 0;
//	private long startTime;
//	private long cycleStartTime;
//
//	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd - HH:mm:ss");
//
//	private static final long TEST_LENGTH_IN_NANOSEC = 60 * 60 * 1000 * 1000000L;
//	private static final long TIME_TO_PRECALC = (TEST_LENGTH_IN_NANOSEC / 1000000) / 2;
//
//	private final boolean isDebug;
//	private final boolean gui;
//
//	private static final boolean INSTA_EXPRESS_ACTIONS = true;
//	public static final Integer[] VELOCITY = (Integer[]) Arrays.asList(170, 150, 130, 110).toArray();
//
//
//	public LeviGameThread(boolean isDebug, boolean gui, String server) {
//		conn = new Connection(server);
//		this.isDebug = isDebug;
//		this.gui = gui;
//
//		log.info("**********************************************");
//		log.info("**********************************************");
//		log.info("******           GAME STARTED           ******");
//		log.info("*Parameters:                                 *");
//		log.info("*Test Length:  " + TEST_LENGTH_IN_NANOSEC / 1000000000 + " seconds       *");
//		log.info("*Precalc Length:  " + TIME_TO_PRECALC / 1000 + " seconds           *");
//		log.info("**********************************************");
//		Calendar cal = Calendar.getInstance();
//		log.info("********    " + sdf.format(cal.getTime()) + "    *******");
//		log.info("**********************************************");
//	}
//
//	@Override
//	public void run() {
//
//		try {
//			doActuallyRun();
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			log.error(e);
//			e.printStackTrace();
//		} finally {
//
//			log.info("**********************************************");
//			log.info("******           GAME ENDED             ******");
//			log.info("**********************************************");
//			Calendar cal = Calendar.getInstance();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd - HH:mm:ss");
//			log.info("********    " + sdf.format(cal.getTime()) + "    *******");
//			log.info("**********************************************");
//
//		}
//	}
//
//	private void doActuallyRun() throws Exception {
//		startTime = System.nanoTime();
//		
//		GameSession game = new GameSession(conn, gui);
//		
//		game.start();
//
//		log.debug(game.getStatusInfo());
//
//		/************ FOCIKLUS ************/
//		while ((System.nanoTime() - startTime) < TEST_LENGTH_IN_NANOSEC) {
//
//			game.updateGameInfo();
//			game.updateShipStatus();
////			game.nextRound();
//			
//			Thread.sleep(1000);
//
//			cycleStartTime = System.nanoTime();
//
//		}
//	}
//}
