package game;

public class ProjectileLike extends Circular {

	public double rotation;
	public double speed;

	public ProjectileLike(double x, double y, double r, double speed, double rotation) {
		super(x, y, r);
		this.rotation = rotation;
		this.speed = speed;
	}

	public void step() {
		this.step(1);
	}

	public void step(int steps) {
		double r = Math.toRadians(this.rotation);
		XVector step = new XVector(Math.cos(r), Math.sin(r)).scale(this.speed);
		for (int i = 0; i < steps; i++) {
			this.position = this.position.add(step);
		}
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
		this.position = new XVector(x, y);
	}

	@Override
	public ProjectileLike clone(){
		return new ProjectileLike(this.x(), this.y(), this.r(), this.speed, this.rotation);
	}
}
