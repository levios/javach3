package connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import model.*;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nkzawa.socketio.client.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import static model.ErrorCode.*;

public class Connection {

	static Logger log = Logger.getLogger(Connection.class.getName());

	private static Connection connectToUrlUsingBasicAuthentication;
	private static final String USER_AGENT = "Mozilla/5.0";
	private static String URL_TO_READ;
	private static final String URL_TO_READ_DBG = "http://localhost:3000";
	private static final String DISPLAY_SERVER_URL = "http://javach-delanni.c9.io/";
	private static final String DISPLAY_SERVER_URL_DBG = "http://localhost:3000";
	private static String TOKEN;
	private URL url;

	private Socket io;

	private boolean debugMode = false;

	private String serverUrl;

	public Connection(String server, String token) {
		URL_TO_READ = server;
		Connection.TOKEN = token;
		this.serverUrl = this.debugMode ? URL_TO_READ_DBG : URL_TO_READ;

		if (this.debugMode) {
			connectSocketIO();
		}
	}

	private void connectSocketIO() {
		String displayURL = this.debugMode ? DISPLAY_SERVER_URL_DBG : DISPLAY_SERVER_URL;
		try {
			io = IO.socket(displayURL);
			io.connect();
			Thread.sleep(2000);
			if (io.connected()) {
				System.out.println("Running in proxy mode");
			} else {
				throw new Exception("Unable to connect to " + displayURL);
			}
		} catch (Exception e) {
			System.out.println("Running in distant mode");
			io = null;
		}
	}

	public void sendIOMessage(String topic, JSONObject obj) {
		if (io != null) {
			io.emit(topic, obj);
		}
	}

