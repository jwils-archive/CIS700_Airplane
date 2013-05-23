package airplane.g2.waypoint.avoidance;

import java.awt.geom.Point2D;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.util.PointUtil;

public class AvoidByMove extends AvoidMethod {
	PlaneIndex planeToMove;
	Point2D.Double moveAmount;
	
	public AvoidByMove(PlaneIndex planeToMove, Point2D.Double moveAmount) {
		this.planeToMove = planeToMove;
		this.moveAmount = moveAmount;
	}

	@Override
	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisionObject) {
		PlanePath outPath1 = new PlanePath(path1);
		PlanePath outPath2 = new PlanePath(path2);
		
		Point2D.Double wayPoint = PointUtil.addPoints(collisionObject.getCollisionPoint(), moveAmount);
		
		if (planeToMove == PlaneIndex.PLANE_ONE) {
			outPath1.addWaypoint(collisionObject.getPlane1segment(), wayPoint); 
		} else {
			outPath2.addWaypoint(collisionObject.getPlane2segment(), wayPoint);
		}
		return new PlanePath[]{outPath1, outPath2};
	}
	

}
