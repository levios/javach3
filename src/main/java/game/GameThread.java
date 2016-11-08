package game;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import connection.Connection;

import static java.lang.System.currentTimeMillis;

public class GameThread extends Thread {


	static Logger log = LoggerFactory.getLogger(GameThread.class);

	private Connection conn;

	public static int POINTS = 0;
	private long startTime;
	private long cycleStartTime;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd - HH:mm:ss");

	private static final long TEST_LENGTH_IN_NANOSEC = 60 * 60 * 1000 * 1000000L;
	private static final long TIME_TO_PRECALC = (TEST_LENGTH_IN_NANOSEC / 1000000) / 2;

	private final boolean isDebug;
	private final boolean hasGui;

	private static final boolean INSTA_EXPRESS_ACTIONS = true;

	public GameThread(boolean isDebug, boolean hasGui, String server) {
		conn = new Connection(server);
		this.isDebug = isDebug;
		this.hasGui = hasGui;

		log.info("**********************************************");
		log.info("**********************************************");
		log.info("******           GAME STARTED           ******");
		log.info("*Parameters:                                 *");
		log.info("*Test Length:  " + TEST_LENGTH_IN_NANOSEC / 1000000000 + " seconds       *");
		log.info("*Precalc Length:  " + TIME_TO_PRECALC / 1000 + " seconds           *");
		log.info("**********************************************");
		Calendar cal = Calendar.getInstance();
		log.info("********    " + sdf.format(cal.getTime()) + "    *******");
		log.info("**********************************************");
	}

	@Override
	public void run() {

		try {
			doActuallyRun();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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

	private void doActuallyRun() throws Exception {
		startTime = System.nanoTime();
		
		GameSession game = new GameSession(conn, hasGui);
		
		game.start();

		log.debug(game.getStatusInfo());
		
		/************ FOCIKLUS ************/
		while (game.state != GameState.ENDED) {
			long timeBefore = currentTimeMillis();

			game.updateGameInfo();
			game.nextRound();
			game.lastTurnLength = currentTimeMillis() - timeBefore;

			log.debug("Last turn took {}ms", game.lastTurnLength);
			
			Thread.sleep(game.mapConfiguration.roundLength - game.lastTurnLength);

			cycleStartTime = System.nanoTime();

		}
	}
}
