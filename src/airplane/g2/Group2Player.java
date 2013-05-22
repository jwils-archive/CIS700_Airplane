package airplane.g2;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import airplane.g2.util.PlaneUtil;
import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class Group2Player extends airplane.sim.Player {	
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	
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
	
	protected void updateBearingOfPlaneClosestToDestinationWithIndex(int indexOfStepsToDestination, ArrayList<Plane> planes, 
			int round, double[] bearings) {
		Plane p = planes.get(indexOfStepsToDestination);	
		if (p.getDepartureTime() == round) {
			bearings[indexOfStepsToDestination] = calculateBearingToDestination(p);
		} else if(p.getDepartureTime() < round) {
			bearings[indexOfStepsToDestination] = p.getBearing() + getVeerBearing(null, null);
		}
	}
	
	protected void updatePlanesAfterSimulationFailure(ArrayList<Plane> planes, int round,
			double[] bearings) {
		
		int indexOfStepsToDestination = getIndexOfPlaneClosestToDestination(planes);
		updateBearingOfPlaneClosestToDestinationWithIndex(indexOfStepsToDestination, planes, round, bearings);
		
		for (int i = 0; i < planes.size(); i++) {
			if (i != indexOfStepsToDestination) {
				bearings[i] = calculateBearingWithCap(planes.get(i), bearings[i]);
			}
		}
	}
	
	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round,
			double[] bearings) {
		
		SimulationResult result = startSimulation(planes, round);
		
		logger.info(String.format("Simulation %s", result.isSuccess() ?  "succeeded" : "failed"));
		if (result.isSuccess()) {
			updatePlanesAfterSimulationSuccess(planes, round, bearings);
		} else {
			updatePlanesAfterSimulationFailure(planes, round, bearings);
		}
		
		return bearings;
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
		
		if (oldBearing > bearing) {
			return (oldBearing - cap + 360) % 360;
		} else {
			return (oldBearing + cap) % 360;
		}	
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
			bearings[i] = calculateBearingWithCap(p, 10);
		}	
		
		return bearings;
	}
	
	double getVeerBearing(Plane plane, Plane toAvoid) {
		//TODO
		return 10;
	}
}
