package airplane.g2.waypoint.pathcalc;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import airplane.g2.PlanePair;
import airplane.g2.util.PlaneUtil;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.waypoint.WaypointSimulationResult;
import airplane.g2.waypoint.avoidance.AvoidByDelay;
import airplane.g2.waypoint.avoidance.AvoidByMove;
import airplane.g2.waypoint.avoidance.AvoidByRandomPoint;
import airplane.g2.waypoint.avoidance.AvoidMethod;
import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;
import airplane.sim.Plane;

public class LockPathCalculator extends SimplePathCalculator {
	private Boolean planeIsLocked[] = null;
	private HashMap<Plane, PlanePath> waypointHash;
	
	public LockPathCalculator() {
		simulationUpdateLimit = 1000;
	}
	
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		this.waypointHash = waypointHash;
		Boolean wasSuccessful = false;
		for(int x = 0, limit = 20; !wasSuccessful && x < limit; x ++) {
			unlockPaths();
			wasSuccessful = modifyHashLockingFromLatestToEarliest();
			// try to delay unlocked planes
			if(!wasSuccessful) {
				wasSuccessful = delayUnlockedPlanes();
			}
		}
		if(!wasSuccessful) {
			logger.error("Paths will crash.");
		}
	}
	
	protected Boolean delayUnlockedPlanes() {
		ArrayList<PlanePath> unlocked = getUnlockedPaths();
		for(PlanePath path: unlocked) {
			if(delayPlanePathUntilSimulationSuccess(path)) {
				lockPath(path);
			} else {
				return false;
			}
		}
		return true;
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
	
	protected ArrayList<PlanePath> getUnlockedPaths() {
		ArrayList<PlanePath> lockedPaths = new ArrayList<PlanePath>();
		ArrayList<PlanePath> paths = getPlanePaths();
		for(int i = 0, size = planeIsLocked.length; i < size; i ++) {
			if(!pathIsLockedAt(i)) {
				lockedPaths.add(paths.get(i));
			}
		}
		return lockedPaths;
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
	
	protected void minimizeMassCollisions() {
		ArrayList<PlanePath> sortedByReverseArrival = pathsByReverseArrival();
		
		
	}
	
//	protected ArrayList<PlanePath> pathsByCollisionCount() {
//		ArrayList<PlanePath> planePaths = getPlanePaths();
//		HashMap<Integer, ArrayList<PlanePath>> collisions = getAllCollisions(planePaths);
//		
//		Collections.sort(planePaths, new Comparator<PlanePath>() {
//
//			@Override
//			public int compare(PlanePath o1, PlanePath o2) {
//				
//				return 0;
//			}
//			
//		});
//	}
	
	protected HashMap<Integer, ArrayList<PlanePath>> getAllCollisions(ArrayList<PlanePath> paths) {
		HashMap<Integer, ArrayList<PlanePath>> results = new HashMap<Integer, ArrayList<PlanePath>>();
		for (PlanePath planePath : paths) {
			results.put(planePath.getPlaneId(), getAllCollisions(planePath,paths));
		}
		return results;
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
			ArrayList<PlanePath> unlockedPaths = getUnlockedPaths();
			logger.error(String.format("Something went wrong with the simulation. There are %d unlocked paths.", unlockedPaths.size()));
			
			WaypointSimulationResult lockedResult = collidePlanePaths(getLockedPaths());
			if(!lockedResult.isSuccess()) {
				logger.error("Crashes exist with locked paths.");
			}
			
			return false;
		}
		
		return true;
	}
	
	protected Boolean delayPlanePathUntilSimulationSuccess(PlanePath path) {
		ArrayList<PlanePath> paths = newListWith(getLockedPaths(), path);
	
		int i = 0;
		int delay = 2;
		int limit = 10;
		WaypointSimulationResult result;
		path.delay(10);
		do {
			path.delay(2);
			result = collidePlanePaths(paths);
			i++;
		} while (!result.isSuccess() && i < limit);
		
		if(!result.isSuccess()) {
			logger.error("Could not delay plane enough");
		}
		
		return result.isSuccess();
	}
	
	protected ArrayList<PlanePath> getAllCollisions(PlanePath planePath, ArrayList<PlanePath> otherPaths) {
		ArrayList<PlanePath> results = new ArrayList<PlanePath>();
		for (PlanePath otherPath : otherPaths) {
			if (otherPath.getPlaneId() == planePath.getPlaneId()) {
				continue;
			}
			ArrayList<PlanePath> paths = new ArrayList<PlanePath>();
			paths.add(planePath);
			paths.add(otherPath);
			WaypointSimulationResult result = collidePlanePaths(paths);
			if (!result.isSuccess()) {
				results.add(otherPath);
			}
		}
		return results;
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
	
	protected Double avoidResultHeuristicValue(AvoidResult r) {
		// we want the heuristic to be greater the better it is
		double adjustedSteps = 100-slowestArrivalStep(r.getPaths());
		// bonus points if we don't crash
		int noCrashFactor = 250;
		int globalCollisionCrashFactor = r.getNextGlobalCollision() == null ? 200 : 0;
		if(r.getNextCollision() != null) { 
			noCrashFactor = 0;
		}
		
		return adjustedSteps + noCrashFactor + globalCollisionCrashFactor; 
	}
	
	public ArrayList<AvoidMethod> getAvoidMethods() {
		ArrayList<AvoidMethod> methods = new ArrayList<AvoidMethod>();
	
		//methods.addAll(getCompassMoveMethodsOfMagnitude(2.6));
		methods.addAll(getCompassMoveMethodsOfMagnitude(5.1));
		methods.addAll(getCompassMoveMethodsOfMagnitude(20));
		methods.addAll(getCompassMoveMethodsOfMagnitude(10));
		methods.addAll(getCompassMoveMethodsOfMagnitude(15));
		methods.addAll(getCompassMoveMethodsOfMagnitude(50));
		
		for(PlaneIndex index: new PlaneIndex[]{PlaneIndex.PLANE_ONE}) {
			for(int delay: new int[]{1,2,3,5,10,20,50,100}) {
				methods.add(new AvoidByDelay(index, delay));
			}	
		}	
		
//		methods.add(new AvoidByRandomPoint(PlaneIndex.PLANE_ONE, new Point2D.Double(25, 25)));
//		methods.add(new AvoidByRandomPoint(PlaneIndex.PLANE_ONE, new Point2D.Double(75, 25)));
//		methods.add(new AvoidByRandomPoint(PlaneIndex.PLANE_ONE, new Point2D.Double(25, 75)));
//		methods.add(new AvoidByRandomPoint(PlaneIndex.PLANE_ONE, new Point2D.Double(75, 75)));
		
		return methods;
	}
}
