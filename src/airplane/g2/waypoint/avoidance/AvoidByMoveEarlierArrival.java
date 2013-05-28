package airplane.g2.waypoint.avoidance;

import java.awt.geom.Point2D;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;

public class AvoidByMoveEarlierArrival extends AvoidByMove {

	public AvoidByMoveEarlierArrival(Point2D.Double moveAmount) {
		super(PlaneIndex.PLANE_ONE, moveAmount);
	}
	
	@Override
	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisionObject) {
		PlanePath earliestPath = path1;
		PlanePath latestPath = path2;
		if(earliestPath.getArrivalStep() > latestPath.getArrivalStep()) {
			earliestPath = path2;
			latestPath = path2;
		}
		
		return super.avoid(earliestPath, latestPath, collisionObject);
	}
	
	
}
