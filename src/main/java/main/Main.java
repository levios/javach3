package main;

import game.*;

import java.awt.EventQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ui.MainPaint;

public class Main {
	static Logger log = LoggerFactory.getLogger(Main.class);
	public static MainPaint GUI;
	
	public static boolean hasGui = true;

	public static void main(String[] args) {
		try {
			
			String server = args[0];
			
			Thread game = new GameThread(false, hasGui, server);
			game.run();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MainPaint startUI(int x, int y){
		if(hasGui) {
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
