package airplane.g2.waypoint;

import java.awt.geom.Point2D;

public class PlanePathPosition {
	private Point2D.Double position;
	private int segment;
	public Point2D.Double getPosition() {
		return position;
	}
	public void setPosition(Point2D.Double position) {
		this.position = position;
	}
	public int getSegment() {
		return segment;
	}
	public void setSegment(int segment) {
		this.segment = segment;
	}
}
