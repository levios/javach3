package game;

// FULLY IMMUTABLE
public class XVector {

	/**
	 * 90
	 * |
	 * 180--+--0
	 * |
	 * 270
	 */

	public double x;
	public double y;

	public XVector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public XVector add(XVector other) {
		return new XVector(this.x + other.x, this.y + other.y);
	}

	public static XVector add(XVector first, XVector other) {
		return new XVector(first.x + other.x, first.y + other.y);
	}

	public XVector scale(XVector vector, double scalar) {
		return new XVector(this.x * scalar, this.y * scalar);
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

	public double distance(XVector other) {
		return this.add(other.negate()).getMagnitude();
	}

	public XVector unit() {
		double m = this.getMagnitude();
		return new XVector(this.x / m, this.y / m);
	}

	public static XVector unit(double rotation) {
		double radRotation = Math.toRadians(rotation);
		return new XVector(Math.cos(radRotation), Math.sin(radRotation));
	}

	public XVector subtract(XVector other) {
		return new XVector(this.x - other.x, this.y - other.y);
	}

	public static XVector subtract(XVector v1, XVector v2) {
		return new XVector(v1.x - v2.x, v1.y - v2.y);
	}

	/**
	 * /\
	 * |   /
	 * |  /
	 * | /__
	 * |/alfa)
	 * +----------------
	 * Return the angle [alfa] of this vector
	 */
	public double getAngleInDegrees() {
		double angle = Math.atan2(this.y, this.x) * 180 / Math.PI;
		if (angle < 0.0) {
			//if angle is negative, we map it info current coordinate system.
			angle += 360.0;
		}
		return angle;
	}

	public XVector clone() {
		return new XVector(this.x, this.y);
	}

	@Override
	public String toString() {
		return "{ X: " + this.x + " , Y: " + this.y + " }";
	}
}
