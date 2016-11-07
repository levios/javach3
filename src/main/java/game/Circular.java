package game;

public class Circular {
	public XVector position;
	public double r;

	/**
	 * Gets the Radius of the circular object
	 *
	 * @return Radius of the circular object
	 */
	public double r() {
		return this.r;
	}

	/**
	 * Y coordinate of the Circular object's center
	 *
	 * @return Y coordinate of the Circular object's center
	 */
	public double y() {
		return this.position.getY();
	}

	/**
	 * X coordinate of the Circular object's center
	 *
	 * @return X coordinate of the Circular object's center
	 */
	public double x() {
		return this.position.getX();
	}

	/**
	 * Checks to see if overlaps with the other circular object
	 *
	 * @param other target of the overlap check
	 * @return true if there is an intersection between the two objects
	 */
	public boolean checkIntersection(Circular other) {
		double distance = this.position.distance(other.position);
		return (distance < (this.r + other.r));
	}

	/**
	 * Moves the object to the given coordinates
	 *
	 * @param X target X coordinate
	 * @param Y target Y coordinate
	 */
	public void moveTo(double X, double Y) {
		this.position = new XVector(X,Y);
	}

	/**
	 *
	 * @param delta
	 */
	public void move(XVector delta) {
		this.position = this.position.add(delta);
	}

	public Circular(double x, double y, double r) {
		this.position = new XVector(x, y);
		this.r = r;
	}
}
