package airplane.g2.waypoint.pathcalc;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import airplane.g2.PlanePair;
import airplane.g2.util.PlaneUtil;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.waypoint.WaypointSimulationResult;
import airplane.g2.waypoint.avoidance.AvoidByDelay;
import airplane.g2.waypoint.avoidance.AvoidByMove;
import airplane.g2.waypoint.avoidance.AvoidByMoveEarlierArrival;
import airplane.g2.waypoint.avoidance.AvoidMethod;
import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;
import airplane.sim.Plane;

public class LockPathCalculator extends SimplePathCalculator {
	private Boolean planeIsLocked[] = null;
	private HashMap<Plane, PlanePath> waypointHash;
	
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		this.waypointHash = waypointHash;
		Boolean wasSuccessful = false;
		for(int x = 0, limit = 100; !wasSuccessful && x < limit; x ++) {
			unlockPaths();
			wasSuccessful = modifyHashLockingFromLatestToEarliest();
		}
		if(!wasSuccessful) {
			logger.error("Paths will crash.");
		}
	}
	
	protected void unlockPaths() {
		planeIsLocked = new Boolean[waypointHash.size()];
		Arrays.fill(planeIsLocked, false);
	}
	
	protected ArrayList<Plane> getPlanes() {
		return PlaneUtil.planesSortedByIndex(new ArrayList<Plane>(waypointHash.keySet()));
	}
	
	protected ArrayList<PlanePath> getPlanePaths() {
		return PlaneUtil.planePathsSortedByIndex(new ArrayList<PlanePath>(waypointHash.values()));
	}
	
	protected ArrayList<PlanePath> getLockedPaths() {
		ArrayList<PlanePath> lockedPaths = new ArrayList<PlanePath>();
		ArrayList<PlanePath> paths = getPlanePaths();
		for(int i = 0, size = planeIsLocked.length; i < size; i ++) {
			if(pathIsLockedAt(i)) {
				lockedPaths.add(paths.get(i));
			}
		}
		return lockedPaths;
	}
	
	protected Boolean pathIsLockedAt(int index) {
		return planeIsLocked[index];
	}
	
	protected Boolean pathIsLocked(PlanePath path) {
		return pathIsLockedAt(path.getPlaneId());
	}
	
	protected void lockPath(int index) {
		planeIsLocked[index] = true;
	}
	
	protected void lockPath(PlanePath path) {
		lockPath(path.getPlaneId());
	}
	
	protected void lockFirstPath(ArrayList<PlanePath> paths) {
		lockPath(paths.get(0));
	}
	
	protected Boolean modifyHashLockingFromLatestToEarliest() {
		ArrayList<PlanePath> sortedByReverseArrival = pathsByReverseArrival();
		
		lockFirstPath(sortedByReverseArrival);
		
		for(PlanePath path: sortedByReverseArrival) {
			if(pathIsLocked(path)) continue; // don't f with the path!
			
			PlanePath newPath = newPathRespectingLockedPaths(path);
			if(newPath == null) continue;
			
			lockPath(newPath);
			putPaths(waypointHash, new PlanePath[]{newPath});
		}
		
		WaypointSimulationResult result = collidePlanePaths(getPlanePaths());
		if(!result.isSuccess()) {
			logger.error("Something went wrong with the simulation.");
			return false;
		}
		
		return true;
	}
	
	protected ArrayList<PlanePath> newListWith(ArrayList<PlanePath> paths, PlanePath add) {
		ArrayList<PlanePath> copy = new ArrayList<PlanePath>();
		copy.add(add);
		copy.addAll(paths);
		return copy;
	}
	
	protected PlanePath getLockedPath(ArrayList<PlanePath> paths, PlanePair pair) {
		PlanePath first = paths.get(pair.getFirstIndex());
		if(pathIsLocked(first)) return paths.get(pair.getFirstIndex());
		PlanePath second = paths.get(pair.getSecondIndex());
		if(pathIsLocked(second)) return paths.get(pair.getSecondIndex());
		return null;
	}
	
	protected PlanePath newPathRespectingLockedPaths(PlanePath modifyPath) {
		ArrayList<PlanePath> lockedPaths = getLockedPaths();
		WaypointSimulationResult lockedResult = collidePlanePaths(lockedPaths);
		if(!lockedResult.isSuccess()) {
			logger.error("This shouldn't happen.");
		}
		ArrayList<PlanePath> allPaths = newListWith(lockedPaths, modifyPath);
		ArrayList<AvoidResult> results = new ArrayList<AvoidResult>();
		
		// we don't need to do anything if there is no collision initially
		WaypointSimulationResult initialResult = collidePlanePaths(allPaths);
		if(initialResult.isSuccess()) return modifyPath;
		
		PlanePath lockedPath = getLockedPath(allPaths, initialResult.getCollidingPairs().get(0));
		
		for(AvoidMethod method: getAvoidMethods()) {
			PlanePath[] adjustedPaths = new PlanePath[0];
		
			try {
				adjustedPaths = method.avoid(modifyPath, lockedPath, initialResult.getCollision());
			} catch(Exception ex) {
				logger.error(ex.toString());
			}
			PlanePath adjustedPath = adjustedPaths[0];
			
			ArrayList<PlanePath> newAllPaths = newListWith(lockedPaths, adjustedPath);
			WaypointSimulationResult localResult = collidePlanePaths(newAllPaths);
			ArrayList<PlanePath> globalPaths = PlaneUtil.planePathsReplacingPaths(getPlanePaths(), adjustedPath);
			WaypointSimulationResult globalResult = collidePlanePaths(globalPaths);
			
			if(localResult.getCollision() != null && globalResult.getCollision() == null) {
				logger.warn("This is a very strange error.");
			}
			
			
			if(!(localResult.isCollision() || localResult.isSuccess())) continue;
			
			int steps = slowestArrivalStep(newAllPaths).intValue();
			results.add(new AvoidResult(method, adjustedPaths, steps, 
					initialResult.getCollision(), localResult.getCollision(), 
					globalResult.getCollision()));
		}
		
		ArrayList<AvoidResult> resultsByHeuristic = avoidResultsByHeuristic(results);
		ArrayList<AvoidResult> resultsWithoutLocalCollision = avoidResultsWithoutLocalCollision(resultsByHeuristic);
		ArrayList<AvoidResult> resultsWithoutWorse = avoidResultsWithoutCrashingSooner(resultsByHeuristic);
		ArrayList<AvoidResult> resultsWithoutCollisions = avoidResultsWithoutGlobalCollision(resultsWithoutWorse);
		
//		if(resultsWithoutCollisions.isEmpty()) {
//			// implement delay until we don't have a crash.
//			WaypointSimulationResult result = collidePlanePaths(allPaths);
//			do {
//				modifyPath.delay(10);
//			}while(!result.isSuccess());
//		}
		
		if(resultsWithoutLocalCollision.isEmpty()) {
			return null;
		}
		
		if(resultsWithoutCollisions.isEmpty()) {
			AvoidResult returnResult = resultsWithoutLocalCollision.get(resultsWithoutLocalCollision.size()-1);
			PlanePath returnPath = returnResult.getPaths()[0];
			return returnPath;
		}
		
		AvoidResult returnResult = resultsWithoutCollisions.get(resultsWithoutCollisions.size()-1);
		PlanePath returnPath = returnResult.getPaths()[0];
		return returnPath;
	}
	protected ArrayList<AvoidResult> avoidResultsWithoutLocalCollision(ArrayList<AvoidResult> results) {
		ArrayList<AvoidResult> sorted = new ArrayList<AvoidResult>();
		for(AvoidResult result: results) {
			if(result.getNextCollision() == null) {
				sorted.add(result);
			}
		}
		return sorted;
	}
	protected ArrayList<AvoidResult> avoidResultsWithoutGlobalCollision(ArrayList<AvoidResult> results) {
		ArrayList<AvoidResult> sorted = new ArrayList<AvoidResult>();
		for(AvoidResult result: results) {
			if(result.getNextGlobalCollision() == null) {
				sorted.add(result);
			}
		}
		return sorted;
	}
	
	protected ArrayList<PlanePath> pathsByReverseArrival() {
		ArrayList<PlanePath> sortedByArrival = PlaneUtil.pathsSortedByArrivalStep(getPlanePaths());
		Collections.reverse(sortedByArrival);
		return sortedByArrival;
	}
	
	public ArrayList<AvoidMethod> getCompassMoveMethodsOfMagnitude(double mag) {
		ArrayList<Point2D.Double> pts = getCompassPointsOfMagnitude(mag);
		ArrayList<AvoidMethod> methods = new ArrayList<AvoidMethod>();
		for(Point2D.Double pt: pts) {
			methods.add(new AvoidByMove(PlaneIndex.PLANE_ONE, pt));
		}
		return methods;
	}
	
	public ArrayList<AvoidMethod> getAvoidMethods() {
		ArrayList<AvoidMethod> methods = new ArrayList<AvoidMethod>();
	
		methods.addAll(getCompassMoveMethodsOfMagnitude(5.1));
		methods.addAll(getCompassMoveMethodsOfMagnitude(20));
		methods.addAll(getCompassMoveMethodsOfMagnitude(10));
		methods.addAll(getCompassMoveMethodsOfMagnitude(15));
		
		for(PlaneIndex index: new PlaneIndex[]{PlaneIndex.PLANE_ONE}) {
			for(int delay: new int[]{10, 20, 30, 50, 100, 200}) {
				methods.add(new AvoidByDelay(index, delay));
			}	
		}	
		
		
		return methods;
	}
}
