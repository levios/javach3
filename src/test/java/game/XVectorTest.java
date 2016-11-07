package game;

import static org.junit.Assert.*;

import org.junit.*;

public class XVectorTest {
	
	@Test
	public void angleTest1(){
		/**
		 * 			/\
		 * 		 \	 |
		 * 		  \  |
		 * 		   \ |
		 * --------- +--------
		 */
		XVector vec = new XVector(-5.0, 5.0);
		double res = vec.getAngleInDegrees();
		assertEquals("should return 90 + 45", 135.0, res, 0.01);
	}
	
	@Test
	public void angleTest2(){
		/**
		 * 	/\
		 * 	|   /
		 * 	|  /
		 * 	| /
		 * -+--------
		 */
		XVector vec = new XVector(5.0, 5.0);
		double res = vec.getAngleInDegrees();
		assertEquals("should return 45", 45.0, res, 0.01);
	}
	
	@Test
	public void angleTest3(){		
		XVector vec = new XVector(-5.0, -5.0);
		double res = vec.getAngleInDegrees();
		assertEquals("should return 180 + 45", 225.0, res, 0.01);
	}

	@Test
	public void angleTest4(){
		XVector vec = new XVector(5.0, -5.0);
		double res = vec.getAngleInDegrees();
		assertEquals("should return 270 + 45", 315.0, res, 0.01);
	}
	
}
