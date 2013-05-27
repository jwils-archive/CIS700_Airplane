package airplane.g2.waypoint.avoidance;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import airplane.g2.util.PointUtil;
import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;


public class AvoidByMoveFromMidpoint extends AvoidByMove {
	public AvoidByMoveFromMidpoint(PlaneIndex planeToMove, Double moveAmount) {
		super(planeToMove, moveAmount);
		// TODO Auto-generated constructor stub
	}

	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisionObject) {
		PlanePath outPath1 = new PlanePath(path1);
		PlanePath outPath2 = new PlanePath(path2);
		
		
		PlanePath targetPath = null;
		int waypointIndex = -1;
		if (planeToMove == PlaneIndex.PLANE_ONE) {
			targetPath = outPath1;
			waypointIndex = collisionObject.getPlane1segment();
		} else {
			targetPath = outPath2;
			waypointIndex = collisionObject.getPlane2segment();
		}
		
		Point2D.Double wayPoint = waypointBetweenWaypointAndColllisionPoint(
						targetPath.waypointAt(waypointIndex), collisionObject.getCollisionPoint());
		wayPoint = PointUtil.addPoints(wayPoint, moveAmount);
		if(shouldAdjustForBounds) wayPoint = adjustPointForBounds(wayPoint);
		
		targetPath.addWaypoint(waypointIndex + 1, wayPoint);
			
		return new PlanePath[]{outPath1, outPath2};
	}
	
	protected Point2D.Double waypointBetweenWaypointAndColllisionPoint(
			Point2D.Double waypoint, Point2D.Double collisionPt) {
		return PointUtil.midpoint(waypoint, collisionPt);
	}
}
