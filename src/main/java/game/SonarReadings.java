package game;

import model.Entity;

import java.util.List;

/**
 * Created by alexszabo on 05/11/16.
 */
public class SonarReadings {
	public final List<Entity> entities;
	public final Integer range;
	public final Vector position;

	public SonarReadings(List<Entity> entities, Vector position, Integer sonarRange) {
		this.entities = entities;
		this.range = sonarRange;
		this.position = position;
	}
}
