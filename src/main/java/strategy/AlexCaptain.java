package strategy;

import game.*;

import java.util.List;

public class AlexCaptain extends Captain {


	public AlexCaptain() {
	}

	@Override
	public void executeStrategy(GameMap map, List<Submarine> myShips) {
		Submarine submarine = myShips.get(0);

		log.info("SHELLSHOCK INC.");
		submarine.actionQueue.add(Action.move(2,
				5
		));
		if (submarine.canShootTorpedo())
			submarine.actionQueue.add(Action.shoot(33));
		if (submarine.canUseExtendedSonar())
			submarine.actionQueue.add(Action.activateSonar());
	}

}
