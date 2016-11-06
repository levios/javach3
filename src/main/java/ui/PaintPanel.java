package ui;

import game.GameSession;
import game.Submarine;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

class PaintPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Color ISLAND_SAND = new Color(145, 141, 77);
	private final Color OCEAN_BLUE = new Color(119, 207, 255);
	private final Color HORNPUB_HEAT = new Color(205, 27, 17);
	private final Color SONAR_SEAWEED = new Color(65, 255, 70, 62);
	private final Color GRASS_GREEN = new Color(143, 255, 40, 206);
	private final Color FOS_LILA = new Color(153, 0, 153);
	
	private final Color ENEMY_SHIP = new Color(240, 217, 65);

	private static volatile GameSession session = null;

	private final int width;
	private final int height;
	private final int fontHeightInPixel = 28;
	private final int marginFromXEndInPixels = 250;
	private long paintStartTime;

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
				drawImage.translate(-island.x(), - (height-island.y()));
			});

			// Draw ships
			session.myShips.forEach(ship -> {
				drawImage.translate(ship.position.x, height - ship.position.y);
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
				drawImage.translate(-ship.position.x, -(height - ship.position.y));
			});
			
			//draw ship's next position
			drawImage.setColor(Color.BLACK);
			session.myShips
					.stream()
					.filter(ship -> !ship.nextPositions.isEmpty())
					.forEach(
							ship -> drawImage.drawLine((int) ship.position.x,
									(int) (height-ship.position.y),
									(int) ship.nextPositions.get(0).getX(),
									(int) (height-ship.nextPositions.get(0).getY())));
			
			// Draw Torpedos 
			session.map.torpedos.stream().forEach(torpedo -> {
				double x = torpedo.position.x;
				double y = torpedo.position.y;
				drawImage.setColor(FOS_LILA);
				drawImage.fillPolygon(new int[]{(int) x, (int) (x-10), (int)x-10}, 
						new int[]{(int) (height -y), (int) (height - y-5), (int)(height - y + 5)}, 3);
			});
			
			session.map.enemyShips.forEach(ship -> {
				drawImage.translate(ship.position.x, height - ship.position.y);
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
				drawImage.translate(-ship.position.x, -(height - ship.position.y));
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
				(int) -r, (int) r, 0
		};
	}

	void drawStatistics(Graphics2D drawImage, List<Submarine> myShips) {
		drawImage.setColor(Color.BLACK);
		int i = 0;
		drawImage.drawString("round: " + session.gameInfo.round, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("roundLength: " + session.gameInfo.mapConfiguration.roundLength, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("rounds: " + session.gameInfo.mapConfiguration.rounds, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));

		drawImage.drawString("myScore: " + session.gameInfo.scores.scores.myScore, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		for(Submarine s : myShips){
			drawImage.drawString("Ship["+s.id+"] HP: " + s.hp, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		}
		drawImage.drawString("---------- TORPEDO --------", width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("torpedoDamage: " + session.gameInfo.mapConfiguration.torpedoDamage, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("torpedoHitScore: " + session.gameInfo.mapConfiguration.torpedoHitScore, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("torpedoHitPenalty: " + session.gameInfo.mapConfiguration.torpedoHitPenalty, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("torpedoCooldown: " + session.gameInfo.mapConfiguration.torpedoCooldown, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("torpedoSpeed: " + session.gameInfo.mapConfiguration.torpedoSpeed, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("torpedoExplosionRadius: " + session.gameInfo.mapConfiguration.torpedoExplosionRadius, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("---------- SONAR ----------", width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("sonarRange: " + session.gameInfo.mapConfiguration.sonarRange, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("extendedSonarRange: " + session.gameInfo.mapConfiguration.extendedSonarRange, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("extendedSonarRounds: " + session.gameInfo.mapConfiguration.extendedSonarRounds, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("extendedSonarCooldown: " + session.gameInfo.mapConfiguration.extendedSonarCooldown, width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("----------------------------", width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("- TOTAL THINK: ?", width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
		drawImage.drawString("- TOTAL DRAW: " + (System.currentTimeMillis() - this.paintStartTime) + "ms", width - marginFromXEndInPixels, (fontHeightInPixel * ++i));
	}

	public void refresh(GameSession session) {
		PaintPanel.session = session;
//		repaint();
		this.invalidate();
	}
}