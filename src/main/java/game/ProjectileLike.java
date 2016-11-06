package game;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class ProjectileLike extends Circular {

	public double rotation;
	public double speed;

	public ProjectileLike(double x, double y, double r, double speed, double rotation) {
		super(x, y, r);
		this.rotation = rotation;
		this.speed = speed;
	}

	public void step() {
		double r = Math.toRadians(this.rotation);
		this.position = this.position.add(new Vector2D(Math.cos(r), Math.sin(r)).scalarMultiply(this.speed));
	}

	public void accelerate(double deltaSpeed) {
		this.speed += deltaSpeed;
	}

	public void steer(double deltaRot) {
		this.rotation += deltaRot;
	}

	public void updatePosition(double x, double y, double angle, double speed) {
		this.rotation = angle;
		this.speed = speed;
		this.position = new Vector2D(x, y);
	}
}
