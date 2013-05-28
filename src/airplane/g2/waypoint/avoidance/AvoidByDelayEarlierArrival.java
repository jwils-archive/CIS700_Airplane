package airplane.g2.waypoint.avoidance;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;

public class AvoidByDelayEarlierArrival extends AvoidByDelay {

	public AvoidByDelayEarlierArrival(int delayAmount) {
		// always delay the first plane, which we will make the one 
		// arriving soonest
		super(PlaneIndex.PLANE_ONE, delayAmount);
	}
	
	@Override
	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisonObject) {
		
		PlanePath earliestPath = path1;
		PlanePath latestPath = path2;
		if(earliestPath.getArrivalStep() > latestPath.getArrivalStep()) {
			earliestPath = path2;
			latestPath = path2;
		}
		
		return super.avoid(earliestPath, latestPath, collisonObject);
	}
}
