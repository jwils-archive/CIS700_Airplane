package airplane.g2.waypoint.pathcalc;

import airplane.g2.util.PlaneUtil;
import airplane.g2.waypoint.avoidance.*;
import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.log4j.Logger;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.waypoint.WaypointSimulationResult;
import airplane.g2.waypoint.WaypointSimulator;
import airplane.sim.Plane;

public class SimplePathCalculator extends PathCalculator{
	private Logger logger = Logger.getLogger(this.getClass());

	//TODO Better method of adding waypoint.
	@Override
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		int x = 0, limit = 1000;
		for(; x < limit; x++) {
			// always simulate from the beginning
			WaypointSimulationResult result = new WaypointSimulator(waypointHash).startWaypointSimulation(0);
			if(result.isCollision()) {
				putPaths(waypointHash, planePathsDidCollide(result.getCollision()));
			} else {
				break;
			}
		}
		if(x == limit) {
			logger.warn("There will be a crash!");
		}
	}
	
	public WaypointSimulationResult simulate(HashMap<Plane, PlanePath> waypointHash) {
		WaypointSimulator sim = new WaypointSimulator(waypointHash);
		return sim.startWaypointSimulation(0);
	}
	
	public ArrayList<PlaneCollision> collisionsInHash(HashMap<Plane, PlanePath> waypointHash) {
		ArrayList<PlanePath> paths = new ArrayList<PlanePath>(waypointHash.values());
		ArrayList<PlaneCollision> collisions = new ArrayList<PlaneCollision>();
		for(int i = 0, count = paths.size(); i < count; i++) {
			for(int j = i + 1; j < count; j++) {
				PlanePath path1 = paths.get(i);
				PlanePath path2 = paths.get(j);
				
				PlaneCollision collision = collidePlanePaths(path1, path2);
				if(collision == null) continue;
				
				collisions.add(collision);
			}
		}
		return collisions;
	}
	
	public ArrayList<PlaneCollision> collisionsSortedByRound(ArrayList<PlaneCollision> collisions) {
		ArrayList<PlaneCollision> sortedCollisions = new ArrayList<PlaneCollision>(collisions);
		Collections.sort(sortedCollisions, new Comparator<PlaneCollision>(){
			@Override
			public int compare(PlaneCollision o1, PlaneCollision o2) {
				return ((Integer) o1.getRound()).compareTo(o2.getRound());
			}
		});
		return sortedCollisions;
	}
	
	public PlaneCollision soonestCollision(ArrayList<PlaneCollision> collisions) {
		if(collisions.isEmpty()) return null;
		
		ArrayList<PlaneCollision> sorted = collisionsSortedByRound(collisions);
		return sorted.get(0);
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
		ArrayList<AvoidMethod> methods = getAvoidMethods();
		
		ArrayList<AvoidResult> results = new ArrayList<AvoidResult>();
		
		for(AvoidMethod avoid: methods) {
			PlanePath[] pathsForAvoidMethod = avoid.avoid(
					collision.getPath1(), collision.getPath2(), collision);
			
			int stepForAvoidMethod = slowestArrivalStep(pathsForAvoidMethod);
			PlaneCollision newCollision = collidePlanePaths(pathsForAvoidMethod[0], pathsForAvoidMethod[1]);
			AvoidResult result = new AvoidResult(avoid, pathsForAvoidMethod, 
					stepForAvoidMethod, collision, newCollision);
			
			results.add(result);
		}
		
		results = avoidResultsByHeuristic(avoidResultsWithoutCrashingSooner(results));
		
		if(results.isEmpty()) {
			return new PlanePath[]{collision.getPath1(), collision.getPath2()};
		}
		
		return results.get(results.size()-1).getPaths();	
	}
	
	/**
	 * Removes results where we crashed sooner
	 * @param results
	 * @return
	 */
	protected ArrayList<AvoidResult> avoidResultsWithoutCrashingSooner(ArrayList<AvoidResult> results) {
		ArrayList<AvoidResult> filtered = new ArrayList<AvoidResult>();
		for(AvoidResult result: results) {
			if(result.isBetterThanPreviousCollision()) 
				filtered.add(result);
		}
		return filtered;
	}
	
	protected Integer avoidResultHeuristicValue(AvoidResult r) {
		// we want the heuristic to be greater the better it is
		int adjustedSteps = 1000-slowestArrivalStep(r.getPaths());
		int nextCrashRound = 0;
		// bonus points if we don't crash
		int noCrashFactor = 200;
		if(r.getNextCollision() != null) { 
			noCrashFactor = 0;
			nextCrashRound = r.getNextCollision().getRound();
		}
		int crashFactor = 5 * (nextCrashRound - r.getPreviousCollision().getRound());
		
		return adjustedSteps + crashFactor + noCrashFactor; 
	}
	
	protected ArrayList<AvoidResult> avoidResultsByHeuristic(ArrayList<AvoidResult> results) {
		ArrayList<AvoidResult> sorted = new ArrayList<AvoidResult>(results);
		
		Collections.sort(sorted, new Comparator<AvoidResult>(){
			@Override
			public int compare(AvoidResult arg0, AvoidResult arg1) {
				arg0.setHeuristicValue(avoidResultHeuristicValue(arg0));
				arg1.setHeuristicValue(avoidResultHeuristicValue(arg1));
				return ((Integer)arg0.getHeuristicValue()).compareTo(arg1.getHeuristicValue());
			}
		});
		
		return sorted;
	}
	
	public Boolean planesCollideBeforePreviousCollision(PlaneCollision collision, PlanePath[] paths) {
		PlaneCollision newCollision = collidePlanePaths(paths);
		if(newCollision == null) return false;
		
		return collision.getRound() >= newCollision.getRound();
	}
	
	public PlaneCollision collidePlanePaths(PlanePath a, PlanePath b) {
		return a.getPlaneCollision(b);
	}
	
	public PlaneCollision collidePlanePaths(PlanePath[] paths) {
		return collidePlanePaths(paths[0], paths[1]);
	}
	
	public Boolean doesCollide(PlanePath a, PlanePath b) {
		return collidePlanePaths(a, b) != null;
	}
	
	public ArrayList<Point2D.Double> getCompassPointsOfMagnitude(double mag) {
		ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();
		Collections.addAll(list, new Point2D.Double[]{
				new Point2D.Double(mag, mag),
				new Point2D.Double(0, mag),
				new Point2D.Double(mag, 0),
				new Point2D.Double(mag, -mag),
				new Point2D.Double(-mag, mag),
				new Point2D.Double(-mag, -mag),
				new Point2D.Double(0, -mag),
				new Point2D.Double(-mag, 0)
		});
		return list;
	}
	
	public ArrayList<AvoidMethod> getCompassMoveMethodsOfMagnitude(PlaneIndex index, double mag) {
		ArrayList<Point2D.Double> pts = getCompassPointsOfMagnitude(mag);
		ArrayList<AvoidMethod> methods = new ArrayList<AvoidMethod>();
		for(Point2D.Double pt: pts) {
			methods.add(new AvoidByMove(index, pt));
		}
		return methods;
	}
	
	public ArrayList<AvoidMethod> getAvoidMethods() {
		ArrayList<AvoidMethod> methods = new ArrayList<AvoidMethod>();
		
		methods.addAll(getCompassMoveMethodsOfMagnitude(PlaneIndex.PLANE_TWO, 5.1));
		methods.addAll(getCompassMoveMethodsOfMagnitude(PlaneIndex.PLANE_TWO, 9));
		methods.addAll(getCompassMoveMethodsOfMagnitude(PlaneIndex.PLANE_TWO, 10));
		
		for(int delay: new int[]{5, 10, 20}) {
			methods.add(new AvoidByDelay(PlaneIndex.PLANE_TWO, delay));
		}
		
		return methods;
	}
	

}
