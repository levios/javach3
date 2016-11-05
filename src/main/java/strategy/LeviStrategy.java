//package strategy;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Map.Entry;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.apache.log4j.Logger;
//
//import connection.Connection;
//import builder.units.Builder;
//import eu.loxon.centralcontrol.ActionCostResponse;
//import eu.loxon.centralcontrol.RadarRequest;
//import eu.loxon.centralcontrol.ResultType;
//import eu.loxon.centralcontrol.Scouting;
//import eu.loxon.centralcontrol.WsBuilderunit;
//import eu.loxon.centralcontrol.WsCoordinate;
//import eu.loxon.centralcontrol.WsDirection;
//import helper.Helper;
////import levi.actions.ActionQueueManager;
//import levi.actions.ActionTypes;
//import levi.actions.MyObjectType;
//
//public class LeviStrategy extends Strategy {
//	static Logger log = Logger.getLogger(LeviStrategy.class.getName());
//
//	boolean myTurn = false;
//
//	public LeviStrategy(Connection conn) {
//		CONN = conn;
//	}
//
//	static boolean END = false;
//
//	@Override
//	public void run() {
//		try {
//			startGameResponse = control.startGame(null);
//			DIMX = startGameResponse.getSize().getX();
//			DIMY = startGameResponse.getSize().getY();
//
//			BOARD = new MyObjectType[DIMX + 1][];
//			for (int i = 0; i <= DIMX; ++i) {
//				BOARD[i] = new MyObjectType[DIMY + 1];
//			}
//			for (int i = 0; i <= DIMX; ++i) {
//				for (int j = 0; j <= DIMY; ++j) {
//					if (j == 0 || j == DIMY || i == 0 || i == DIMX) {
//						BOARD[i][j] = MyObjectType.OBSIDIAN;
//					} else {
//						BOARD[i][j] = MyObjectType.UNKNOWN;
//					}
//				}
//			}
//
//			for (WsBuilderunit iterable_element : startGameResponse.getUnits()) {
//				builders.put(iterable_element.getUnitid(), new Builder(
//						iterable_element.getCord(), WsDirection.DOWN,
//						iterable_element.getUnitid()));
//			}
//
//			getSpaceShuttlePosResponse = control.getSpaceShuttlePos(null);
//			spaceShuttlePosition = getSpaceShuttlePosResponse.getCord();
//
//			invertSpaceShuttlePosition = Helper
//					.getInversePosition(spaceShuttlePosition);
//			invertXSpaceShuttlePosition = Helper.getXInversePosition(
//					spaceShuttlePosition, 0);
//			invertYSpaceShuttlePosition = Helper.getYInversePosition(
//					spaceShuttlePosition, 0);
//
//			// if(DIMX != DIMY)
//			// builder0Pos = invertSpaceShuttlePosition;
//			// else
//			// builder0Pos = Helper.getYInversePosition(spaceShuttlePosition,
//			// Math.min(4, DIMX));
//			builder1Pos = Helper.getClosestCorner(spaceShuttlePosition);
//
//			// builder3Pos = invertXSpaceShuttlePosition;
//			builder3Pos = Helper.getClosestCorner(spaceShuttlePosition);
//			builder2Pos = invertYSpaceShuttlePosition;
//
//			// if(DIMY > DIMX)
//			// builder3Pos = Helper.getYInversePosition(spaceShuttlePosition,
//			// Math.min(4, DIMX));
//			// else
//
//			builder0Pos = spaceShuttlePosition;
//			// builder0Pos = Helper.getXInversePosition(spaceShuttlePosition,
//			// Math.min(4, DIMY));
//
//			for (Map.Entry<?, Builder> entry : builders.entrySet()) {
//				if (!entry.getValue().pos.equals(spaceShuttlePosition)) {
//					entry.getValue().isOutOfShuttle = true;
//					log.info("Már alapból kint volt: " + entry.getKey());
//				}
//			}
//
//			BOARD[spaceShuttlePosition.getX()][spaceShuttlePosition.getY()] = MyObjectType.SHUTTLE;
//
//			// lekérdezzünk a kijárat helyét
//			getSpaceShuttleExitPosResponse = control
//					.getSpaceShuttleExitPos(null);
//			exitPosition = getSpaceShuttleExitPosResponse.getCord();
//
//			// Azt az egységet, amelyik rossz irányban áll (a kijárat felé
//			// kezdene el elindulni) elfordítjuk
//			WsDirection wrongDir = Helper.getDirectionFromCoord(exitPosition,
//					spaceShuttlePosition);
//			for (Entry<Integer, Builder> entries : builders.entrySet()) {
//				if (entries.getValue().currentDir == wrongDir) {
//					entries.getValue().currentDir = Helper.turn90(entries
//							.getValue().currentDir);
//				}
//			}
//
//			actionCostResponse = control.getActionCost(null);
//			fillUpCOSTS(actionCostResponse);
//			explosivesLeft = actionCostResponse.getAvailableExplosives();
//			availableActionPoints = actionCostResponse
//					.getAvailableActionPoints();
//
//			currentBuilder = new Builder(null, wrongDir, -1);
//
//			int waitTime = 167;
//
//			while (true) {
//				try {
//					isMyTurnResponse = control.isMyTurn(null);
//					// megvárom amig en jovok
//					while (!isMyTurnResponse.isIsYourTurn()) {
//						log.info("Nem én jövök");
//						myTurn = false;
//						Thread.sleep(waitTime);
//						isMyTurnResponse = control.isMyTurn(null, false);
//					}
//
//					if (currentBuilder.unitNum == isMyTurnResponse.getResult()
//							.getBuilderUnit()) {
//						log.info("Ha már az elobb sem tudtam lepni, most sem fogok tudni");
//						Thread.sleep(waitTime);
//						continue;
//					}
//
//					// várok egy kicsit - hogy
//					// try{
//					// Thread.sleep(150);
//					// }catch(Exception e){
//					// e.printStackTrace();
//					// }
//
//					// Beallitom a soron levo egyseg szamat
//					currentBuilderNumber = isMyTurnResponse.getResult()
//							.getBuilderUnit();
//					currentBuilder = builders.get(currentBuilderNumber);
//					currentBuilder.actionPointsLeft = availableActionPoints;
//					lastActionResult = ResultType.DONE;
//
//					log.info("Újra én jövök: " + currentBuilderNumber);
//
//					// Ennyi penzt kolthetek
//					actionPointsLeft = isMyTurnResponse.getResult()
//							.getActionPointsLeft();
//
//					try {
//						while (!END && lastActionResult == ResultType.DONE) {
//							log.info("Jöhet a next");
//							lastActionResult = currentBuilder.next();
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//					log.info("End of turn: " + currentBuilderNumber);
//
//					if (isMyTurnResponse.getResult().getTurnsLeft() == 1
//							&& currentBuilderNumber == 3) {
//						log.info("End of game!");
//						END = true;
//					}
//
//					// if(actionPointsLeft > 0 && COSTS.get(ActionTypes.RADAR)
//					// <= actionPointsLeft){
//					// radar(currentBuilder);
//					// }
//
//					Thread.sleep(waitTime);
//					myTurn = true;
//
//				} catch (Exception e) {
//					e.printStackTrace();
//					Thread.sleep(waitTime);
//				}
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void radar(Builder currentBuilder2) {
//		WsCoordinate coord = currentBuilder2.pos;
//		List<WsCoordinate> list = new ArrayList<WsCoordinate>();
//		RadarRequest parameters = new RadarRequest(currentBuilder2.unitNum,
//				list);
//		// list.add();
//		radarResponse = control.radar(parameters);
//		for (Scouting sc : radarResponse.getScout()) {
//			BOARD[sc.getCord().getX()][sc.getCord().getY()] = Helper
//					.toMyObjType(sc);
//		}
//	}
//
//	private void fillUpCOSTS(ActionCostResponse actionCostResponse2) {
//		COSTS.put(ActionTypes.DRILL, actionCostResponse2.getDrill());
//		COSTS.put(ActionTypes.EXPLODE, actionCostResponse2.getExplode());
//		COSTS.put(ActionTypes.MOVE, actionCostResponse2.getMove());
//		COSTS.put(ActionTypes.RADAR, actionCostResponse2.getRadar());
//		COSTS.put(ActionTypes.WATCH, actionCostResponse2.getWatch());
//	}
//
//	// private void getOutOfSpaceShuttle(int unitNum) throws RemoteException {
//	// WatchRequest rq1 = new WatchRequest(unitNum);
//	// watchResponse = Strategy.control.watch(rq1);
//	//
//	// WsDirection wd =
//	// Helper.getDirectionFromCoord(LeviStrategy.spaceShuttlePosition,
//	// LeviStrategy.exitPosition);
//	//
//	// while (true) { // ez csak safety
//	// for (Scouting sc : watchResponse.getScout()) {
//	// if (sc.getCord().equals(LeviStrategy.exitPosition)) {
//	//
//	// if (sc.getObject() == ObjectType.Rock) {
//	//
//	// structureTunnelRequest = new StructureTunnelRequest(unitNum, wd);
//	// structureTunnelResponse =
//	// control.structureTunnel(structureTunnelRequest);
//	//
//	// if (structureTunnelResponse.getResult().getType() != ResultType.DONE) {
//	// log.warn("Itt valami nem stimmelt");
//	// }
//	// } else if (sc.getObject() == ObjectType.BuilderUnit) {
//	// // ha a kijaratnal van egy masik egyseg: na itt varni
//	// // kell, amig az el nem takarodik onnan
//	// try {
//	// wait();
//	// } catch (InterruptedException e) {
//	// // TODO Auto-generated catch block
//	// e.printStackTrace();
//	// }
//	// }
//	//
//	// // Move
//	// moveBuilderUnitRequest = new MoveBuilderUnitRequest(unitNum, wd);
//	// moveBuilderUnitResponse =
//	// control.moveBuilderUnit(moveBuilderUnitRequest);
//	//
//	// actionpoints = moveBuilderUnitResponse.getResult().getActionPointsLeft();
//	//
//	// if (moveBuilderUnitResponse.getResult().getType() == ResultType.DONE)
//	// return;
//	// }
//	// }
//	// }
//	// }
//}
