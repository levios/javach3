package strategy;

import game.*;

import java.util.ArrayList;
import java.util.List;

public class AlexCaptain extends Captain {

	private double shellshockcountdown;
	private int otherCountdown;

	public AlexCaptain() {
		shellshockcountdown = 0.0;
		otherCountdown = 0;
	}

	@Override
	public void executeStrategy(GameMap map, List<Submarine> myShips) {
		Submarine submarine = myShips.get(0);

		log.info("SHELLSHOCK INC.");
		submarine.actionQueue.add(Action.move(shellshockcountdown,
				5
		));
		if (submarine.canShootTorpedo())
			submarine.actionQueue.add(Action.shoot(33));
		if (submarine.isSonarReady())
			submarine.actionQueue.add(Action.activateSonar());
	}

}
