
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import airplane.sim.Plane;


public class MoveTest {
	
	private Plane p;

	@Before
	public void setUp() throws Exception {
		p = new Plane(60, 60, 80, 80, 0);
	}
	
	@Test
	public void testMoveNorth() {
		assertTrue(p.move(0));
		assertEquals(0, p.getBearing(), 0.01);
		assertEquals(60, p.getX(), 0.01);
		assertEquals(59, p.getY(), 0.01);
	}

	@Test
	public void testMoveEast() {
		assertTrue(p.move(90));
		assertEquals(90, p.getBearing(), 0.01);
		assertEquals(61, p.getX(), 0.01);
		assertEquals(60, p.getY(), 0.01);
	}
	
	@Test
	public void testMoveSouth() {
		assertTrue(p.move(180));
		assertEquals(180, p.getBearing(), 0.01);
		assertEquals(60, p.getX(), 0.01);
		assertEquals(61, p.getY(), 0.01);
	}
	
	@Test
	public void testMoveWest() {
		assertTrue(p.move(270));
		assertEquals(270, p.getBearing(), 0.01);
		assertEquals(59, p.getX(), 0.01);
		assertEquals(60, p.getY(), 0.01);
	}
	
	@Test
	public void testMoveNorthEast() {
		assertTrue(p.move(45));
		assertEquals(45, p.getBearing(), 0.01);
		double delta = Math.cos(45 * Math.PI / 180);
		assertEquals(60 + delta, p.getX(), 0.01);
		assertEquals(60 - delta, p.getY(), 0.01);
	}

	@Test
	public void testMoveOutOfBounds() {
		p.setX(99.5);
		assertFalse(p.move(90));
		
		p.setX(0.5);
		assertFalse(p.move(270));
		
		p.setY(99.5);
		assertFalse(p.move(180));
		
		p.setY(0.5);
		assertFalse(p.move(0));
	}

}
