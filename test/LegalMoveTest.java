
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import airplane.sim.Plane;


public class LegalMoveTest {

	private Plane p;
	
	@Before
	public void setUp() throws Exception {
		p = new Plane(60, 60, 80, 80, 0);
	}
	
	@Test
	public void testDueNorth() {
		assertTrue(p.isLegalMove(0));
	}

	@Test
	public void testDueEast() {
		assertTrue(p.isLegalMove(90));
	}
	
	@Test
	public void testDueSouth() {
		assertTrue(p.isLegalMove(180));
	}
	
	@Test
	public void testDueWest() {
		assertTrue(p.isLegalMove(270));
	}
	
	@Test
	public void testStayOnGround() {
		p.setBearing(-1);
		assertTrue(p.isLegalMove(-1));
	}
	
	@Test
	public void testGoBackToWaiting() {
		p.setBearing(10);
		assertFalse(p.isLegalMove(-1));
	}

	@Test
	public void testStayLanded() {
		p.setBearing(-2);
		assertTrue(p.isLegalMove(-2));
	}

	@Test
	public void testLand() {
		p.setBearing(50);
		// YOU don't land; the simulator lands you
		assertFalse(p.isLegalMove(-2));
	}

	@Test
	public void testNegative() {
		assertFalse(p.isLegalMove(-3));
	}
	
	@Test
	public void testTooBig() {
		assertFalse(p.isLegalMove(361));
	}
	
	@Test
	public void testIncreaseBearingOk() {
		p.setBearing(50);
		assertTrue(p.isLegalMove(60));
	}

	@Test
	public void testDecreaseBearingOk() {
		p.setBearing(50);
		assertTrue(p.isLegalMove(40));
	}

	@Test
	public void testIncreaseBearingNotOk() {
		p.setBearing(50);
		assertFalse(p.isLegalMove(60.1));
	}

	@Test
	public void testDecreaseBearingNotOk() {
		p.setBearing(50);
		assertFalse(p.isLegalMove(39.9));
	}
	
	@Test
	public void testSmallBearingIncreaseOk() {
		p.setBearing(1);
		assertTrue(p.isLegalMove(10));
	}

	@Test
	public void testSmallBearingIncreaseNotOk() {
		p.setBearing(1);
		assertFalse(p.isLegalMove(11.1));
	}
	
	@Test
	public void testSmallBearingDecreaseOk() {
		p.setBearing(5);
		assertTrue(p.isLegalMove(1));
	}

	@Test
	public void testSmallBearingDecreaseToZeroOk() {
		p.setBearing(5);
		assertTrue(p.isLegalMove(0));
	}

	@Test
	public void testSmallBearingDecreaseTo360Ok() {
		p.setBearing(5);
		assertTrue(p.isLegalMove(360));
	}
	
	@Test
	public void testSmallBearingDecreaseCrossBorderOk() {
		p.setBearing(1);
		assertTrue(p.isLegalMove(351));
	}

	@Test
	public void testSmallBearingDecreaseCrossBorderNotOk() {
		p.setBearing(1);
		assertFalse(p.isLegalMove(350));
	}
	
	@Test
	public void testLargeBearingDecreaseOk() {
		p.setBearing(355);
		assertTrue(p.isLegalMove(345));
	}

	@Test
	public void testLargeBearingDecreaseNotOk() {
		p.setBearing(355);
		assertFalse(p.isLegalMove(344.5));
	}
	
	@Test
	public void testLargeBearingIncreaseOk() {
		p.setBearing(355);
		assertTrue(p.isLegalMove(359));
	}

	@Test
	public void testLargeBearingIncreaseToZeroOk() {
		p.setBearing(355);
		assertTrue(p.isLegalMove(0));
	}

	@Test
	public void testLargeBearingIncreaseTo360Ok() {
		p.setBearing(355);
		assertTrue(p.isLegalMove(360));
	}
	
	@Test
	public void testLargeBearingIncreaseCrossBorderOk() {
		p.setBearing(355);
		assertTrue(p.isLegalMove(4));
	}

	@Test
	public void testLargeBearingIncreaseCrossBorderNotOk() {
		p.setBearing(355);
		assertFalse(p.isLegalMove(7));
	}


}
