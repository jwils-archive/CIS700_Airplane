package airplane.g2;

import java.util.ArrayList;

import airplane.g2.util.PlaneUtil;
import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class Group2Player extends airplane.sim.Player {

	@Override
	public String getName() {
		return "Group 2";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		
	}

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round,
			double[] bearings) {
		
		SimulationResult result = startSimulation(planes, round);
		
		if (result.isSuccess()) {
			for (int i = 0; i < planes.size(); i++) {
				Plane p = planes.get(i);
				if (p.getDepartureTime() <= round) {
					bearings[i] = calculateBearingWithCap(p, bearings[i]);
				}
						
			}
		} else {
			int minStepsToDestination = 1000;
			int indexOfStepsToDestination = 1000;
			for (int i = 0; i < planes.size(); i++) {
				Plane p = planes.get(i);
				if (PlaneUtil.stepsToDestination(p) <= minStepsToDestination) {
					minStepsToDestination = PlaneUtil.stepsToDestination(p);
					indexOfStepsToDestination = i;
				}
			}
			
			Plane p = planes.get(indexOfStepsToDestination);
			if (p.getDepartureTime() <= round) {
				if (p.getDepartureTime() == round) {
					bearings[indexOfStepsToDestination] = calculateBearing(p.getLocation(), p.getDestination());
				} else {
					bearings[indexOfStepsToDestination] = bearings[indexOfStepsToDestination]; //+ 10;
				}
			}
			
			for (int i = 0; i < planes.size(); i++) {
				if (i != indexOfStepsToDestination) {
					bearings[i] = calculateBearingWithCap(p, bearings[i]);
				}
			}
		}
		
		return bearings;
	}
	double calculateBearingWithCap(Plane p, double oldBearing) {
		return calculateBearingWithCap(p, oldBearing, 10);
	}
	double calculateBearingWithCap(Plane p, double oldBearing, int cap) {
		if (oldBearing == -2) {
			return -2;
		}
		
		double bearing = calculateBearing(p.getLocation(), p.getDestination());
		
		if (Math.abs(bearing - oldBearing) > cap) {
			if (oldBearing > bearing) {
				return (oldBearing - cap + 360) % 360;
			} else {
				return (oldBearing + cap) % 360;
			}
		} else {
			return bearing;
		}
	
	}
	
	@Override
	protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
		// not implemented
		
		for (int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			if (p.getDepartureTime() <= round) {
				if (bearings[i] == -1) {
					bearings[i] = calculateBearing(p.getLocation(), p.getDestination());
				} else {
					bearings[i] = calculateBearingWithCap(p, bearings[i], 2);
				}
			}	
		}	
		
		return bearings;
	}
	
	double getVeerBearing(Plane plane, Plane toAvoid) {
		//TODO
		return -1;
	}
}
