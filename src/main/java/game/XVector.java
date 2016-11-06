package game;

public class XVector {

	public double x;
	public double y;

	public XVector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public XVector add(XVector other) {
		return new XVector(this.x + other.x, this.y + other.y);
	}

	public XVector addInPlace(XVector other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}

	public XVector scale(double scalar) {
		return new XVector(this.x * scalar, this.y * scalar);
	}

	public XVector scaleInPlace(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}

	public XVector negate() {
		return this.scale(-1);
	}

	public double getMagnitude() {
		return Math.sqrt(x * x + y * y);
	}

	public XVector rotate(double rotation) {
		double newX = 0;
		double newY = 0;
		return new XVector(newX, newY);
	}

	public double distanceFrom(XVector other){
		return this.add(other.negate()).getMagnitude();
	}

	public static XVector unit(double rotation) {
		double radRotation = Math.toRadians(rotation);
		return new XVector(Math.cos(radRotation), Math.sin(radRotation));
	}

}
