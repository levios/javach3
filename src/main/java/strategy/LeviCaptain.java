package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

public class LeviCaptain extends Captain {

	private GameMap map;

	public LeviCaptain() {
	}

	public void executeStrategy(GameMap map, List<Submarine> myShips) {

		this.map = map;

		myShips.forEach(ship -> {
			ship.actionQueue.clear();
			ship.tempTargetPosition = null;
			
			ship.tryActivateSonar();
			
			ProjectileLike enemyShip = ship.getClosestEnemyShip(map.enemyShips);

			if (enemyShip != null) {
				// boolean willLikelyHit = ship.shootAtTarget(enemyShip);
				log.info("Ship[{}] chasing target", ship.id);
				ship.chaseOrFleeTarget(enemyShip);
			}

			XVector current = ship.position;
			
			XVector nextTargetPosition = null;
			
			if (ship.tempTargetPosition != null) {
				
				nextTargetPosition = ship.tempTargetPosition;
				
//				// if I'm close enough, pop the Position out of the List AND add it to the end
//				if (current.distance(nextTargetPosition) < 50.0) {
//					ship.tempTargetPosition = null;
//				}
				
			} 
			else {
//			if (ship.tempTargetPosition == null) {

				// get next coordinate I want to go
				if (ship.nextPositions.isEmpty()) {
					// this will fill the nextPositions
					ship.nextPositions = planNextMoves(ship, myShips.indexOf(ship) % 2 == 0);
				}

				nextTargetPosition = ship.nextPositions.get(0);

				// if I'm close enough, pop the Position out of the List AND add it to the end
				if (current.distance(nextTargetPosition) < 100.0) {
					ship.nextPositions.remove(0);
					ship.nextPositions.add(nextTargetPosition);
					nextTargetPosition = ship.nextPositions.get(0);
				}
			}

			ship.gotoXY(nextTargetPosition);
			
			if (enemyShip != null) {
				if (!ship.wouldIHitMyAllyShip(ship, myShips.stream().filter(s-> s.id != ship.id).findAny(), enemyShip)) {
					boolean willLikelyHit = ship.shootAtTarget(enemyShip);
					if (willLikelyHit) {
						log.info("Ship would likely hit");
					} else {
						log.info("Ship would NOT likely hit or would get damaged");
					}
				} else {
					log.info("Ship would likely damage OTHER SHIP");
				}
			}
		});
	}

	public List<XVector> planNextMoves(Submarine submarine, boolean counterClockwise) {
		return planRouteForShips(submarine, counterClockwise);
	}

	/**
	 * Lesz egy megtervezett trajektoria, amin a hajok haladni fognak
	 * Alap utvonal: 0,2,4, stb. ship-ek oramutato jarasaval megegyezoen, tobbi ellentetesen
	 */
	public List<XVector> planRouteForShips(Submarine ship, boolean counterClockwise) {
		List<XVector> route = new ArrayList<>();
		double sonarRange = Submarine.SONAR_RANGE;
		double top = this.map.mapConfig.height - sonarRange;
		double right = this.map.mapConfig.width - 300;
		double longerSonarRange = sonarRange;
		if (counterClockwise) {
			//oramutato jarasaval megegyezoen
			log.info("ship id {} goes up", ship.id);
			//fent

			for (int x = (int) ship.x(); x < right; x += 100) {
				route.add(new XVector(x, top));
			}
			for (int y = (int) top; y > sonarRange; y -= 100) {
				route.add(new XVector(right, y));
			}
			for (int x = (int) right; x > sonarRange; x -= 100) {
				route.add(new XVector(x, sonarRange));
			}
			for (int y = (int) sonarRange; y < top; y += 100) {
				route.add(new XVector(sonarRange, y));
			}
			for (int x = (int) sonarRange; x < ship.x(); x += 100) {
				route.add(new XVector(x, top));
			}
		} else {
			//oramutato jarasaval ellentetesen
			log.info("ship id {} goes down", ship.id);
			//alul && magasabban

			for (int x = (int) ship.x(); x < right; x += 100) {
				route.add(new XVector(x, longerSonarRange));
			}
			for (int y = (int) longerSonarRange; y < top; y += 100) {
				route.add(new XVector(right, y));
			}
			for (int x = (int) right; x > longerSonarRange; x -= 100) {
				route.add(new XVector(x, top));
			}
			for (int y = (int) top; y > longerSonarRange; y -= 100) {
				route.add(new XVector(longerSonarRange, y));
			}
			for (int x = (int) longerSonarRange; x < ship.x(); x += 100) {
				route.add(new XVector(x, longerSonarRange));
			}
		}

		return route;
	}
}
