package airplane.g2.waypoint;

import java.util.ArrayList;

import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class WaypointSimulationResult extends SimulationResult {

	public WaypointSimulationResult(int _reason, int _round,
			ArrayList<Plane> _planes) {
		super(_reason, _round, _planes);
		// TODO Auto-generated constructor stub
	}
	
	public WaypointSimulationResult(SimulationResult result) {
		super(result.getReason(), result.getRound(), result.getPlanes());
	}

}
