package game;

public class ProjectileLike extends Circular {

	public double rotation;
	public double speed;

	public ProjectileLike(double x, double y, double r, double speed, double rotation){
		super(x, y, r);
		this.rotation = rotation;
		this.speed = speed;
	}
	
	public void step(){
		this.position.addInPlace(Vector.unit(this.rotation).scaleInPlace(this.speed));
	}
	
	public void accelerate(double deltaSpeed){
		this.speed+=deltaSpeed;
	}

	public void steer(double deltaRot){
		this.rotation += deltaRot;
	}
	
	public void updatePosition(double x, double y, double angle, double speed) {
		this.rotation = angle;
		this.speed = speed;
		this.position.x = x;
		this.position.y = y;
	}
}
