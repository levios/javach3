package game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import strategy.Strategy;
import ui.MainPaint;
//import utils.Transformer;
import connection.Connection;
import main.Main;
import model.*;
import static model.ErrorCode.*;

public class GameSession {
	private static Logger log = LoggerFactory.getLogger(GameSession.class);

	Long gameId;
	private Connection connection;
	private GameState state = GameState.UNINITIALIZED;

	public Game gameInfo;
	public MapConfiguration mapConfiguration;
	private ConnectionStatus connectionStatus;
	private Integer myScore;
	private Integer round;
	public GameMap map;
	public List<Submarine> myShips;
	private Map<Long, Submarine> myShipMap;

	private final boolean gui;
	private MainPaint GUI = null;

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

		if (GUI == null && this.gui) {
			GUI = startUI(mapConfiguration.width, mapConfiguration.height);
		}

		if (full) {
			Submarine.setBounds(this.mapConfiguration);
			Torpedo.setBounds(this.mapConfiguration);
			// Submarines
			this.myShips = this.createShips(gameInfo.mapConfiguration);
			//map
			this.map = new GameMap(gameInfo.mapConfiguration, myShips.stream().map(ship -> ship.id).collect(Collectors.toList()));
		}

		this.connectionStatus = gameInfo.connectionStatus;
		this.evaluateStatus(gameInfo.status);

		// If gui is enabled we refresh the UI
		if (gui) {
			refreshUI();
		}
	}

	MainPaint startUI(int x, int y) {
		return Main.startUI(x, y);
	}

	void refreshUI() {
		GUI.refresh(this);
	}

	private List<Submarine> createShips(MapConfiguration mapConfiguration) {
		List<model.Submarine> submarineModels = this.connection.submarine(this.gameId);
		List<Submarine> submarines = submarineModels.stream().map(s ->
				new Submarine(s.id, s.owner.name, s.position.x, s.position.y, s.velocity, s.angle, mapConfiguration.width, mapConfiguration.height,
						this, connection))
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
		List<Long> gameList = this.connection.gameList();
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
				"\n| ID:    \t" + this.gameId +
				"\n| Status:\t" + this.state.toString() +
				"\n| Round:\t" + this.round +
				"\n| Score:\t" + this.myScore +
				"\n| Connection:\t" + this.connection +
				"\n-------------------------->";
		return statusInfo;
	}

	public void executeStrategy() {
		List<Object> sonarReadings = this.myShips.stream().map(s -> {
			List<Entity> entities = this.connection.sonar(this.gameId, s.id);
			SonarReadings readings = new SonarReadings(entities, s.position, this.mapConfiguration.sonarRange);
			this.map.applyReadings(readings);
			return readings;
		}).collect(Collectors.toList());

//		this.myShips.forEach(s -> {
//			this.connection.move(this.gameId, s.id, this.mapConfiguration.maxAccelerationPerRound, this.mapConfiguration.maxSteeringPerRound);
//		});
		
		//if(no enemy ships around)
//		this.myShips.forEach(ship -> ship.setStrategy(Strategy.MOVEAROUND));
		this.myShips.get(0).setStrategy(Strategy.MOVEAROUND);
		this.myShips.get(1).setStrategy(Strategy.CAMP);
		//else
		// attach enemy ship
		
//		this.myShips.forEach(ship -> ship.executeStrategy());
		this.myShips.get(0).executeStrategy();
		this.myShips.get(1).executeStrategy();
		
//		}

	}
}
