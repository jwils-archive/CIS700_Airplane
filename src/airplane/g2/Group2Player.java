package airplane.g2;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import airplane.g2.util.PlaneUtil;
import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class Group2Player extends airplane.sim.Player {	
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	protected ArrayList<PlanePair> crashes = new ArrayList<PlanePair>();

	@Override
	public String getName() {
		return "Group 2";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		
	}
	
	/**
	 * Returns the index of the plane which is closest to its destination.
	 * @param planes
	 * @return
	 */
	protected int getIndexOfPlaneClosestToDestination(ArrayList<Plane> planes) {
		int minStepsToDestination = 1000;
		int indexOfStepsToDestination = 1000;
		for (int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			if (PlaneUtil.stepsToDestination(p) <= minStepsToDestination) {
				minStepsToDestination = PlaneUtil.stepsToDestination(p);
				indexOfStepsToDestination = i;
			}
		}
		return indexOfStepsToDestination;
	}
	
	protected void updateDepartedPlanesWithBearingCap(ArrayList<Plane> planes, 
			int round, double[] bearings) {
		for (int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			if (p.getDepartureTime() <= round) {
				bearings[i] = calculateBearingWithCap(p);
			}
					
		}
	}
	
	protected void updatePlanesAfterSimulationSuccess(ArrayList<Plane> planes, int round,
			double[] bearings) {
		updateDepartedPlanesWithBearingCap(planes, round, bearings);
	}
	
	protected void updateBearingOfPlaneClosestToDestinationWithIndex(int index, ArrayList<Plane> planes, 
			int round, double[] bearings) {
		Plane p = planes.get(index);	
		if (p.getDepartureTime() == round) {
			bearings[index] = calculateBearingToDestination(p);
		} else if(p.getDepartureTime() < round) {
			bearings[index] = bearingForPlaneWithVeer(planes, index);
		}
	}
	
	protected ArrayList<Integer> planesThatCrashedInto(ArrayList<Plane> planes, int index) {
		ArrayList<Integer> crashedInto = new ArrayList<Integer>();
		for(PlanePair pp: crashes) {
			if(!pp.contains(index)) continue;
			crashedInto.add(pp.planeNot(index));
		}
		return crashedInto;
	}
	
	protected double bearingForPlaneWithVeer(ArrayList<Plane> planes, int index) {
		// was the plane involved in crashes
		ArrayList<Integer> crashedInto = planesThatCrashedInto(planes, index);
		Plane p = planes.get(index);
		if(crashedInto.isEmpty()) return calculateBearingWithCap(p);
		
		// we should have at least one plane
		Integer crashIndex = crashedInto.get(0);
		Plane firstPlane = planes.get(crashIndex);
		
		return PlaneUtil.normalizedBearing(getVeerBearing(firstPlane, p));
	}
	
	protected void updatePlanesAfterSimulationFailure(ArrayList<Plane> planes, int round,
			double[] bearings) {
		
		ArrayList<PlaneInfo> planeData = PlaneInfo.toPlaneInfo(planes);
		ArrayList<PlaneInfo> sortedPlaneData = PlaneInfo.sortedByStepsToDestinationIncludingDeparture(planeData, round);
		
		int lastIndex = sortedPlaneData.size()-1;
		for(int i = 0; i < lastIndex; i ++) {
			int planeIndex = sortedPlaneData.get(i).getIndex();
			updateBearingOfPlaneClosestToDestinationWithIndex(planeIndex, planes, round, bearings);
		}
		
		PlaneInfo lastPlane = sortedPlaneData.get(lastIndex);
		bearings[lastPlane.getIndex()] = calculateBearingWithCap(lastPlane);
	}
	
	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round,
			double[] bearings) {
		
		if (!shouldVeerPlanes(planes, round)) {
			updatePlanesAfterSimulationSuccess(planes, round, bearings);
		} else {
			updatePlanesAfterSimulationFailure(planes, round, bearings);
		}
		
		return bearings;
	}
	
	protected Boolean shouldVeerPlanes(ArrayList<Plane> planes, int round) {
		SimulationResult result = startSimulation(planes, round);
		crashes = getSimulationCrashes(result, 12);
		
		logger.info(String.format("Simulation %s", result.isSuccess() ?  "succeeded" : "failed"));
		logger.info(String.format("Crashes: %s", crashes));
		
		if(result.isSuccess()) return false;
		if(simulationCrashTooFarIntoFuture(result, round)) return false;

		return true;
	}
	
	/**
	 * Use to determine whether there was a plane crash in the future and whether
	 * it is too far into the future to consider.
	 * @param result
	 * @param round
	 * @return
	 */
	protected Boolean simulationCrashTooFarIntoFuture(SimulationResult result, int currentRound) {
		return result.getReason() == SimulationResult.TOO_CLOSE && 
				result.getRound() > currentRound + 40;
	}
	
	protected double getBearingCap() {
		return 10.0;
	}
	double calculateBearingWithCap(Plane p) {
		return calculateBearingWithCap(p, getBearingCap());
	}
	double calculateBearingWithCap(Plane p, double cap) {
		double oldBearing = p.getBearing();
		
		if (isLanded(oldBearing)) {
			return oldBearing;
		}
		
		double bearing = calculateBearingToDestination(p);
		
		if(isAwaitingTakeoff(oldBearing)) return bearing;
		if (Math.abs(bearing - oldBearing) <= cap) return bearing;
		
		int sign = oldBearing > bearing ? -1 : 1;
		return PlaneUtil.normalizedBearing(oldBearing + sign * cap);	
	}
	
	protected Boolean isAwaitingTakeoff(double bearing) {
		return bearing == -1;
	}
	
	protected Boolean isLanded(double bearing) {
		return bearing == -2;
	}
	
	protected double calculateBearingToDestination(Plane p) {
		return calculateBearing(p.getLocation(), p.getDestination());
	}
	
	@Override
	protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
		// not implemented
		
		for (int i = 0; i < planes.size(); i++) {
			
			Plane p = planes.get(i);
			if (p.getDepartureTime() > round) continue; // ignore planes which may not depart yet
			
			double bearing = bearings[i];
			if(isLanded(bearing)) continue;
			
			if(isAwaitingTakeoff(bearing)) {
				bearings[i] = calculateBearingToDestination(p);
				continue;
			}
			
			// mid-flight
			bearings[i] = calculateBearingWithCap(p, getBearingCap());
		}	
		
		return bearings;
	}
	
	
	double getVeerBearing(Plane prioritizedPlane, Plane deprioritizedPlane) {
		return PlaneUtil.bearingAway(prioritizedPlane, deprioritizedPlane, 9.9);
	}
	
	/**
	 * Runs the simulation and returns any crashes that occurred.
	 * @return
	 */
	ArrayList<PlanePair> getSimulationCrashes(SimulationResult result, double distanceThreshold) {
		ArrayList<Plane> finalPlanes = result.getPlanes();
		return PlaneUtil.detectCollisions(finalPlanes, distanceThreshold);
	}
}
