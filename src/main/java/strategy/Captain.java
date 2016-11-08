package strategy;

import game.*;

import java.util.List;

import model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Captain {

	protected static Logger log = LoggerFactory.getLogger(Captain.class);

	protected GameMap map;

	public abstract void executeStrategy(GameMap map, List<Submarine> myShips);

}
