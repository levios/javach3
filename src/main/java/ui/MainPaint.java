package ui;

import game.GameSession;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class MainPaint extends JFrame {
	private static final long serialVersionUID = 1L;
	
	PaintPanel paintPan = null;

    public MainPaint(int x, int y) {
        setTitle("test paint");
        setSize(x + 50, y + 80);       
        
        paintPan = new PaintPanel(x, y);
        add(paintPan, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    
    public void refresh(GameSession session){
    	paintPan.refresh(session);
    	repaint();
    }
}