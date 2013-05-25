package airplane.g2.waypoint.avoidance;

import java.awt.geom.Point2D;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.util.PointUtil;

public class AvoidByMove extends AvoidMethod {
	PlaneIndex planeToMove;
	Point2D.Double moveAmount;
	Boolean shouldAdjustForBounds = true;
	
	//TODO change these
	double minX = 6;
	double minY = 6;
	double maxX = 94;
	double maxY = 94;
	
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
		if(shouldAdjustForBounds) wayPoint = adjustPointForBounds(wayPoint);
		
		if (planeToMove == PlaneIndex.PLANE_ONE) {
			outPath1.addWaypoint(collisionObject.getPlane1segment() + 1, wayPoint); 
		} else {
			outPath2.addWaypoint(collisionObject.getPlane2segment() + 1, wayPoint);
		}
		return new PlanePath[]{outPath1, outPath2};
	}
	
	protected double bound(double num, double min, double max) {
		return Math.max(Math.min(num, max), min);
	}
	
	protected Point2D.Double adjustPointForBounds(Point2D.Double wayPoint) {
		return new Point2D.Double(
				bound(wayPoint.x, minX, maxX), 
				bound(wayPoint.y, minY, maxY));
	}
}
