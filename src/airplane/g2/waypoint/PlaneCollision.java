package airplane.g2.waypoint;

import java.awt.geom.Point2D;


public class PlaneCollision {
	private PlanePath path1;
	private PlanePath path2;
	private int plane1segment;
	private int plane2segment;
	private int round;
	
	private Point2D.Double collisionPoint;
	
	public String toString() {
		String output = "";
		output += "Path1: " + path1;
		output += "Path2: " + path2;
		output += "round: " + round;
		return output;
	}
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

	public PlanePath getPath1() {
		return path1;
	}

	public void setPath1(PlanePath path1) {
		this.path1 = path1;
	}

	public PlanePath getPath2() {
		return path2;
	}

	public void setPath2(PlanePath path) {
		this.path2 = path;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
	
	public Integer getPlaneId1() {
		return getPath1().getPlaneId();
	}
	public Integer getPlaneId2() {
		return getPath2().getPlaneId();
	}
	
}
