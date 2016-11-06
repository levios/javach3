package game;

/**
 * Created by alexszabo on 06/11/16.
 */
public interface Action {

	public static class MoveAction implements Action {
		public final double acceleration;
		public final double steering;

		MoveAction(double steering, double acceleration){
			this.steering = steering;
			this.acceleration = acceleration;
		}
	}

	public static class ShootAction implements Action {
		public final double direction;

		ShootAction(double direction){
			this.direction = direction;
		}
	}

	public static MoveAction move(double steering, double acceleration){
		return new MoveAction(steering, acceleration);
	}

	public static ShootAction shoot(double direction){
		return new ShootAction(direction);
	}
}
