package airplane.g2.waypoint;

import java.awt.geom.Point2D;


public class PlaneCollision {
	PlanePath plane1;
	PlanePath plane2;
	private int plane1segment;
	private int plane2segment;
	
	private Point2D.Double collisionPoint;
	
	public Point2D.Double getCollisionPoint() {
		return collisionPoint;
	}
	
	public void setCollisionPoint(Point2D.Double collisionPoint) {
		this.collisionPoint = collisionPoint;
	}
	
	public int getPlane2segment() {
		return plane2segment;
	}
	
	public void setPlane2segment(int plane2segment) {
		this.plane2segment = plane2segment;
	}
	
	public int getPlane1segment() {
		return plane1segment;
	}
	
	public void setPlane1segment(int plane1segment) {
		this.plane1segment = plane1segment;
	}
	
	
}
