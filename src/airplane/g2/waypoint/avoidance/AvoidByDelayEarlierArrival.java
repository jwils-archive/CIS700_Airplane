package airplane.g2.waypoint.avoidance;

import java.util.ArrayList;

import airplane.g2.util.PlaneUtil;
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
		
		ArrayList<PlanePath> paths = PlaneUtil.pathsSortedByArrivalStep(path1, path2);
		
		return super.avoid(paths.get(0), paths.get(1), collisonObject);
	}
}
