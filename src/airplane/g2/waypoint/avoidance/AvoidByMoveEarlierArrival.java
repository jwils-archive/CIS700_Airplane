package airplane.g2.waypoint.avoidance;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import airplane.g2.util.PlaneUtil;
import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;

public class AvoidByMoveEarlierArrival extends AvoidByMove {

	public AvoidByMoveEarlierArrival(Point2D.Double moveAmount) {
		super(PlaneIndex.PLANE_ONE, moveAmount);
	}
	
	@Override
	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisionObject) {
		ArrayList<PlanePath> paths = PlaneUtil.pathsSortedByArrivalStep(path1, path2);
		
		return super.avoid(paths.get(0), paths.get(1), collisionObject);
	}
	
	
}
