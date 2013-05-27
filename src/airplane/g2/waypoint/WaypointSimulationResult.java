package airplane.g2.waypoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import airplane.g2.PlanePair;
import airplane.g2.util.PlaneUtil;
import airplane.sim.Plane;
import airplane.sim.SimulationResult;

public class WaypointSimulationResult extends SimulationResult {
	
	private HashMap<Plane, PlanePath> waypointHash;
	private ArrayList<PlanePath> paths;
	private PlaneCollision collision;
	private ArrayList<PlanePair> collidingPairs;
	private double[][] distances;
	private static Double collideThreshold = 10.01;

	public WaypointSimulationResult(int _reason, int _round,
			ArrayList<Plane> _planes) {
		super(_reason, _round, _planes);
		// TODO Auto-generated constructor stub
	}
	
	public WaypointSimulationResult(SimulationResult result) {
		super(result.getReason(), result.getRound(), result.getPlanes());
		setDistances(PlaneUtil.getDistances(result.getPlanes()));
		setCollidingPairs(PlaneUtil.detectCollisions(result.getPlanes(), collideThreshold));
	}
	
	protected PlanePath pathAt(int index) {
		return paths.get(index);
	}
	
	protected ArrayList<PlanePath> pathsByIndex() {
		return PlaneUtil.planePathsSortedByIndex(
				new ArrayList<PlanePath>(waypointHash.values()));
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
		this.paths = pathsByIndex();
		
		loadCollision();
	}
	
	protected void loadCollision() {
		if(! isCollision()) {
			setCollision(null);
			return;
		}
		
		PlanePair pair = getCollidingPairs().get(0);
		
		PlaneCollision collision = new PlaneCollision();
		PlanePath path1 = pathAt(pair.getFirstIndex());
		PlanePath path2 = pathAt(pair.getSecondIndex());
		collision.setPath1(path1);
		collision.setPath2(path2);
		collision.setPlane1segment(path1.getSegmentForLastBearingRequest());
		collision.setPlane2segment(path2.getSegmentForLastBearingRequest());
		
		collision.setCollisionPoint(collisionPointForPair(pair));
		collision.setRound(getRound());
		
		setCollision(collision);
	}
	
	protected Point2D.Double collisionPointForPair(PlanePair pair) {
		return PlaneUtil.midpointBetweenPlanes(
				planeAt(pair.getFirstIndex()), 
				planeAt(pair.getSecondIndex()));
	}

	public double[][] getDistances() {
		return distances;
	}

	public void setDistances(double[][] distances) {
		this.distances = distances;
	}

	public ArrayList<PlanePair> getCollidingPairs() {
		return collidingPairs;
	}

	public void setCollidingPairs(ArrayList<PlanePair> collidingPairs) {
		this.collidingPairs = collidingPairs;
	}

	public ArrayList<PlanePath> getPaths() {
		return paths;
	}

	public void setPaths(ArrayList<PlanePath> paths) {
		this.paths = paths;
	}
	
	public Boolean isCollision() {
		return getReason() == SimulationResult.TOO_CLOSE;
	}
	
	public Boolean wasStopped() {
		return getReason() == SimulationResult.STOPPED;
	}
	
	protected Plane planeAt(int index) {
		return getPlanes().get(index);
	}

}
