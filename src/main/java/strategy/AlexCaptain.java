package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

public class AlexCaptain extends Captain {

	private int shellshockcountdown;

	public AlexCaptain() {
		shellshockcountdown = 1;
	}

	@Override
	public void executeStrategy(GameMap map, List<Submarine> myShips) {
		Submarine submarine = myShips.get(0);

		if (shellshockcountdown-- == 0) {
			log.info("SHELLSHOCK INC.");
			submarine.actionQueue.add(Action.move(0.01, 1.01));
			if (submarine.canShootTorpedo())
				submarine.actionQueue.add(Action.shoot(33));
//			if (submarine.isSonarReady())
//				submarine.actionQueue.add(Action.activateSonar());
		}
	}

}
