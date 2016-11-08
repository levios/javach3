package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

public class LeviCaptain extends Captain {

	public LeviCaptain(GameMap map) {
		super(map);
		// TODO Auto-generated constructor stub
	}

//	@Override
//	public List<XVector> planNextMoves(Submarine submarine) {
//		List<XVector> nextPositions = new ArrayList<>();
//		XVector startPosition = new XVector(200.0, 200.0);
//		// move around 200,200 -> 200,600 -> 1500,600 -> 1500,200
//		double width = this.map.width;
//		double height = this.map.height;
//		nextPositions.add(new XVector(startPosition.x, height - startPosition.y));
//		nextPositions.add(new XVector(width - startPosition.x, height - startPosition.y));
//		nextPositions.add(new XVector(width - startPosition.x, startPosition.y));
//		nextPositions.add(new XVector(startPosition.x, startPosition.y));
//		return nextPositions;
//	}

	public void executeStrategy(Submarine ship) {

		ship.actionQueue.clear();

		if (ship.usingExtendedSonar == false && ship.canUseExtendedSonar()) {
			ship.useExtendedSonar();
		} else if (ship.usingExtendedSonar == true) {
			// kapcsoljuk ki (a paintmodellhez kell)
			if (ship.currentRoundNum - ship.extendedSonarUsedInRound >= ship.SONAR_DURATION) {
				ship.usingExtendedSonar = false;
			}
		}

		if (!ship.map.enemyShips.isEmpty() && ship.canShootTorpedo()) {
			ProjectileLike enemyShip = ship.getClosestEnemyShip(this.map.enemyShips);
			if (enemyShip != null) {
				boolean willLikelyHit = ship.shootAtTarget(enemyShip);
				ship.chaseTarget(enemyShip);
			}
		}

		XVector current = ship.position;
		// get next coordinate I want to go
		if (ship.nextPositions.isEmpty()) {
			// this will fill the nextPositions
			ship.nextPositions = planNextMoves(ship);
		}

		XVector nextTargetPosition = ship.nextPositions.get(0);

		// if I'm close enough, pop the Position out of the List AND add
		// it to the end
		if (current.distance(nextTargetPosition) < 100.0) {
			ship.nextPositions.remove(0);
			ship.nextPositions.add(nextTargetPosition);
			nextTargetPosition = ship.nextPositions.get(0);
		}

		ship.gotoXYZ(current, nextTargetPosition);

	}
}
