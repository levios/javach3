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

import model.GameInfo;
import model.GameModel;
//import models.DropPackageModel;
//import models.GoModel;
//import models.PickPackageModel;
//import models.WhereIsModel;

import model.GamesModel;
import model.MoveRequest;
import model.MoveResponse;
import model.Submarine;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nkzawa.socketio.client.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

			int responseCode = conn.getResponseCode();
			// log.info("\nSending 'GET' request to URL : " + url);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			log.info("\n[" + sdf.format(cal.getTime()) + "]"
					+ " sending 'GET' request to URL : " + Url);
			if (responseCode != 200) {
				log.info("Response Code : " + responseCode);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
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

	public String sendPost(String Url, JSONObject data) {
		HttpURLConnection conn;
		try {
			url = new URL(URL_TO_READ + Url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("TEAMTOKEN", TOKEN);

			// Send post request
			log.info("Post parameters: " + prettyfy(data.toString()));
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(data.toString());
			wr.flush();
			wr.close();

			int responseCode = conn.getResponseCode();
			log.info("\nSending 'POST' request to URL : " + url);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			log.info("\n[" + sdf.format(cal.getTime()) + "]"
					+ " sending 'POST' request to URL : " + Url);
			
			if (responseCode != 200) {
				log.info("Response Code : " + responseCode);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
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
	 * POST
	 * http://server-adress:port/jc16-srv/game
	 * VALASZ: { "message": "OK", "code": 0, "id": 1187580416 }
	 */
	public GameModel createGame() {
		String Url = "game";
		String jSONObjectAsString = sendPost(Url, null);
		log.info(prettyfy(jSONObjectAsString));
		GameModel gameModel = new Gson().fromJson(jSONObjectAsString,
				GameModel.class);
		return gameModel;
	}
	
	public GamesModel gameList() {
		String Url = "game";
		String jSONObjectAsString = sendGet(Url);
		log.info(prettyfy(jSONObjectAsString));
		GamesModel gameModel = new Gson().fromJson(jSONObjectAsString,
				GamesModel.class);
		return gameModel;
	}
	
	/**
	 * POST 
	 * http://server-adress:port/jc16-srv/game/{gameId} 
	 * 1 - Nincs a csapat meghívva 
	 * 2 - Folyamatban lévõ játék 
	 * 3 - Nem létezõ gameId
	 */
	public GamesModel joinGame(Integer gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendPost(Url, null);
		log.info(prettyfy(jSONObjectAsString));
		GamesModel gameModel = new Gson().fromJson(jSONObjectAsString,
				GamesModel.class);
		return gameModel;
	}
	
	/**
	 * GET 
	 * http://server-adress:port/jc16-srv/game/{gameId} 
	 * 3 - Nem létezõ gameId
	 */
	public GameInfo gameInfo(Integer gameId) {
		String Url = "game/" + gameId;
		String jSONObjectAsString = sendGet(Url);
		log.info(prettyfy(jSONObjectAsString));
		GameInfo gameModel = new Gson().fromJson(jSONObjectAsString,
				GameInfo.class);
		return gameModel;
	}

	/**
	 * GET 
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine
	 * 3 - Nem létezõ gameId
	 */
	public Submarine submarine(Integer gameId) {
		String Url = "game/" + gameId + "/submarine";
		String jSONObjectAsString = sendGet(Url);
		log.info(prettyfy(jSONObjectAsString));
		Submarine submarine = new Gson().fromJson(jSONObjectAsString,
				Submarine.class);
		return submarine;
	}
	
	/**
	 * POST
	 * http://server-adress:port/jc16-srv/game/{gameId}/submarine/{submarineId}/move
	 * 1 - Nincs a csapat meghívva 
	 * 2 - Folyamatban lévõ játék 
	 * 3 - Nem létezõ gameId
	 */
	public MoveResponse move(Integer gameId, Integer submarineId, MoveRequest request) {
		String Url = "game/" + gameId + "/submarine/" + submarineId + "/move";
		String jSONObjectAsString = sendPost(Url, null);
		log.info(prettyfy(jSONObjectAsString));
		return new Gson().fromJson(jSONObjectAsString,
				MoveResponse.class);
	}
	
	private String prettyfy(String uglyJSONString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		String prettyJsonString = gson.toJson(je);
		return prettyJsonString;
	}

//	/**
//	 * VALASZ: { status: "PACKAGE_PICKED" (enum) remainingCapacity: 2 (Integer)
//	 * }
//	 */
//	public PickPackageModel pickPackage(Integer packageId) {
//		String Url = "pickPackage";
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("packageId", packageId.toString());
//		String ret = sendPost(Url, map);
//
//		JSONObject obj = new JSONObject(ret);
//		// sendIOMessage("PICKRESULT", obj);
//
//		PickStatus pStatus = PickStatus.valueOf(obj.getString("status"));
//
//		Integer remainingCapacity = null;
//		if (obj.get("remainingCapacity").toString() != "null")
//			remainingCapacity = Integer.parseInt(obj.get("remainingCapacity")
//					.toString());
//
//		log.info("status: " + pStatus + "\nremainingCapacity: "
//				+ remainingCapacity);
//
//		PickPackageModel model = new PickPackageModel();
//		model.setpStatus(pStatus);
//		model.setRemainingCapacity(remainingCapacity);
//		return model;
//	}
//
//	/**
//	 * VALASZ: { status: "PACKAGE_DROPPED" (enum) scoreIncrease: 0 (int) }
//	 */
//	public DropPackageModel dropPackage(Integer packageId) {
//		String Url = "dropPackage";
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("packageId", packageId.toString());
//		String ret = sendPost(Url, map);
//
//		JSONObject obj = new JSONObject(ret);
//		// sendIOMessage("DROPRESULT", obj);
//
//		DropStatus dStatus = DropStatus.valueOf(obj.getString("status"));
//		int scoreIncrease = obj.getInt("scoreIncrease");
//
//		log.info("status: " + dStatus + "\nscoreIncrease: " + scoreIncrease);
//
//		DropPackageModel model = new DropPackageModel();
//		model.setdStatus(dStatus);
//		model.setScoreIncrease(scoreIncrease);
//		return model;
//	}
//
//	/**
//	 * VALASZ: { status: "MOVING" , (enum) arriveAfterMs: 46000 , (Integer)
//	 * destination: "Tarantulon 6" (String) }
//	 */
//	public GoModel go(String destination) {
//		String Url = "go";
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("planetName", destination);
//		String ret = sendPost(Url, map);
//
//		JSONObject obj = new JSONObject(ret);
//		// sendIOMessage("GORESULT", obj);
//
//		GoStatus goStatus = GoStatus.valueOf(obj.getString("status"));
//		Integer arriveTime = null;
//		if (obj.get("arriveAfterMs").toString() != "null")
//			arriveTime = Integer.parseInt(obj.get("arriveAfterMs").toString());
//		String dest = null;
//		if (obj.get("destination").toString() != "null")
//			dest = obj.getString("destination");
//
//		log.info("status: " + goStatus + "\narriveAfterMs: " + arriveTime
//				+ "\ndestination: " + dest);
//
//		GoModel model = new GoModel();
//		model.setArriveTime(arriveTime);
//		model.setDest(dest);
//		model.setGoStatus(goStatus);
//		return model;
//	}
//
//	/**
//	 * VALASZ: { "planets": [ { "name":"Amazonia", "x":63.0, "y":6.0,
//	 * "packages": [ { packageId: 1370, originalPlanet: "Amazonia" targetPlanet:
//	 * "Stumbos IV" text: "atomreaktor" actualPlanet: "Amazonia" fee: 29 } ] } ]
//	 * }
//	 */
//	public List<Planet> getGalaxy() {
//		String Url = "getGalaxy";
//		String ret = sendGet(Url);
//
//		JSONObject obj = new JSONObject(ret);
//		// sendIOMessage("SOW", obj);
//
//		List<Planet> planets = new ArrayList<Planet>();
//
//		JSONArray arr = obj.getJSONArray("planets");
//		for (int i = 0; i < arr.length(); i++) {
//			String planetName = arr.getJSONObject(i).getString("name");
//			double x = arr.getJSONObject(i).getDouble("x");
//			double y = arr.getJSONObject(i).getDouble("y");
//
//			Planet planet = new Planet();
//			planet.setName(planetName);
//			planet.setX(x);
//			planet.setY(y);
//
//			JSONArray pac = arr.getJSONObject(i).getJSONArray("packages");
//			for (int j = 0; j < pac.length(); j++) {
//				int packageId = pac.getJSONObject(j).getInt("packageId");
//				String originalPlanet = pac.getJSONObject(j).getString(
//						"originalPlanet");
//				String targetPlanet = pac.getJSONObject(j).getString(
//						"targetPlanet");
//				String text = pac.getJSONObject(j).getString("text");
//				String actualPlanet = pac.getJSONObject(j).getString(
//						"actualPlanet");
//				int fee = pac.getJSONObject(j).getInt("fee");
//
//				Package pack = new Package();
//				pack.setActualPlanet(actualPlanet);
//				pack.setFee(fee);
//				pack.setOriginalPlanet(originalPlanet);
//				pack.setPackageId(packageId);
//				pack.setTargetPlanet(targetPlanet);
//				pack.setText(text);
//				planet.add(pack);
//			}
//
//			planets.add(planet);
//		}
//
//		// for( Planet i : planets){
//		// log.info(i.toString());
//		// }
//		return planets;
//	}

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
