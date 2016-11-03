package game;

import java.util.List;

import connection.Connection;
import model.ConnectionStatus;
import model.ErrorCode;
import model.Game;
import model.GameModel;
import model.GamesModel;
import model.MapConfiguration;

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

	public GameSession(Connection connection) {
		this.connection = connection;
	}

	public void start(Integer id) throws Exception {
		this.gameId = id;
		this.state = GameState.LOBBY_WAIT;
		this.joinGame(this.gameId);
		this.initialize();
	}

	public void start() throws Exception {
		if (this.state == GameState.UNINITIALIZED) {
			Integer gameId = this.connection.createGame();

			this.gameId = gameId;
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
		this.updateGameInfo();
		
		//
	}

	private void updateGameInfo() {
		Game gameInfo = this.connection.gameInfo(this.gameId);

		this.gameInfo = gameInfo;
		this.round = gameInfo.round;
		this.myScore = gameInfo.scores.scores.myScore;
		this.mapConfiguration = gameInfo.mapConfiguration;
		this.updateMapInfo(gameInfo.mapConfiguration);
		this.connectionStatus = gameInfo.connectionStatus;
		// this.updateConnectionInfo(gameInfo.connectionStatus);
		this.evaluateStatus(gameInfo.status);
	}

	private void updateMapInfo(MapConfiguration mapConfiguration) {
		
	}

	private void evaluateStatus(String status) {
		switch (status) {
		case "WAITING":
			break;
		case "RUNNING":
			if (this.state != GameState.RUNNING){
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
		if (gameList.stream().anyMatch(o -> o == id)) {
			ErrorCode joinResult = this.connection.joinGame(id);

			if (joinResult != OK) {
				throw new Exception("Join failed for some reason. Message was " + joinResult.tagline);
			} else {
				this.state = GameState.JOINED;
			}
		}
	}

	public String getStatusInfo() {
		String statusInfo = "" + "----------GAME------------>" + "| ID:    \t" + this.gameId + "| Status:\t"
				+ this.state.toString() + "-------------------------->";
		return statusInfo;
	}

}
