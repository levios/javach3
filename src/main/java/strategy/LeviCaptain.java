package strategy;

import game.*;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class LeviCaptain extends Captain {

//	private strategy.Strategy myStrategy = Strategy.MOVEAROUND;

	private Deque<XVector> nextPositions;

	public LeviCaptain() {
		this.nextPositions = new LinkedBlockingDeque<>();
		this.nextPositions.add(new XVector(200, 200));
		this.nextPositions.add(new XVector(200, 600));
		this.nextPositions.add(new XVector(1500, 600));
		this.nextPositions.add(new XVector(1500, 200));
	}

	public void executeStrategy(GameMap map, List<Submarine> myShips) {
		this.map = map;

		if (myShips.size() > 0) {
			Submarine explorer = myShips.get(0);

			if (false && !this.map.enemyShips.isEmpty()) {
				explorer.actionQueue.clear();
				PlayerObject enemyShip = this.map.enemyShips.get(0);
				boolean willLikelyHit = explorer.shootAtTarget(enemyShip);

				explorer.gotoXY(enemyShip.position);
			} else {

				if (explorer.actionQueue.size() < 3) {
					explorer.actionQueue.clear();
					XVector nextTargetPosition = nextPositions.remove();
					nextPositions.add(nextTargetPosition);
					log.info("Ship[{}]: NextTargetPosition calculated. {}", explorer.id, nextTargetPosition);

					explorer.gotoXY(nextTargetPosition);
				}
			}

			explorer.tryActivateSonar();
		}

		if (myShips.size() > 1){
			Submarine battleship = myShips.get(1);

			if (false && !this.map.enemyShips.isEmpty()) {
				battleship.actionQueue.clear();
				PlayerObject enemyShip = this.map.enemyShips.get(0);
				boolean willLikelyHit = battleship.shootAtTarget(enemyShip);
			} else {
				if (battleship.actionQueue.size() < 3) {
					battleship.actionQueue.clear();
					battleship.gotoXY(battleship.position.add(new XVector(Math.random() * 400 - 100, Math.random() * 400 - 100)));
				}
			}

			battleship.tryActivateSonar();
		}
	}
}
