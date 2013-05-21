package airplane.g2.util;

import java.util.ArrayList;

import airplane.g2.PlanePair;
import airplane.sim.Plane;

public class PlaneUtil {
	public static int stepsToDestination(Plane plane) {
		return (int)plane.getLocation().distance(plane.getDestination());
	}
	
	public static Plane getFirstToLand(Plane p1, Plane p2) {
		return null;
	}
	
	
	public static double[][] getDistances(ArrayList<Plane> planes) {
		return null;
	}
	
	public static ArrayList<PlanePair> detectCollisions(ArrayList<Plane> planes, int radius) {
		return null;
	}
	
	
}
