package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Game;

public abstract class Captain {
	static Logger log = LoggerFactory.getLogger(Submarine.class);
	protected GameMap map;
	protected List<Submarine> myShips;
	
	public Captain(GameMap map){
		this.map = map;
	}
	
	public void setShips(List<Submarine> ships){
		this.myShips = ships;
	}

	public List<XVector> planNextMoves(Submarine submarine){
		return planRouteForShips(submarine);
	}
	
	public abstract void executeStrategy(Submarine ships);
	
	
	/**
	 * Lesz egy megtervezett trajektoria, amin a hajok haladni fognak
	 * Alap utvonal: 0,2,4, stb. ship-ek oramutato jarasaval megegyezoen, tobbi ellentetesen
	 * 
	 * 
	 */
	public List<XVector> planRouteForShips(Submarine ship) {
		List<XVector> route = new ArrayList<>();
		double sonarRange = map.mapConfig.sonarRange;
		double top = map.mapConfig.height  - sonarRange;
		double right = map.mapConfig.width - 300;
		double longerSonarRange = sonarRange;
		if(myShips.indexOf(ship) % 2 == 0){
			//oramutato jarasaval megegyezoen			
			log.info("ship id {} goes up", ship.id);
			//fent	
			
			for(int x = (int)ship.x(); x<right; x+=100){
				route.add(new XVector(x, top));
			}
			for(int y = (int)top; y>sonarRange; y-=100){
				route.add(new XVector(right, y));
			}
			for(int x = (int)right; x>sonarRange; x-=100){
				route.add(new XVector(x, sonarRange));
			}
			for(int y = (int)sonarRange; y<top; y+=100){
				route.add(new XVector(sonarRange, y));
			}
			for(int x = (int)sonarRange; x<ship.x(); x+=100){
				route.add(new XVector(x, top));
			}
		} else {
			//oramutato jarasaval ellentetesen	
			log.info("ship id {} goes down", ship.id);
			//alul && magasabban
			
			for(int x = (int)ship.x(); x<right; x+=100){
				route.add(new XVector(x, longerSonarRange));
			}
			for(int y = (int)longerSonarRange; y<top; y+=100){
				route.add(new XVector(right, y));
			}
			for(int x = (int)right; x>longerSonarRange; x-=100){
				route.add(new XVector(x, top));
			}
			for(int y = (int)top; y>longerSonarRange; y-=100){
				route.add(new XVector(longerSonarRange, y));
			}
			for(int x = (int)longerSonarRange; x<ship.x(); x+=100){
				route.add(new XVector(x, longerSonarRange));
			}
		}
		
		return route;
	}
	
}
