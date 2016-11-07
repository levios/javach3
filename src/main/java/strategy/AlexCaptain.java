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
		XVector gotoPosition = new XVector(1700, 800);
		nextPositions.add(gotoPosition);
		return nextPositions;
	}

}
