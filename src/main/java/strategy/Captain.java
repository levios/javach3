package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Captain {

	protected static Logger log = LoggerFactory.getLogger(Captain.class);

	public abstract void executeStrategy(GameMap map, List<Submarine> myShips);
	
}
