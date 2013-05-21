package airplane.g2;

import java.util.ArrayList;

import airplane.sim.Plane;

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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
		// not implemented
		return bearings;
	}
}
