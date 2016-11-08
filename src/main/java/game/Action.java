package game;

/**
 * Created by alexszabo on 06/11/16.
 */
public interface Action {

	final class MoveAction implements Action {
		public final double acceleration;
		public final double steering;

		MoveAction(double steering, double acceleration) {
			this.steering = steering;
			this.acceleration = acceleration;
		}

		@Override
		public String toString() {
			return "MoveAction { turn: " + this.steering + ", speed: " + this.acceleration + " }";
		}
	}

	final class ShootAction implements Action {
		public final double direction;

		ShootAction(double direction) {
			this.direction = direction;
		}

		@Override
		public String toString() {
			return "ShootAction { angle: " + this.direction + " }";
		}
	}

	final class SonarAction implements Action {
		@Override
		public String toString() {
			return "SonarAction { }";
		}
	}

	static MoveAction move(double steering, double acceleration) {
		return new MoveAction(steering, acceleration);
	}

	static ShootAction shoot(double direction) {
		return new ShootAction(direction);
	}

	static SonarAction activateSonar() {
		return new SonarAction();
	}
}
