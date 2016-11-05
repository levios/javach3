package game;

import java.util.*;
import java.util.stream.Collectors;

import model.MapConfiguration;

public class GameMap {

	private MapConfiguration rules;
	public Integer height;
	public Integer width;
	public List<Circular> islands;
	public List<Submarine> ships;
	private Map<Integer, Submarine> shipMap;
	private List<ProjectileLike> torpedos;

	public GameMap(MapConfiguration mapConfiguration) {
		shipMap = new HashMap<>();

		this.rules = mapConfiguration;

		this.width = mapConfiguration.width;
		this.height = mapConfiguration.height;

		this.islands = mapConfiguration.islandPositions.stream().map(position ->
				new Circular(position.x, position.y, mapConfiguration.islandSize)).collect(Collectors.toList());

		this.ships = new ArrayList<>();
		this.torpedos = new ArrayList<>();
	}

	public void applyReadings(SonarReadings readings) {
		readings.entities.forEach(e -> {
			Integer id = e.id;
			if (Objects.equals("Submarine", e.type)) {
				// TODO: watch out for own ships
				if (!shipMap.containsKey(id)) {
					Submarine ship = new Submarine(e.id, e.owner.name, e.position.x, e.position.y, e.velocity, e.angle);
					this.ships.add(ship);
					shipMap.put(id, ship);
				} else {
					Submarine submarine = shipMap.get(id);
					submarine.updatePosition(e.position.x, e.position.y, e.angle, e.velocity);
				}
			}
			if (Objects.equals("Torpedo", e.type)) {
				if (!shipMap.containsKey(id)) {
					Torpedo t = new Torpedo(e.id, e.owner.name, e.position.x, e.position.y, e.angle);
				} else {
					Submarine submarine = shipMap.get(id);
					submarine.updatePosition(e.position.x, e.position.y, e.angle, e.velocity);
				}
			}
	});
}
}
