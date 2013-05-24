package airplane.g2.waypoint.pathcalc;

import airplane.g2.util.PlaneUtil;
import airplane.g2.waypoint.avoidance.*;
import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.sim.Plane;

public class SimplePathCalculator extends PathCalculator{

	//TODO Better method of adding waypoint.
	@Override
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		ArrayList<PlanePath> paths = new ArrayList<PlanePath>(waypointHash.values());
		
		for(int i = 0, count = paths.size(); i < count; i++) {
			for(int j = i + 1; j < count; j++) {
				putNewPathsIfPlanesAtIndicesCollide(waypointHash, paths, i, j);
			}
		}
	}
	
	protected void putNewPathsIfPlanesAtIndicesCollide(
			HashMap<Plane, PlanePath> waypointHash,
			ArrayList<PlanePath> paths,
			int i, int j) {
		PlanePath path1 = paths.get(i);
		PlanePath path2 = paths.get(j);
		
		if(pathPlanesAreEqual(path1, path2)) return;
		
		PlaneCollision collision = collidePlanePaths(path1, path2);
		if(collision == null) return;
		
		putPaths(waypointHash, planePathsDidCollide(collision));
	}
	
	protected void putPaths(HashMap<Plane, PlanePath> waypointHash, PlanePath[] paths) {
		for(PlanePath path: paths) {
			putPath(waypointHash, path);
		}
	}
	
	protected void putPath(HashMap<Plane, PlanePath> waypointHash, PlanePath path) {
		waypointHash.put(path.getPlane(), path);
	}
	
	protected Boolean pathPlanesAreEqual(PlanePath path1, PlanePath path2) {
		Plane p1 = path1.getPlane();
		Plane p2 = path2.getPlane();
		
		return PlaneUtil.planesAreEqual(p1, p2);
	}
	
	public int slowestArrivalStep(PlanePath[] paths) {
		Integer slowest = Integer.MIN_VALUE;
		for(PlanePath path: paths) {
			if(path.getArrivalStep() > slowest) {
				slowest = path.getArrivalStep();
			}
		}
		return slowest;
	}
	
	public PlanePath[] planePathsDidCollide(PlaneCollision collision) {
		AvoidMethod[] methods = getAvoidMethods();
		int slowest = Integer.MAX_VALUE;
		PlanePath[] bestPaths = null;
		
		for(AvoidMethod avoid: methods) {
			PlanePath[] pathsForAvoidMethod = avoid.avoid(
					collision.getPath1(), collision.getPath2(), collision);
			int stepForAvoidMethod = slowestArrivalStep(pathsForAvoidMethod);
			//TODO make sure they don't collide anymore
			if(stepForAvoidMethod < slowest) {
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
