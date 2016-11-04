package game;

public class Vector {

	public double x;
	public double y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector add(Vector other) {
		return new Vector(this.x + other.x, this.y + other.y);
	}

	public Vector addInPlace(Vector other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}

	public Vector scale(double scalar) {
		return new Vector(this.x * scalar, this.y * scalar);
	}

	public Vector scaleInPlace(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}

	public Vector negate() {
		return this.scale(-1);
	}

	public double getMagnitude() {
		return Math.sqrt(x * x + y * y);
	}

	public Vector rotate(double rotation) {
		double newX = 0;
		double newY = 0;
		return new Vector(newX, newY);
	}

	public static Vector unit(double rotation) {
		double radRotation = Math.toRadians(rotation);
		return new Vector(Math.cos(radRotation), Math.sin(radRotation));
	}
}
