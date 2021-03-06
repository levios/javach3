package game;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.MapConfiguration;

import static utils.Constants.TEAM_NAME;

public class GameMap {
	private static Logger log = LoggerFactory.getLogger(GameMap.class);

	public MapConfiguration mapConfig;
	public Integer height;
	public Integer width;
	public List<Circular> islands;
	public List<ProjectileLike> enemyShips;
	public List<PlayerObject> torpedos;
//	public Map<Integer, ProjectileLike> enemyShipsMap = new HashMap<>();
//	public Map<Integer, Torpedo> torpedoMap = new HashMap<>();

	public GameMap(MapConfiguration mapConfiguration) {
		this.mapConfig = mapConfiguration;
		this.width = mapConfiguration.width;
		this.height = mapConfiguration.height;

		this.islands = mapConfiguration.islandPositions.stream().map(position ->
				new Circular(position.x, position.y, mapConfiguration.islandSize)).collect(Collectors.toList());

		this.enemyShips = new ArrayList<>();
		this.torpedos = new ArrayList<>();
	}

	public void applyReadings(SonarReadings readings) {
		// delete torpedos and enemy ships to only display torpedos that we see

		// TODO: Instead of deleting, just put them into a state, where they exist, but we can just approximate their position
		this.torpedos.clear();
		this.enemyShips.clear();

		readings.entities.forEach(e -> {
			Long id = e.id;
			if (Objects.equals("Submarine", e.type)) {

				if (!Objects.equals(e.owner.name, TEAM_NAME)) {
					PlayerObject enemy = new PlayerObject(e.id, e.owner.name,
							PlayerObjectType.SUBMARINE, e.position.x, e.position.y, mapConfig.submarineSize, e.velocity, e.angle);
					this.enemyShips.add(enemy);
				}

//				if (!enemyShipsMap.containsKey(id)) {
//					Submarine ship = new Submarine(e.id, e.owner.name, e.position.x, e.position.y, e.velocity, e.angle);
//					this.enemyShips.add(ship);
//					enemyShipsMap.put(id, ship);
//				} else {
//					Submarine submarine = enemyShipsMap.get(id);
//					submarine.updatePosition(e.position.x, e.position.y, e.angle, e.velocity);
//				}
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
