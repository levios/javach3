package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

public class AlexCaptain extends Captain{

	public AlexCaptain(GameMap map) {
		super(map);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<XVector> planNextMoves(Submarine submarine) {
		List<XVector> nextPositions = new ArrayList<>();
		XVector gotoPosition0 = new XVector(700, 400);
		XVector gotoPosition1 = new XVector(700, 220);
		XVector gotoPosition2 = new XVector(100, 30);
		nextPositions.add(gotoPosition0);
		nextPositions.add(gotoPosition1);
		nextPositions.add(gotoPosition2);
		return nextPositions;
	}

}
