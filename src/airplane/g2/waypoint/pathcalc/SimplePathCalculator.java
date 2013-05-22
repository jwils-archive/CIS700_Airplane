package airplane.g2.waypoint.pathcalc;

import java.awt.geom.Point2D;
import java.util.HashMap;

import airplane.g2.waypoint.PlanePath;
import airplane.sim.Plane;

public class SimplePathCalculator extends PathCalculator{

	//TODO Better method of adding waypoint.
	@Override
	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
		int i = 1;
		for (Plane p1 : waypointHash.keySet()) {
			for(Plane p2 : waypointHash.keySet()) {
				if (p1 != p2 && waypointHash.get(p1).getCollisionPoint(waypointHash.get(p2)) != null) {
					Point2D.Double collisionPoint = waypointHash.get(p1).getCollisionPoint(waypointHash.get(p2));
					double xMove = 10;
					double yMove = 10;
					if (p2.getX() < collisionPoint.x) {
						xMove = -10;
					}
					
					if (p2.getY() < collisionPoint.y) {
						yMove = -10;
					}
					
					i ++;
					
					waypointHash.get(p1).appendWaypoint(new Point2D.Double(collisionPoint.x + xMove, collisionPoint.y + yMove));
					waypointHash.get(p1).delay(4 * i);
				}
			}
			
		}
		
	}

}
