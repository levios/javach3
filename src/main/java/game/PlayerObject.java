package game;

/**
 * Created by alexszabo on 06/11/16.
 */
public class PlayerObject extends ProjectileLike {
	public final Long id;
	public final String owner;
	public final PlayerObjectType type;

	public PlayerObject(Long id, String owner, PlayerObjectType type, double x, double y, double r, double speed, double rotation) {
		super(x, y, r, speed, rotation);
		this.id = id;
		this.owner = owner;
		this.type = type;
	}
}
