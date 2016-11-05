package ui;

import game.Circular;
import game.GameSession;
import game.Submarine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;

import javax.swing.JPanel;

import model.MapConfiguration;

class PaintPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Color DARK_GREEN = new Color(0, 51, 0);

	private static volatile GameSession session = null;

	private final int x;
	private final int y;
	private final int fontHeightInPixel = 28;
	private final int marginFromXEndInPixels = 220;
	
	public PaintPanel(int x, int y) {
		this.x = x;
		this.y = y;
		setBackground(Color.ORANGE);
		this.setFocusable(true);
		this.grabFocus();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D drawImage = (Graphics2D) g;

//		g.translate(10, 10);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		
		
		// Segedvonalak
		for (int i = 0; i <= x; i += 100) {
			drawImage.setColor(Color.LIGHT_GRAY);
			drawImage.drawLine(i, 0, i, y);
			drawImage.setColor(Color.DARK_GRAY);
			drawImage.drawString("" + i, i, y);
		}
		for (int i = 0; i <= y; i += 100) {
			drawImage.setColor(Color.LIGHT_GRAY);
			drawImage.drawLine(0, i, x, i);
			drawImage.setColor(Color.DARK_GRAY);
			if(i != y)
				drawImage.drawString("" + (y - i), 0, i + 14);
		}

		if (session != null) {
			// Draw islands
			drawImage.setColor(Color.RED);
			session.map.islands.forEach(island -> drawImage.fillOval(
					(int) (island.x() - island.r()),
					(int)(y - island.y() - island.r()), 
					(int) (island.r() * 2),
					(int) (island.r() * 2)));

			// Draw ships
			drawImage.setColor(Color.BLUE);
			session.myShips.forEach(ship -> drawImage.fillOval(
					(int) (ship.position.x - session.submarineSize),
					(int)(y - ship.position.y - session.submarineSize),
					(int) (session.submarineSize * 2),
					(int) (session.submarineSize * 2)));
			
			// Draw Statistics
			drawStatistics(drawImage);
		}
	}
	
	void drawStatistics(Graphics2D drawImage){
		drawImage.setColor(Color.BLACK);
		drawImage.drawString("round: " + session.gameInfo.round, x - marginFromXEndInPixels, (fontHeightInPixel * 1));
		drawImage.drawString("roundLength: " + session.gameInfo.mapConfiguration.roundLength, x - marginFromXEndInPixels, (fontHeightInPixel * 2));
		drawImage.drawString("rounds: " + session.gameInfo.mapConfiguration.rounds, x - marginFromXEndInPixels, (fontHeightInPixel * 3));

		drawImage.drawString("myScore: " + session.gameInfo.scores.scores.myScore, x - marginFromXEndInPixels, (fontHeightInPixel * 4));
		drawImage.drawString("teamCount: " + session.gameInfo.mapConfiguration.teamCount, x - marginFromXEndInPixels, (fontHeightInPixel * 5));
		drawImage.drawString("---------- TORPEDO --------", x - marginFromXEndInPixels, (fontHeightInPixel * 6));
		drawImage.drawString("torpedoDamage: " + session.gameInfo.mapConfiguration.torpedoDamage, x - marginFromXEndInPixels, (fontHeightInPixel * 7));
		drawImage.drawString("torpedoHitScore: " + session.gameInfo.mapConfiguration.torpedoHitScore, x - marginFromXEndInPixels, (fontHeightInPixel * 8));
		drawImage.drawString("torpedoHitPenalty: " + session.gameInfo.mapConfiguration.torpedoHitPenalty, x - marginFromXEndInPixels, (fontHeightInPixel * 9));
		drawImage.drawString("torpedoCooldown: " + session.gameInfo.mapConfiguration.torpedoCooldown, x - marginFromXEndInPixels, (fontHeightInPixel * 10));
		drawImage.drawString("torpedoSpeed: " + session.gameInfo.mapConfiguration.torpedoSpeed, x - marginFromXEndInPixels, (fontHeightInPixel * 11));
		drawImage.drawString("torpedoExplosionRadius: " + session.gameInfo.mapConfiguration.torpedoExplosionRadius, x - marginFromXEndInPixels, (fontHeightInPixel * 12));
		drawImage.drawString("---------- SONAR ----------", x - marginFromXEndInPixels, (fontHeightInPixel * 13));
		drawImage.drawString("sonarRange: " + session.gameInfo.mapConfiguration.sonarRange, x - marginFromXEndInPixels, (fontHeightInPixel * 14));
		drawImage.drawString("extendedSonarRange: " + session.gameInfo.mapConfiguration.extendedSonarRange, x - marginFromXEndInPixels, (fontHeightInPixel * 15));
		drawImage.drawString("extendedSonarRounds: " + session.gameInfo.mapConfiguration.extendedSonarRounds, x - marginFromXEndInPixels, (fontHeightInPixel * 16));
		drawImage.drawString("extendedSonarCooldown: " + session.gameInfo.mapConfiguration.extendedSonarCooldown, x - marginFromXEndInPixels, (fontHeightInPixel * 17));
	}

	public void refresh(GameSession session) {
		PaintPanel.session = session;
		repaint();
	}
}