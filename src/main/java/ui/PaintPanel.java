package ui;

import game.GameSession;
import game.Submarine;

import java.awt.*;
import java.util.List;

import javax.swing.JPanel;

class PaintPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Color ISLAND_SAND = new Color(145, 141, 77);
	private final Color OCEAN_BLUE = new Color(119, 207, 255);
	private final Color HORNPUB_HEAT = new Color(205, 27, 17);
	private final Color SONAR_SEAWEED = new Color(65, 255, 70, 62);
	private final Color GRASS_GREEN = new Color(143, 255, 40, 206);
	private final Color FOS_LILA = new Color(153, 0, 153);
	private final Color LILA_TORPEDO_EXPLOSION_RADIUS = new Color(238, 139, 205, 100);
	
	private final Color ENEMY_SHIP = new Color(240, 217, 65);

	private static volatile GameSession session = null;

	private final int width;
	private final int height;
	private final int fontHeightInPixel = 28;
	private final int marginFromXEndInPixels = 250;
	private long paintStartTime;
	private int lineOffset = 0;
	private Graphics2D imageForStatistics;

	public PaintPanel(int x, int y) {
		this.width = x;
		this.height = y;
		setBackground(Color.DARK_GRAY);
		this.setFocusable(true);
		this.grabFocus();

	}

	@Override
	public void paintComponent(Graphics g) {
		this.paintStartTime = System.currentTimeMillis();
		super.paintComponent(g);
		Graphics2D drawImage = (Graphics2D) g;

		double scaler = (double) this.getWidth() / (double) width;
		drawImage.scale(scaler, scaler);

		g.setFont(new Font("TimesRoman", Font.PLAIN, 14));

		drawImage.setColor(OCEAN_BLUE);
		drawImage.fillRect(0, 0, width, height);

		// Segedvonalak
		for (int i = 0; i <= width; i += 100) {
			drawImage.setColor(Color.LIGHT_GRAY);
			drawImage.drawLine(i, 0, i, height);
			drawImage.setColor(Color.DARK_GRAY);
			drawImage.drawString("" + i, i, height);
		}
		for (int i = 0; i <= height; i += 100) {
			drawImage.setColor(Color.LIGHT_GRAY);
			drawImage.drawLine(0, i, width, i);
			drawImage.setColor(Color.DARK_GRAY);
			if (i != height)
				drawImage.drawString("" + (height - i), 0, i + 14);
		}

		if (session != null) {

			// Draw islands
			drawImage.setColor(ISLAND_SAND);
			session.map.islands.forEach(island -> {
				drawImage.translate(island.x(), height - island.y());
				drawImage.fillOval(
						(int) (-island.r()),
						(int) (-island.r()),
						(int) (island.r() * 2),
						(int) (island.r() * 2));
				drawImage.translate(-island.x(), -(height - island.y()));
			});

			// Draw ships
			session.myShips.forEach(ship -> {
				drawImage.translate(ship.position.getX(), height - ship.position.getY());
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
				drawImage.translate(-ship.position.getX(), -(height - ship.position.getY()));
			});

			//draw line for ship's next position
			drawImage.setColor(Color.BLACK);
			session.myShips
					.stream()
					.filter(ship -> !ship.nextPositions.isEmpty())
					.forEach(
							ship -> drawImage.drawLine((int) ship.position.getX(),
									(int) (height - ship.position.getY()),
									(int) ship.nextPositions.get(0).getX(),
									(int) (height - ship.nextPositions.get(0).getY())));

			// Draw Torpedos 
			session.map.torpedos.forEach(torpedo -> {
				double x = torpedo.position.getX();
				double y = torpedo.position.getY();
				drawImage.translate(x, height - y);
				
				drawImage.setColor(LILA_TORPEDO_EXPLOSION_RADIUS);
				drawImage.fillOval(
						(-session.mapConfiguration.torpedoExplosionRadius),
						(-session.mapConfiguration.torpedoExplosionRadius),
						(session.mapConfiguration.torpedoExplosionRadius * 2),
						(session.mapConfiguration.torpedoExplosionRadius * 2));
				
				drawImage.setColor(FOS_LILA);
				drawImage.fillOval(
						-5,
						-5,
						10,
						10);
				drawImage.translate(-x, -(height - y));
			});

			session.map.enemyShips.forEach(ship -> {
				drawImage.translate(ship.position.getX(), height - ship.position.getY());
				drawImage.rotate(Math.toRadians(90 - ship.rotation));

				drawImage.setColor(ENEMY_SHIP);
				drawImage.fillOval(
						(int) (-ship.r),
						(int) (-ship.r),
						(int) (ship.r * 2),
						(int) (ship.r * 2));
				drawImage.setColor(GRASS_GREEN);
				drawImage.fillPolygon(makeTriangleX(ship.r), makeTriangleY(ship.r), 3);
				drawImage.rotate(Math.toRadians(-(90 - ship.rotation)));
				drawImage.translate(-ship.position.getX(), -(height - ship.position.getY()));
			});

			// Draw Statistics
			drawStatistics(drawImage, session.myShips);
		}
	}

	private int[] makeTriangleY(double r) {
		return new int[]{
				0, 0, (int) -r
		};
	}

	private int[] makeTriangleX(double r) {
		return new int[]{
				(int) -r/2, (int) r/2, 0
		};
	}

	private void printLine(String str) {
		imageForStatistics.drawString(str, width - marginFromXEndInPixels, (fontHeightInPixel * ++lineOffset));
	}

	void drawStatistics(Graphics2D drawImage, List<Submarine> myShips) {
		drawImage.setColor(Color.BLACK);
		lineOffset = 0;
		imageForStatistics = drawImage;

		printLine("round: " + session.gameInfo.round);
		printLine("roundLength: " + session.gameInfo.mapConfiguration.roundLength);
		printLine("rounds: " + session.gameInfo.mapConfiguration.rounds);

		printLine("myScore: " + session.gameInfo.scores.scores.myScore);
		for (Submarine s : myShips) {
			printLine("--- Ship[" + s.id + "]----");
			printLine("HP: " + s.hp);
			printLine("TORPEDO: " + s.torpedoCooldown);
			printLine("SONAR: " + s.sonarCooldown);
		}
		printLine("---------- TORPEDO --------");
		printLine("torpedoDamage: " + session.gameInfo.mapConfiguration.torpedoDamage);
		printLine("torpedoHitScore: " + session.gameInfo.mapConfiguration.torpedoHitScore);
		printLine("torpedoHitPenalty: " + session.gameInfo.mapConfiguration.torpedoHitPenalty);
		printLine("torpedoCooldown: " + session.gameInfo.mapConfiguration.torpedoCooldown);
		printLine("torpedoSpeed: " + session.gameInfo.mapConfiguration.torpedoSpeed);
		printLine("torpedoExplosionRadius: " + session.gameInfo.mapConfiguration.torpedoExplosionRadius);
		printLine("---------- SONAR ----------");
		printLine("sonarRange: " + session.gameInfo.mapConfiguration.sonarRange);
		printLine("extendedSonarRange: " + session.gameInfo.mapConfiguration.extendedSonarRange);
		printLine("extendedSonarRounds: " + session.gameInfo.mapConfiguration.extendedSonarRounds);
		printLine("extendedSonarCooldown: " + session.gameInfo.mapConfiguration.extendedSonarCooldown);
		printLine("----------------------------");
		printLine("- TOTAL THINK: " + session.lastTurnLength + "ms");
		printLine("- TOTAL DRAW: " + (System.currentTimeMillis() - this.paintStartTime) + "ms");
	}

	public void refresh(GameSession session) {
		PaintPanel.session = session;
		this.invalidate();
	}
}