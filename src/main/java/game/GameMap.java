package game;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.MapConfiguration;

public class GameMap {
	private static Logger log = LoggerFactory.getLogger(GameMap.class);

	private MapConfiguration rules;
	public Integer height;
	public Integer width;
	public List<Circular> islands;
	public List<Submarine> ships;
	private Map<Integer, Submarine> shipMap;
	public List<ProjectileLike> torpedos;
	public Map<Integer, Torpedo> torpedoMap = new HashMap<>();

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
		// delete torpedo map to only display torpedos that we see
		this.torpedos.clear();
		
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
			} else if (Objects.equals("Torpedo", e.type)) {
//				if (!torpedoMap.containsKey(id)) {
					Torpedo t = new Torpedo(e.id, e.owner.name, e.position.x, e.position.y, e.angle);
					this.torpedos.add(t);
//				} else {
//					Torpedo t = torpedos.get(id);
//					t.updatePosition(e.position.x, e.position.y, e.angle, e.velocity);
//				}
			} else {
				log.warn("Unidentified object of type {} spotted. Hopefully not a Kraken :S", e.type);
			}
	});
}
}
