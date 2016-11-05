package game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ui.MainPaint;
//import utils.Transformer;
import connection.Connection;
import main.Main;
import model.*;
import static model.ErrorCode.*;

public class GameSession {
	static Logger log = LoggerFactory.getLogger(GameSession.class);
	private Long gameId;
	private Connection connection;
	private GameState state = GameState.UNINITIALIZED;
	public Game gameInfo;
	private MapConfiguration mapConfiguration;
	private ConnectionStatus connectionStatus;
	private Integer myScore;
	private Integer round;
	public GameMap map;
	public List<Submarine> myShips;
	private Map<Long, Submarine> myShipMap;
	private final boolean gui;
	private MainPaint GUI = null;
	public int submarineSize;

	public GameSession(Connection connection, boolean gui) {
		this.connection = connection;
		this.myShipMap = new HashMap<>();
		this.gui = gui;
	}

	public void start(Long id) throws Exception {
		this.gameId = id;
		this.state = GameState.LOBBY_WAIT;
		this.joinGame(this.gameId);
		this.initialize();
	}

	public void start() throws Exception {
		if (this.state == GameState.UNINITIALIZED) {
			this.gameId = this.connection.createGame();
			this.state = GameState.LOBBY_WAIT;
		}

		if (this.state == GameState.LOBBY_WAIT) {
			if (this.gameId != null) {
				this.joinGame(this.gameId);
				this.initialize();
			} else {
				this.state = GameState.CORRUPT;
			}
		}
	}

	private void initialize() {
		this.updateGameInfo(true);

		this.state = GameState.RUNNING;

	}

	public void updateGameInfo() {
		this.updateGameInfo(false);
	}

	private void updateGameInfo(boolean full) {
		Game gameInfo = this.connection.gameInfo(this.gameId);

		this.gameInfo = gameInfo;
		this.round = gameInfo.round;
		this.myScore = gameInfo.scores.scores.myScore;
		this.mapConfiguration = gameInfo.mapConfiguration;
		
		if(GUI == null){
			GUI = startUI(mapConfiguration.width, mapConfiguration.height);
		}

		Submarine.setBounds(this.mapConfiguration);
		Torpedo.setBounds(this.mapConfiguration);

		if (full) {
			// Submarines 
			this.submarineSize = gameInfo.mapConfiguration.submarineSize;
			this.map = new GameMap(gameInfo.mapConfiguration);
			this.myShips = this.createShips(gameInfo.mapConfiguration);
		}

		this.connectionStatus = gameInfo.connectionStatus;
		this.evaluateStatus(gameInfo.status);
		
		// If gui is enabled we refresh the UI
		refreshUI();
	}
	
	MainPaint startUI(int x, int y){
		if(gui) {
			return  Main.startUI(x, y);
		}
		return null;
	}
	
	void refreshUI(){
		if(gui)  {
			GUI.refresh(this);
		}
	}

	private List<Submarine> createShips(MapConfiguration mapConfiguration) {
		List<model.Submarine> submarineModels = this.connection.submarine(this.gameId);
		List<Submarine> submarines = submarineModels.stream().map(s ->
				new Submarine(s.id, s.owner.name, s.position.x, s.position.y, s.velocity, s.angle))
				.collect(Collectors.toList());
		submarines.forEach(s -> myShipMap.put(s.id, s));
		return submarines;
	}

	public void updateShipStatus() {
		List<model.Submarine> submarineModels = this.connection.submarine(this.gameId);
		submarineModels.forEach(s -> {
			Submarine submarine = this.myShipMap.get(s.id);
			submarine.hp = s.hp;
			submarine.sonarCooldown = s.sonarCooldown;
			submarine.sonarDuration = s.sonarExtended;
			submarine.torpedoCooldown = s.torpedoCooldown;
			submarine.validatePosition(s.position.x, s.position.y, s.angle, s.velocity);
		});
	}

	private void evaluateStatus(String status) {
		switch (status) {
			case "WAITING":
				break;
			case "RUNNING":
				if (this.state != GameState.RUNNING) {
					// TODO: Kaki van, a jatek megy, mi meg nem
				}
				break;
			case "ENDED":
				this.gracefullyStop();
				break;
			default:
				break;
		}
	}

	private void gracefullyStop() {
		// TODO Auto-generated method stub

	}

	private void joinGame(Long id) throws Exception {
		List<Integer> gameList = this.connection.gameList();
		if (gameList.stream().anyMatch(o -> Objects.equals(o, id))) {
			ErrorCode joinResult = this.connection.joinGame(id);

			if (joinResult != OK) {
				throw new Exception("Join failed for some reason. Message was " + joinResult.tagline);
			} else {
				this.state = GameState.JOINED;
				log.info("game {} joined", id);
			}
		}
	}

	public String getStatusInfo() {
		String statusInfo = "" +
				"----------GAME------------>" +
				"| ID:    \t" + this.gameId +
				"| Status:\t" + this.state.toString() +
				"| Round:\t" + this.round +
				"| Score:\t" + this.myScore +
				"| Connection:\t" + this.connection +
				"-------------------------->";
		return statusInfo;
	}

	public void executeStrategy() {
		List<Object> sonarReadings = this.myShips.stream().map(s -> {
			List<Entity> sonar = this.connection.sonar(this.gameId, s.id);
			SonarReadings readings = new SonarReadings(sonar, s.position, this.mapConfiguration.sonarRange);
			this.map.applyReadings(readings);
			return readings;
		}).collect(Collectors.toList());

	}
}
