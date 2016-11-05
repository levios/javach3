package main;

import java.awt.EventQueue;

import ui.MainPaint;
import game.GameThread;
import connection.*;

public class Main {
	
	public static MainPaint GUI;
	
	public static boolean gui = true;

	public static void main(String[] args) {
		try {
			
			if(gui) {
		        EventQueue.invokeLater(new Runnable() {
		            
		            @Override
		            public void run() {
		            	GUI = new MainPaint();
		            	GUI.setVisible(true);
		            }
		        });
			}
			
			String server = args[0];
			
			Thread game = new GameThread(false, gui, GUI, server);
			game.run();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
