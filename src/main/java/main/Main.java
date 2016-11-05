package main;

import game.GameSession;
import game.LeviGameThread;

import java.awt.EventQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ui.MainPaint;

public class Main {
	static Logger log = LoggerFactory.getLogger(Main.class);
	public static MainPaint GUI;
	
	public static boolean gui = true;

	public static void main(String[] args) {
		try {
			
			String server = args[0];
			
			Thread game = new LeviGameThread(false, gui, server);
			game.run();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MainPaint startUI(int x, int y){
		if(gui) {
	        EventQueue.invokeLater(new Runnable() {
	            
	            @Override
	            public void run() {
	            	GUI = new MainPaint(x, y);
	            	GUI.setVisible(true);
	            }
	        });
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("GUI initialized");
		return GUI;
	}
}
