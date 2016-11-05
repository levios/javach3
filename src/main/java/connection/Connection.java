package connection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.nkzawa.socketio.parser.Parser;
import com.github.nkzawa.utf8.UTF8;
import org.slf4j.*;

import model.*;

import org.json.JSONObject;

import com.github.nkzawa.socketio.client.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import static model.ErrorCode.*;

public class Connection {
	private static Logger log = LoggerFactory.getLogger(Connection.class);

	private static Connection connectToUrlUsingBasicAuthentication;
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String URL_TO_READ_DBG = "http://localhost:3000";
	private static final String DISPLAY_SERVER_URL = "http://javach-delanni.c9users.io/";
	private static final String DISPLAY_SERVER_URL_DBG = "http://localhost:3000";
	private static String TOKEN = "8AEB9295F1223DB1D89B55980770DD8B";
	private URL url;

	private Socket io;

	private boolean isLocal = false;

	private String serverUrl;

	private static Gson GSON = new GsonBuilder().create();

	public Connection(String server) {
		this(server, false, false);
	}

	public Connection(String server, boolean hasDisplay, boolean isLocal) {
		this.isLocal = isLocal;

		this.serverUrl = this.isLocal ? URL_TO_READ_DBG : server;

		if (hasDisplay) {
			connectSocketIO();
		}
	}

	private void connectSocketIO() {
		String displayURL = this.isLocal ? DISPLAY_SERVER_URL_DBG : DISPLAY_SERVER_URL;
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

	private String sendGet(String topic, String Url) {
		HttpURLConnection conn;
		try {
			url = new URL(this.serverUrl + Url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("TEAMTOKEN", TOKEN);

			log.info("{} GET to [{}]", topic, Url);

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				String message = conn.getResponseMessage();
				log.error("Error: {} - {}", responseCode, message);
			}

			StringBuilder response = new StringBuilder();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}

			String jSONObjectAsString = response.toString();

//			io.emit(topic, jSONObjectAsString);

			log.info("Get RESPONSE: {}", prettify(jSONObjectAsString));
			return jSONObjectAsString;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private String sendPost(String topic, String Url, String jsonData) {
		HttpURLConnection conn;
		try {
			url = new URL(this.serverUrl + Url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("TEAMTOKEN", TOKEN);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json");

			// Send post request
			jsonData = jsonData == null ? "" : jsonData;
			log.info("{} POST to [{}]. INPUT: {}", topic, Url, prettify(jsonData));
			OutputStream os = conn.getOutputStream();
			os.write(jsonData.getBytes());
			os.close();
			os.flush();

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				String message = conn.getResponseMessage();
				Map<String, List<String>> headerFields = conn.getHeaderFields();
				log.error("Error: {} - {}", responseCode, message);
				log.error("Headers: {}", headerFields);
			}

			StringBuilder response = new StringBuilder();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}

			String jSONObjectAsString = response.toString();

//			io.emit(topic, jSONObjectAsString);

			log.info("Post RESPONSE: {}", prettify(jSONObjectAsString));
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
	public Long createGame() {
		String Url = "game";
		String jSONObjectAsString = sendPost("createGame", Url, null);
		CreateGameResponse gameModel = GSON.fromJson(jSONObjectAsString, CreateGameResponse.class);

		ErrorCode c = ErrorCode.fromCode(gameModel.getCode());
		if (c != OK) {
			log.warn("Game creation failed: " + c.tagline);
			return null;
		} else {
			return gameModel.getId();
		}
	}

	/**
	 * GET
	 * http://server-adress:port/jc16-srv/game
	 */
	public List<Long> gameList() {
		String Url = "game";
		String jSONObjectAsString = sendGet("gameList", Url);
		GamesListResponse gamesListResponse = GSON.fromJson(jSONObjectAsString, GamesListResponse.class);

		ErrorCode c = ErrorCode.fromCode(gamesListResponse.getCode());
		if (c == OK) {
			return gamesListResponse.getGames();
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * POST http://server-adress:port/jc16-srv/game/{gameId}
	 * 1 - Nincs a csapat meghívva
	 * 2 - Folyamatban lévõ játék
	 * 3 - Nem létezõ gameId
	 */
	public ErrorCode joinGame(Long gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendPost("joinGame", Url, null);
		MessageWithCodeResponse joinResult = GSON.fromJson(jSONObjectAsString, MessageWithCodeResponse.class);

		return ErrorCode.fromCode(joinResult.code);
	}

	/**
	 * GET http://server-adress:port/jc16-srv/game/{gameId}
	 * 3 - Nem letezo gameId
	 */
	public Game gameInfo(Long gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendGet("gameInfo", Url);
		GameInfoResponse gameModel = GSON.fromJson(jSONObjectAsString, GameInfoResponse.class);

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
	 * 3 - Nem letezo gameId
	 */
	public List<Submarine> submarine(Long gameId) {
		String Url = "game/" + gameId + "/submarine";
		String jSONObjectAsString = sendGet("submarine", Url);
		SubmarineResponse submarines = GSON.fromJson(jSONObjectAsString, SubmarineResponse.class);

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
	 * 11 - Tul nagy gyorsulas
	 * 12 - Tul nagy kanyarodas
	 */
	public ErrorCode move(Long gameId, Long submarineId, double speedDiff, double rotationDiff) {
		MoveRequest request = new MoveRequest();
		request.speed = speedDiff;
		request.turn = rotationDiff;
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/move";
		String requestJson = GSON.toJson(request);
		String jSONObjectAsString = sendPost("move", Url, requestJson);
		MessageWithCodeResponse moveResponse = GSON.fromJson(jSONObjectAsString, MessageWithCodeResponse.class);

		return ErrorCode.fromCode(moveResponse.code);
	}

	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/shoot
	 * 3 - Nem létezõ gameId
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 * 7 - A torpedó cooldownon van
	 */
	public ErrorCode shoot(Integer gameId, Integer submarineId, ShootRequest request) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/shoot";
		String requestJson = GSON.toJson(request);
		String jSONObjectAsString = sendPost("shoot", Url, requestJson);
		ShootResponse shootResponse = new Gson().fromJson(jSONObjectAsString, ShootResponse.class);

		return ErrorCode.fromCode(shootResponse.code);
	}

	/**
	 * GET
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/sonar
	 * 3 - Nem letezo gameId
	 * 4 - Nincs a csapatnak jogosultsaga a megadott tengeralattjot kezelni
	 */
	public List<Entity> sonar(Long gameId, Long submarineId) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/sonar";
		String jSONObjectAsString = sendGet("sonar", Url);
		SonarResponse sonarResponse = GSON.fromJson(jSONObjectAsString, SonarResponse.class);

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
	 * 3 - Nem letezo gameId
	 * 4 - Nincs a csapatnak jogosultsága a megadott tengeralattjárót kezelni
	 * 8 - Ujratoltodes elotti hivas
	 */
	public ErrorCode extendSonar(Integer gameId, Integer submarineId) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/sonar";
		String jSONObjectAsString = sendPost("extendSonar", Url, null);
		MessageWithCodeResponse sonarActivationResult = GSON.fromJson(jSONObjectAsString,
				MessageWithCodeResponse.class);

		return ErrorCode.fromCode(sonarActivationResult.code);
	}

	private String prettify(String uglyJSONString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		return gson.toJson(je);
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
