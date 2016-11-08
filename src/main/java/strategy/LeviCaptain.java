package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

public class LeviCaptain extends Captain {
	
	public LeviCaptain(GameMap map) {
		super(map);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<XVector> planNextMoves(Submarine submarine) {
		List<XVector> nextPositions = new ArrayList<>();
		XVector startPosition = new XVector(200.0, 200.0); 
		// move around 200,200 -> 200,600 -> 1500,600 ->  1500,200
		double width = this.map.width;
		double height = this.map.height;
		nextPositions.add(new XVector(startPosition.x, height - startPosition.y));
		nextPositions.add(new XVector(width - startPosition.x, height - startPosition.y));
		nextPositions.add(new XVector(width - startPosition.x, startPosition.y));
		nextPositions.add(new XVector(startPosition.x, startPosition.y));
		return nextPositions;
	}
	
	public void executeStrategy() {
		/* if enemies are around:
		 * 	foreach ship : myship:
		 * 		if ship.canShoot:
		 * 			if ship.wouldHitEnemy:
		 * 				if ship.wouldNotKillAnyOne:
		 * 					ship.shoot
		 * 				else:
		 * 					ship.move - closer/furher
		 * 			else:
		 * 				ship.move - closer
		 *  else:
		 *  	if can extendedSolar:
		 *  		do extendedSolar
		 *  	else
		 *  		move in cikk cakk
		 */
		
		
	}
}
