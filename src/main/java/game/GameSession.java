package game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import connection.Connection;
import model.*;

import static model.ErrorCode.*;

public class GameSession {

	private Integer gameId;
	private Connection connection;
	private GameState state = GameState.UNINITIALIZED;
	private Game gameInfo;
	private MapConfiguration mapConfiguration;
	private ConnectionStatus connectionStatus;
	private Integer myScore;
	private Integer round;
	private GameMap map;
	private List<Submarine> myShips;
	private Map<Integer, Submarine> myShipMap;

	public GameSession(Connection connection) {
		this.connection = connection;
		this.myShipMap = new HashMap<>();
	}

	public void start(Integer id) throws Exception {
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

		Submarine.setBounds(this.mapConfiguration);
		Torpedo.setBounds(this.mapConfiguration);

		if (full) {
			this.map = new GameMap(gameInfo.mapConfiguration);
			this.myShips = this.createShips(gameInfo.mapConfiguration);
		}

		this.connectionStatus = gameInfo.connectionStatus;
		this.evaluateStatus(gameInfo.status);
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

	private void joinGame(Integer id) throws Exception {
		List<Integer> gameList = this.connection.gameList();
		if (gameList.stream().anyMatch(o -> Objects.equals(o, id))) {
			ErrorCode joinResult = this.connection.joinGame(id);

			if (joinResult != OK) {
				throw new Exception("Join failed for some reason. Message was " + joinResult.tagline);
			} else {
				this.state = GameState.JOINED;
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
