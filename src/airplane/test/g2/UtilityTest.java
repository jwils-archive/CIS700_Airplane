package airplane.test.g2;

import static org.junit.Assert.*;

import java.util.ArrayList;

import airplane.g2.util.*;
import airplane.sim.Plane;

import org.junit.Before;
import org.junit.Test;

public class UtilityTest {

	protected ArrayList<Plane> planes;
	
	@Before
	public void setUp() throws Exception {
		planes = new ArrayList<Plane>();
		planes.add(new Plane(1, 1, 1, 2, 0)); // distance to destination 1
		planes.add(new Plane(2, 1, 2, 4, 0)); // distance to destination 3
		planes.add(new Plane(4, 1, 2, 4, 0)); 
	}

	@Test
	public void testDistancegrid() {
		double[][] expectedDistances = new double[][] {
				{0, 1, 3},
				{1, 0, 2},
				{3, 2, 0}
		};
		double[][] distances = PlaneUtil.getDistances(planes);
		
		assertArrayEquals(expectedDistances, distances);
	}

	@Test
	public void testBearingAway() {
		assertEquals(160, PlaneUtil.bearingAway(180, 170, 10), 0.01);
		double ambiguousBearing = PlaneUtil.bearingAway(180, 180, 10);
		assertTrue(ambiguousBearing == 190 || ambiguousBearing == 170);
		assertEquals(350, PlaneUtil.bearingAway(90, 0, 10), 0.01);
	}
}
