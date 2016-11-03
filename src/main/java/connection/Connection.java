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
import org.slf4j.*;
import javax.net.ssl.HttpsURLConnection;

import model.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nkzawa.socketio.client.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Connection {
	static Logger log = LoggerFactory.getLogger(Connection.class);
	
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
	
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public Connection(String server, String token) {
		URL_TO_READ = server;
		Connection.TOKEN = token;
		this.serverUrl = this.debugMode ? URL_TO_READ_DBG : URL_TO_READ;

		// connectSocketIO();
	}

	private void connectSocketIO() {
		String displayURL = this.debugMode ? DISPLAY_SERVER_URL_DBG
				: DISPLAY_SERVER_URL;
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

			log.info("Get to url [{}]", Url);

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				log.error("Response Code: {}", responseCode);
			}

			StringBuffer response = new StringBuffer();

			try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}

			String jSONObjectAsString = response.toString();
			log.info("Get RESPONSE: {}", prettyfy(jSONObjectAsString));
			return jSONObjectAsString;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
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
			jsonData = jsonData == null ? "" : jsonData;
			log.info("Post to url {}. INPUT {}", Url, prettyfy(jsonData));
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(jsonData);
			wr.flush();
			wr.close();

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				log.error("Response Code: {}", responseCode);
			}

			StringBuffer response = new StringBuffer();

			try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}

			String jSONObjectAsString = response.toString();
			log.info("Post RESPONSE: {}", prettyfy(jSONObjectAsString));
			return jSONObjectAsString;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * POST 
	 * http://server-adress:port/jc16-srv/game
	 */
	public CreateGameResponse createGame() {
		String Url = "game";
		String jSONObjectAsString = sendPost(Url, null);
		return GSON.fromJson(jSONObjectAsString,
				CreateGameResponse.class);
	}

	/**
	 * GET
	 * http://server-adress:port/jc16-srv/game
	 */
	public GamesListResponse gameList() {
		String Url = "game";
		String jSONObjectAsString = sendGet(Url);
		return GSON.fromJson(jSONObjectAsString,
				GamesListResponse.class);
	}

	/**
	 * POST 
	 * http://server-adress:port/jc16-srv/game/{gameId} 
	 * 1 - Nincs a csapat meghívva 
	 * 2 - Folyamatban lévõ játék 
	 * 3 - Nem létezõ gameId
	 */
	public MessageWithCodeResponse joinGame(Integer gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendPost(Url, null);
		return GSON.fromJson(jSONObjectAsString,
				MessageWithCodeResponse.class);
	}

	/**
	 * GET 
	 * http://server-adress:port/jc16-srv/game/{gameId} 
	 * 3 - Nem létezõ gameId
	 */
	public GameInfoResponse gameInfo(Integer gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendGet(Url);
		return GSON.fromJson(jSONObjectAsString,
				GameInfoResponse.class);
	}

	/**
	 * GET 
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine 
	 * 3 - Nem létezõ gameId
	 */
	public SubmarineResponse submarine(Integer gameId) {
		String Url = "game/" + gameId + "/submarine";
		String jSONObjectAsString = sendGet(Url);
		return GSON.fromJson(jSONObjectAsString,
				SubmarineResponse.class);
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/move 
	 * 1 - Nincs a csapat meghívva 
	 * 2 - Folyamatban lévõ játék 
	 * 3 - Nem létezõ gameId
	 */
	public MessageWithCodeResponse move(Integer gameId, Integer submarineId,
			MoveRequest request) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/move";
		String requestJson = GSON.toJson(request);
		String jSONObjectAsString = sendPost(Url, requestJson);
		return GSON.fromJson(jSONObjectAsString,
				MessageWithCodeResponse.class);
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/shoot 
	 * 3 - Nem létezõ gameId 
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni 
	 * 7 - A torpedó cooldownon van
	 */
	public ShootResponse shoot(Integer gameId, Integer submarineId,
			ShootRequest request) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/shoot";
		String requestJson = GSON.toJson(request);
		String jSONObjectAsString = sendPost(Url, requestJson);
		return GSON.fromJson(jSONObjectAsString, ShootResponse.class);
	}

	/**
	 * GET
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/sonar 
	 * 3 - Nem létezõ gameId 
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 */
	public SonarResponse sonar(Integer gameId, Integer submarineId) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/sonar";
		String jSONObjectAsString = sendGet(Url);
		return GSON.fromJson(jSONObjectAsString, SonarResponse.class);
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/sonar 
	 * 3 - Nem létezõ gameId
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 * 8 - Újratöltõdés elõtti hívás
	 */
	public MessageWithCodeResponse extendSonar(Integer gameId,
			Integer submarineId) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/sonar";
		String jSONObjectAsString = sendPost(Url, null);
		return GSON.fromJson(jSONObjectAsString,
				MessageWithCodeResponse.class);
	}

	private String prettyfy(String uglyJSONString) {
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		String prettyJsonString = GSON.toJson(je);
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
