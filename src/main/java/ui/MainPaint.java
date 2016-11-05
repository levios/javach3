package ui;

import game.GameSession;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class MainPaint extends JFrame {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	PaintPanel paintPan = null;

    public MainPaint() {
        setTitle("test paint");
        setSize(1700, 800);       
        
        paintPan = new PaintPanel();
        add(paintPan, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    
    public void refresh(GameSession session){
    	paintPan.refresh(session);
    	repaint();
    }
}