package game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.MapConfiguration;

public class GameMap {

	private MapConfiguration rules;
	private Integer height;
	private Integer width;
	private List<Circular> islands;
	private List<Submarine> ships;
	private List<ProjectileLike> torpedos;

	public GameMap(MapConfiguration mapConfiguration) {
		this.rules = mapConfiguration;

		this.width = mapConfiguration.width;
		this.height = mapConfiguration.height;

		this.islands = mapConfiguration.islandPositions.stream().map(position -> {
			return new Circular(position.x, position.y, mapConfiguration.islandSize);
		}).collect(Collectors.toList());
		
		this.ships = new ArrayList<Submarine>();
		this.torpedos = new ArrayList<ProjectileLike>();
	}
}
