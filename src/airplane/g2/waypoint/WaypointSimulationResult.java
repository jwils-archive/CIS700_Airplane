package airplane.g2.waypoint;

import java.util.ArrayList;
import java.util.HashMap;

import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class WaypointSimulationResult extends SimulationResult {
	
	private HashMap<Plane, PlanePath> waypointHash;
	private PlaneCollision collision;

	public WaypointSimulationResult(int _reason, int _round,
			ArrayList<Plane> _planes) {
		super(_reason, _round, _planes);
		// TODO Auto-generated constructor stub
	}
	
	public WaypointSimulationResult(SimulationResult result) {
		super(result.getReason(), result.getRound(), result.getPlanes());
	}

	public PlaneCollision getCollision() {
		return collision;
	}

	public void setCollision(PlaneCollision collision) {
		this.collision = collision;
	}

	public HashMap<Plane, PlanePath> getWaypointHash() {
		return waypointHash;
	}

	public void setWaypointHash(HashMap<Plane, PlanePath> waypointHash) {
		this.waypointHash = waypointHash;
	}

}
