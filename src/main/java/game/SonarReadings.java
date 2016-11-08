package game;

import model.Entity;

import java.util.List;

/**
 * Created by alexszabo on 05/11/16.
 */
public class SonarReadings {
	public final List<Entity> entities;
	public final double range;
	public final XVector position;

	public SonarReadings(List<Entity> entities, XVector position, double sonarRange) {
		this.entities = entities;
		this.range = sonarRange;
		this.position = position;
	}
}
