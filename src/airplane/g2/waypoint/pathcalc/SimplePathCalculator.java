package airplane.g2.waypoint.pathcalc;

import airplane.g2.waypoint.avoidance.*;
import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;

import java.awt.geom.Point2D;
import java.util.HashMap;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.sim.Plane;

public class SimplePathCalculator extends PathCalculator{

	//TODO Better method of adding waypoint.
	@Override
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		int i = 1;
		for (PlanePath path1 : waypointHash.values()) {
			for(PlanePath path2 : waypointHash.values()) {
				Plane p1 = path1.getPlane();
				Plane p2 = path2.getPlane();
				PlaneCollision collision = collidePlanePaths(path1, path2);
				if (p1.id != p2.id && collision != null) {
					PlanePath[] newPaths = planePathsDidCollide(collision);
					//TODO set new paths
				}
			}
			
		}	
	}
	
	public int slowestArrivalStep(PlanePath[] paths) {
		//TODO
		return 0;
	}
	
	public PlanePath[] planePathsDidCollide(PlaneCollision collision) {
		AvoidMethod[] avoidances = getAvoidMethods();
		int slowest = 99;
		AvoidMethod bestAvoidMethod = null;
		PlanePath[] bestPaths = null;
		
		for(AvoidMethod avoid: avoidances) {
			PlanePath[] pathsForAvoidMethod = avoid.avoid(collision.getPath1(), collision.getPath2(), collision);
			int stepForAvoidMethod = slowestArrivalStep(pathsForAvoidMethod);
			//TODO make sure they don't collide anymore
			if(stepForAvoidMethod < slowest) {
				bestAvoidMethod = avoid;
				slowest = stepForAvoidMethod;
				bestPaths = pathsForAvoidMethod;
			}
		}
		return bestPaths;
	}
	
	public PlaneCollision collidePlanePaths(PlanePath a, PlanePath b) {
		return a.getPlaneCollision(b);
	}
	
	public Boolean doesCollide(PlanePath a, PlanePath b) {
		return collidePlanePaths(a, b) != null;
	}
	
	public AvoidMethod[] getAvoidMethods() {
		return new AvoidMethod[] {
				new AvoidByDelay(PlaneIndex.PLANE_ONE, 5),
				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(5, 5)),
				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(-5, -5))
		};
	}
	

}
