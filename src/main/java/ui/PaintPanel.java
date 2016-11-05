package ui;

import game.GameSession;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.Objects;

import javax.swing.JPanel;

class PaintPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Color ISLAND_SAND = new Color(145, 141, 77);
	private final Color OCEAN_BLUE = new Color(119, 207, 255);
	private final Color HORNPUB_HEAT = new Color(205, 27, 17);
	private final Color SONAR_SEAWEED = new Color(65, 255, 70, 62);
	private final Color GRASS_GREEN = new Color(143, 255, 40, 206);

	private static volatile GameSession session = null;

	private final int x;
	private final int y;
	private final int fontHeightInPixel = 28;
	private final int marginFromXEndInPixels = 250;
	private long paintStartTime;

	public PaintPanel(int x, int y) {
		this.x = x;
		this.y = y;
		setBackground(Color.DARK_GRAY);
		this.setFocusable(true);
		this.grabFocus();

	}

	@Override
	public void paintComponent(Graphics g) {
		this.paintStartTime = System.currentTimeMillis();
		super.paintComponent(g);
		Graphics2D drawImage = (Graphics2D) g;

		double scaler = (double) this.getWidth() / (double) x;
		drawImage.scale(scaler, scaler);

		g.setFont(new Font("TimesRoman", Font.PLAIN, 14));

		drawImage.setColor(OCEAN_BLUE);
		drawImage.fillRect(0, 0, x, y);

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
			if (i != y)
				drawImage.drawString("" + (y - i), 0, i + 14);
		}

		if (session != null) {

			// Draw islands
			drawImage.setColor(ISLAND_SAND);
			session.map.islands.forEach(island -> {
				drawImage.translate(island.x(), y - island.y());
				drawImage.fillOval(
						(int) (-island.r()),
						(int) (-island.r()),
						(int) (island.r() * 2),
						(int) (island.r() * 2));
				drawImage.translate(-island.x(), - (y-island.y()));
			});

			// Draw ships
			session.myShips.forEach(ship -> {
				drawImage.translate(ship.position.x, y - ship.position.y);
				drawImage.rotate(Math.toRadians(90 - ship.rotation));

				drawImage.setColor(SONAR_SEAWEED);
				drawImage.fillOval(
						(-session.mapConfiguration.sonarRange),
						(-session.mapConfiguration.sonarRange),
						(session.mapConfiguration.sonarRange * 2),
						(session.mapConfiguration.sonarRange * 2));

				drawImage.setColor(HORNPUB_HEAT);
				drawImage.fillOval(
						(int) (-ship.r),
						(int) (-ship.r),
						(int) (ship.r * 2),
						(int) (ship.r * 2));
				drawImage.setColor(GRASS_GREEN);
				drawImage.fillPolygon(makeTriangleX(ship.r), makeTriangleY(ship.r), 3);
				drawImage.rotate(Math.toRadians(-(90 - ship.rotation)));
				drawImage.translate(-ship.position.x, -(y - ship.position.y));
			});

//			// Draw enemy ships
//			session.map.ships.stream().filter(x-> !Objects.equals(x.owner, "HornPub")).forEach(ship->{
//				int red = (ship.owner.hashCode() * 12345) % 255;
//				int green = (ship.owner.hashCode() * 77777) % 255;
//				int blue = (ship.owner.hashCode() * 7654321) % 255;
//				Color enemyColor = new Color(red,green, blue);
//
//				drawImage.translate(ship.position.x, y - ship.position.y);
//				drawImage.rotate(Math.toRadians(90 - ship.rotation));
//
//				drawImage.setColor(enemyColor);
//				drawImage.drawOval(
//						(-session.mapConfiguration.sonarRange),
//						(-session.mapConfiguration.sonarRange),
//						(session.mapConfiguration.sonarRange * 2),
//						(session.mapConfiguration.sonarRange * 2));
//
//				drawImage.setColor(HORNPUB_HEAT);
//				drawImage.drawOval(
//						(int) (-ship.r),
//						(int) (-ship.r),
//						(int) (ship.r * 2),
//						(int) (ship.r * 2));
//
//				drawImage.setColor(GRASS_GREEN);
//				drawImage.fillPolygon(makeTriangleX(ship.r), makeTriangleY(ship.r), 3);
//				drawImage.rotate(Math.toRadians(-(90 - ship.rotation)));
//				drawImage.translate(-ship.position.x, -(y - ship.position.y));
//			});

			// Draw Statistics
			drawStatistics(drawImage);
		}
	}

	private int[] makeTriangleY(double r) {
		return new int[]{
				0, 0, (int) -r
		};
	}

	private int[] makeTriangleX(double r) {
		return new int[]{
				(int) -r, (int) r, 0
		};
	}

	void drawStatistics(Graphics2D drawImage) {
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
		drawImage.drawString("----------------------------", x - marginFromXEndInPixels, (fontHeightInPixel * 18));
		drawImage.drawString("- TOTAL THINK: ?", x - marginFromXEndInPixels, (fontHeightInPixel * 19));
		drawImage.drawString("- TOTAL DRAW: " + (System.currentTimeMillis() - this.paintStartTime) + "ms", x - marginFromXEndInPixels, (fontHeightInPixel * 20));
	}

	public void refresh(GameSession session) {
		PaintPanel.session = session;
//		repaint();
		this.invalidate();
	}
}