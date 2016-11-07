package strategy;

import game.*;

import java.util.List;

import model.Game;

public abstract class Captain {
	
	protected GameMap map;
	
	public Captain(GameMap map){
		this.map = map;
	}

	public abstract List<XVector> planNextMoves(Submarine submarine);
	
}
