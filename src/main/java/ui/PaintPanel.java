package ui;

import game.Circular;
import game.GameSession;
import game.Submarine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

class PaintPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Color DARK_GREEN = new Color(0, 51, 0);

	private static volatile GameSession session = null;

	public PaintPanel() {
		setBackground(Color.ORANGE);
		this.setFocusable(true);
		this.grabFocus();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D drawImage = (Graphics2D) g;

		g.translate(10, 10);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 14));

		if (session != null) {
			// Draw islands
			drawImage.setColor(Color.RED);
			session.map.islands.forEach(island -> drawImage.fillOval(
					(int) (island.x() - island.r()),
					(int) (island.y() - island.r()), (int) (island.r() * 2),
					(int) (island.r() * 2)));

			// Draw ships
			drawImage.setColor(Color.BLUE);
			session.myShips.forEach(ship -> drawImage.fillOval(
					(int) (ship.position.x - session.submarineSize),
					(int) (ship.position.y - session.submarineSize),
					(int) (session.submarineSize * 2),
					(int) (session.submarineSize * 2)));
		}
	}

	public void refresh(GameSession session) {
		PaintPanel.session = session;
		repaint();
	}
}