	public String sendGet(String Url) {
		HttpURLConnection conn;
		try {
			url = new URL(this.serverUrl + Url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("TEAMTOKEN", TOKEN);

			int responseCode = conn.getResponseCode();
			// log.info("\nSending 'GET' request to URL : " + url);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			log.info("\n[" + sdf.format(cal.getTime()) + "]" + " sending 'GET' request to URL : " + Url);
			if (responseCode != 200) {
				log.info("Response Code : " + responseCode);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// System.out.println(response.toString());
			return response.toString();

		} catch (Exception e) {
			if (e.getMessage().contains("503")) {
				throw new RuntimeException(e);
			}
			log.error(e);
			return "ERROR";
		}
	}

	public String sendPost(String Url, String jsonData) {
		HttpURLConnection conn;
		try {
			url = new URL(URL_TO_READ + Url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("TEAMTOKEN", TOKEN);

			// Send post request
			log.info("Post parameters: " + prettify(jsonData));
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(jsonData);
			wr.flush();
			wr.close();

			int responseCode = conn.getResponseCode();
			log.info("\nSending 'POST' request to URL : " + url);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			log.info("\n[" + sdf.format(cal.getTime()) + "]" + " sending 'POST' request to URL : " + Url);

			if (responseCode != 200) {
				log.info("Response Code : " + responseCode);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// System.out.println(response.toString());
			return response.toString();

		} catch (Exception e) {
			if (e.getMessage().contains("503")) {
				throw new RuntimeException(e);
			}
			log.error(e);
			return null;
		}
	}

	/**
	 * POST http://server-adress:port/jc16-srv/game
	 *  VALASZ: { 
	 *    "message": "OK",
	 *    "code": 0,
	 *    "id": 1187580416
	 *  }
	 */
	public Integer createGame() {
		String Url = "game";
		String jSONObjectAsString = sendPost(Url, null);
		log.info(prettify(jSONObjectAsString));
		GameModel gameModel = new Gson().fromJson(jSONObjectAsString, GameModel.class);

		ErrorCode c = ErrorCode.fromCode(gameModel.getCode());
		if (c != OK) {
			log.warn("Game creation failed: " + c.tagline);
			return null;
		} else {
			return gameModel.getId();
		}
	}

	public List<Integer> gameList() {
		String Url = "game";
		String jSONObjectAsString = sendGet(Url);
		log.info(prettify(jSONObjectAsString));
		GamesModel gameModel = new Gson().fromJson(jSONObjectAsString, GamesModel.class);

		ErrorCode c = ErrorCode.fromCode(gameModel.getCode());
		if (c == OK) {
			return gameModel.getGames();
		} else {
			return new ArrayList<Integer>();
		}
	}

	/**
	 * POST http://server-adress:port/jc16-srv/game/{gameId} 
	 * 1 - Nincs a csapat meghívva
	 * 2 - Folyamatban lévõ játék 
	 * 3 - Nem létezõ gameId
	 */
	public ErrorCode joinGame(Integer gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendPost(Url, null);
		log.info(prettify(jSONObjectAsString));
		MessageWithCodeResponse joinResult = new Gson().fromJson(jSONObjectAsString, MessageWithCodeResponse.class);

		ErrorCode errorCode = ErrorCode.fromCode(joinResult.code);
		return errorCode;
	}

	/**
	 * GET http://server-adress:port/jc16-srv/game/{gameId}
	 * 3 - Nem létezõ gameId
	 */
	public Game gameInfo(Integer gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendGet(Url);
		log.info(prettify(jSONObjectAsString));
		GameInfo gameModel = new Gson().fromJson(jSONObjectAsString, GameInfo.class);

		ErrorCode errorCode = ErrorCode.fromCode(gameModel.code);
		if (errorCode == OK) {
			return gameModel.game;
		} else if (errorCode == BAD_ID) {
			return null;
		} else {
			return null;
		}
	}

	/**
	 * GET http://server-adress:port/jc16-srv/game/{gameId}/submarine
	 * 3 - Nem létezõ gameId
	 */
	public List<Submarine> submarine(Integer gameId) {
		String Url = "game/" + gameId + "/submarine";
		String jSONObjectAsString = sendGet(Url);
		log.info(prettify(jSONObjectAsString));
		Submarines submarines = new Gson().fromJson(jSONObjectAsString, Submarines.class);

		ErrorCode errorCode = ErrorCode.fromCode(submarines.code);
		if (errorCode == OK) {
			return submarines.submarines;
		} else if (errorCode == BAD_ID) {
			return null;
		} else {
			return null;
		}
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/move
	 * 3 - Nem létező gameId
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 * 9 - A játék nincs folyamatban
	 * 10 - A megadott hajó már mozgott ebben a körben
	 * 11 - Túl nagy gyorsulás
	 * 12 - Túl nagy kanyarodás
	 */
	public ErrorCode move(Integer gameId, Integer submarineId, MoveRequest request) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/move";
		Gson gson = new Gson();
		String requestJson = gson.toJson(request);
		String jSONObjectAsString = sendPost(Url, requestJson);
		log.info(prettify(jSONObjectAsString));
		MessageWithCodeResponse moveResponse = new Gson().fromJson(jSONObjectAsString, MessageWithCodeResponse.class);

		ErrorCode errorCode = ErrorCode.fromCode(moveResponse.code);
		return errorCode;
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/shoot
	 *  3 - Nem létezõ gameId 
	 *  4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 *  7 - A torpedó cooldownon van
	 */
	public ErrorCode shoot(Integer gameId, Integer submarineId, ShootRequest request) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/shoot";
		Gson gson = new Gson();
		String requestJson = gson.toJson(request);
		String jSONObjectAsString = sendPost(Url, requestJson);
		log.info(prettify(jSONObjectAsString));
		ShootResponse shootResponse = new Gson().fromJson(jSONObjectAsString, ShootResponse.class);

		ErrorCode errorCode = ErrorCode.fromCode(shootResponse.code);
		return errorCode;
	}

	/**
	 * GET
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/sonar
	 * 3 - Nem létezõ gameId
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 */
	public List<Entity> sonar(Integer gameId, Integer submarineId) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/sonar";
		String jSONObjectAsString = sendGet(Url);
		log.info(prettify(jSONObjectAsString));
		SonarResponse sonarResponse = new Gson().fromJson(jSONObjectAsString, SonarResponse.class);

		ErrorCode errorCode = ErrorCode.fromCode(sonarResponse.code);
		if (errorCode == OK) {
			return sonarResponse.entities;
		} else {
			return null;
		}
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/sonar 
	 * 3 - Nem létezõ gameId
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 * 8 - Újratöltõdés elõtti hívás
	 */
	public ErrorCode extendSonar(Integer gameId, Integer submarineId) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/sonar";
		String jSONObjectAsString = sendPost(Url, null);
		log.info(prettify(jSONObjectAsString));
		MessageWithCodeResponse sonarActivationResult = new Gson().fromJson(jSONObjectAsString,
				MessageWithCodeResponse.class);

		ErrorCode errorCode = ErrorCode.fromCode(sonarActivationResult.code);
		return errorCode;
	}

	private String prettify(String uglyJSONString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		String prettyJsonString = gson.toJson(je);
		return prettyJsonString;
	}

	// public static Connection instance(String server, String username, String
	// pw) {
	// if(connectToUrlUsingBasicAuthentication == null){
	// connectToUrlUsingBasicAuthentication = new Connection(server, username,
	// pw);
	// }
	// return connectToUrlUsingBasicAuthentication;
	// }
	//
	// public static Connection instance() {
	// return connectToUrlUsingBasicAuthentication;
	// }
}
