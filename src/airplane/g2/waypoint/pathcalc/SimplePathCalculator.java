package airplane.g2.waypoint.pathcalc;

import airplane.g2.PlanePair;
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
	private ArrayList<AvoidMethod> avoidMethodsUsed = null;

	//TODO Better method of adding waypoint.
	@Override
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		modifyHashWithAvoidanceSchemes(waypointHash);
		insertDelaysUntilNoCrash(waypointHash, 1000);
	}
	
	protected void modifyHashWithAvoidanceSchemes(HashMap<Plane, PlanePath> waypointHash) {
		avoidMethodsUsed = new ArrayList<AvoidMethod>();
		int x = 0, limit = 100;
		for(; x < limit; x++) {
			// always simulate from the beginning
			WaypointSimulationResult result = simulationResultWithHash(waypointHash);
			if(result.isCollision()) {
				putPaths(waypointHash, planePathsDidCollide(waypointHash, result.getCollision()));
			} else {
				break;
			}
		}
		if(x == limit) {
			logger.warn("There will be a crash, inserting delays");
		}
	}
	
	protected void insertDelaysUntilNoCrash(HashMap<Plane, PlanePath> waypointHash) {
		insertDelaysUntilNoCrash(waypointHash, 100);
	}
	
	protected void insertDelaysUntilNoCrash(HashMap<Plane, PlanePath> waypointHash, int tryLimit) {
		int x = 0, limit = tryLimit;
		ArrayList<PlanePath> paths = PlaneUtil.planePathsSortedByIndex(new ArrayList<PlanePath>(waypointHash.values()));
		for(; x < limit; x++) {
			WaypointSimulationResult result = simulationResultWithHash(waypointHash);
			if(!result.isCollision()) break;
			
			insertDelayForSimulationResult(result, paths); 
		}
		if(x == limit) {
			logger.warn("Could not delay planes successfully. There will be an unavoidable crash.");
		}
	}
	
	protected WaypointSimulationResult simulationResultWithHash(HashMap<Plane, PlanePath> waypointHash) {
		return new WaypointSimulator(waypointHash).startWaypointSimulation(0);
	}
	
	protected void insertDelayForSimulationResult(WaypointSimulationResult result, ArrayList<PlanePath> paths) {
		PlanePair pair = result.getCollidingPairs().get(0);
		
		PlanePath path1 = paths.get(pair.getFirstIndex());
		PlanePath path2 = paths.get(pair.getSecondIndex());
		
		// do this by plane id so we don't wind up in a cycle (this is a fail-safe)
		PlanePath sooner = path1.getPlaneId() < path2.getPlaneId() ? path1: path2;
		PlanePath later = path1 == sooner ? path2 : path1;
		
		sooner.delay(10);
	}
	
	protected WaypointSimulationResult simulate(HashMap<Plane, PlanePath> waypointHash) {
		WaypointSimulator sim = new WaypointSimulator(waypointHash);
		return sim.startWaypointSimulation(0);
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
	
	public PlanePath[] planePathsDidCollide(HashMap<Plane, PlanePath> waypointHash, PlaneCollision collision) {
		ArrayList<AvoidMethod> methods = getAvoidMethods();
		
		ArrayList<AvoidResult> results = new ArrayList<AvoidResult>();
		
		for(AvoidMethod avoid: methods) {
			PlanePath[] pathsForAvoidMethod = avoid.avoid(
					collision.getPath1(), collision.getPath2(), collision);
			
			int stepForAvoidMethod = slowestArrivalStep(pathsForAvoidMethod);
			WaypointSimulationResult simResultLocal = collidePlanePaths(waypointHash,
					pathsForAvoidMethod[0], pathsForAvoidMethod[1]);
			WaypointSimulationResult simResultGlobal = 
					simulate(PlaneUtil.waypointMapWithReplacingPaths(waypointHash, pathsForAvoidMethod));
			
			// don't consider results that aren't either a collision or success
			if(simResultLocal.wasStopped()) continue;
			if(!(simResultLocal.isCollision() || simResultLocal.isSuccess())) continue;
			
			AvoidResult result = new AvoidResult(avoid, pathsForAvoidMethod, 
					stepForAvoidMethod, collision, 
					simResultLocal.getCollision(),
					simResultGlobal.isCollision() ? simResultGlobal.getCollision() : null);
			
			results.add(result);
		}
		
		results = avoidResultsByHeuristic(results);
		results = avoidResultsWithoutCrashingSooner(results);
		
		if(results.isEmpty()) {
			return new PlanePath[]{collision.getPath1(), collision.getPath2()};
		}
		
		AvoidResult selectedResult = results.get(results.size()-1);
		
		avoidMethodsUsed.add(selectedResult.getAvoidMethod());
		
		return selectedResult.getPaths();
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
		int noCrashFactor = 250;
		int delayPenalty = 0;
		int globalCollisionCrashFactor = r.getNextGlobalCollision() == null ? 200 : 0;
//		if(r.getAvoidMethod().getClass() == AvoidByDelay.class) {
//			delayPenalty = -100;
//		}
		if(r.getNextCollision() != null) { 
			noCrashFactor = 0;
			nextCrashRound = r.getNextCollision().getRound();
		}
		int crashFactor = 5 * (nextCrashRound - r.getPreviousCollision().getRound());
		
		return adjustedSteps + crashFactor + noCrashFactor + delayPenalty + globalCollisionCrashFactor; 
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
	
	public WaypointSimulationResult collidePlanePaths(HashMap<Plane, PlanePath> waypointHash, 
			PlanePath a, PlanePath b) {
		ArrayList<PlanePath> paths = new ArrayList<PlanePath>();
		paths.add(a);
		paths.add(b);
		
		// run the simulation only with the passed paths
		HashMap<Plane, PlanePath> hash = PlaneUtil.waypointMapWithPaths(paths);
		WaypointSimulationResult result = new WaypointSimulator(hash).startWaypointSimulation(0);
		return result;
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
