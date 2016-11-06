package game;

import model.Entity;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

/**
 * Created by alexszabo on 05/11/16.
 */
public class SonarReadings {
	public final List<Entity> entities;
	public final Integer range;
	public final Vector2D position;

	public SonarReadings(List<Entity> entities, Vector2D position, Integer sonarRange) {
		this.entities = entities;
		this.range = sonarRange;
		this.position = position;
	}
}
