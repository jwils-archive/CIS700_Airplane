package airplane.g2.waypoint.avoidance;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import airplane.g2.util.PointUtil;
import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;

public class AvoidByRandomPoint extends AvoidByMove {

	public AvoidByRandomPoint(PlaneIndex planeToMove, Double moveAmount) {
		super(planeToMove, moveAmount);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisionObject) {
		PlanePath outPath1 = new PlanePath(path1);
		PlanePath outPath2 = new PlanePath(path2);
		
		Point2D.Double wayPoint = moveAmount;
		
		
		if (planeToMove == PlaneIndex.PLANE_ONE) {
			outPath1.addWaypoint(collisionObject.getPlane1segment() + 1, wayPoint); 
		} else {
			outPath2.addWaypoint(collisionObject.getPlane2segment() + 1, wayPoint);
		}
		return new PlanePath[]{outPath1, outPath2};
	}

}
