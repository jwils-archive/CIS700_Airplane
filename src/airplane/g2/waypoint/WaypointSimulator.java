package airplane.g2.waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import airplane.g2.sim.Simulator;
import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class WaypointSimulator extends Simulator {
	private HashMap<Plane, PlanePath> waypointHash;
	public WaypointSimulator(HashMap<Plane, PlanePath> waypointHash) {
		setWaypointHash(waypointHash);
	}
	
	public WaypointSimulationResult startWaypointSimulation(int round) {
		SimulationResult result = startSimulation(getPlanesByIndex(), round);
		WaypointSimulationResult waypointResult = new WaypointSimulationResult(result);
		waypointResult.setWaypointHash(waypointHash);
		return waypointResult;
	}
	
	protected ArrayList<Plane> getPlanesByIndex() {
		ArrayList<Plane> sorted = new ArrayList<Plane>(waypointHash.keySet());
		Collections.sort(sorted, new Comparator<Plane>(){
			@Override
			public int compare(Plane arg0, Plane arg1) {
				return ((Integer) arg0.id).compareTo(arg1.id);
			}
		});
		return sorted;
	}
	
	protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
		//TODO - this is a copy of the updateSimulation method from the
		// Group2WaypointPlayer class.
		for (int i = 0; i < planes.size(); i++) { 
			if (bearings[i] == -2) continue;
			PlanePath path = waypointHash.get(planes.get(i));
			double newBearing = path.getBearing(round);
			
			if (bearings[i] >= 0 && Math.abs(newBearing - bearings[i]) > 9.5) {
				if ( (newBearing > bearings[i] && newBearing - bearings[i] < 180) || (newBearing < bearings[i] && newBearing - bearings[i] > 180)) {
					bearings[i] = (bearings[i] + 9.5) % 360;
				} else {
					bearings[i] = (bearings[i] - 9.5 + 360) % 360;
				}
			} else {
				bearings[i] = newBearing;
			}
		}
		return bearings;
	}
	public HashMap<Plane, PlanePath> getWaypointHash() {
		return waypointHash;
	}
	public void setWaypointHash(HashMap<Plane, PlanePath> waypointHash) {
		this.waypointHash = waypointHash;
	}
}
