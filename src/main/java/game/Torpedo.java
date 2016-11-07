package game;

import model.MapConfiguration;

/**
 * Created by alexszabo on 05/11/16.
 */
public class Torpedo extends PlayerObject {

	private static Integer TORPEDO_DAMAGE;
	private static Integer TORPEDO_EXPLOSION;
	private static Integer TORPEDO_RANGE;
	private static Integer TORPEDO_SPEED;
	private final Integer explosionRange;
	private final Integer explosionDamage;
	private final double life;

	public static void setBounds(MapConfiguration rules){
		TORPEDO_DAMAGE = rules.torpedoDamage;
		TORPEDO_EXPLOSION = rules.torpedoExplosionRadius;
		TORPEDO_RANGE = rules.torpedoRange;
		TORPEDO_SPEED = rules.torpedoSpeed;
	}

	public Torpedo(Long id, String owner, double x, double y, double rotation) {
		super(id, owner, PlayerObjectType.TORPEDO, x, y, 0, TORPEDO_SPEED, rotation);

		this.explosionRange = TORPEDO_RANGE;
		this.explosionDamage = TORPEDO_DAMAGE;
		this.life = (double)TORPEDO_RANGE / (double)TORPEDO_SPEED;
	}
}
