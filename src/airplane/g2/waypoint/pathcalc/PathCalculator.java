package airplane.g2.waypoint.pathcalc;

import java.util.HashMap;

import airplane.g2.waypoint.PlanePath;
import airplane.sim.Plane;

public abstract class PathCalculator {
	public abstract void calculatePaths(HashMap<Plane, PlanePath> waypointHash);
}
