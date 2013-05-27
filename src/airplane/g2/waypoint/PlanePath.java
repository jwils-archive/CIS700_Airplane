package airplane.g2.waypoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import airplane.sim.Plane;

public class PlanePath {

	double INTERSECTION_DISTANCE = 5.1;
	private Logger logger = Logger.getLogger(this.getClass());

	ArrayList<Point2D.Double> waypoints;
	private Plane plane;
	int startTimestep;

	public PlanePath(Plane p) {
		startTimestep = p.getDepartureTime();

		waypoints = new ArrayList<Point2D.Double>();
		waypoints.add(p.getLocation());
		waypoints.add(p.getDestination());

		setPlane(p);
	}

	public PlanePath(PlanePath oldPath) {
		plane = oldPath.getPlane();
		startTimestep = oldPath.getStartTimestep();
		waypoints = new ArrayList<Point2D.Double>(oldPath.waypoints);
	}

	public void delay(int amount) {
		startTimestep += amount;
	}

	public String toString() {
		String output = "\nPlane path:\n";
		for (Point2D.Double point : waypoints) {
			output += point + "\n";
		}
		output += "\n";
		return output;
	}

	public void appendWaypoint(Point2D.Double point) {
		addWaypoint(waypoints.size() - 1, point);
	}

	public void addWaypoint(int index, Point2D.Double point) {
		waypoints.add(index, point);
		smoothCurves(index);
	}

	public int getStartTimestep() {
		return startTimestep;
	}

	public PlaneCollision getPlaneCollision(PlanePath otherPath) {
		PlaneCollision collision = new PlaneCollision();
		collision.setPath1(this);
		collision.setPath2(otherPath);
		for (int i = Math.max(startTimestep, otherPath.getStartTimestep()); i <= Math
				.min(getArrivalStep(), otherPath.getArrivalStep()); i++) {
			PlanePathPosition thisPathPos = getPathPosition(i);
			PlanePathPosition otherPathPos = otherPath.getPathPosition(i);
			Point2D.Double thisPos = thisPathPos.getPosition();
			Point2D.Double otherPos = otherPathPos.getPosition();

			if (thisPos.distance(otherPos) < INTERSECTION_DISTANCE) {
				collision.setRound(i);
				collision.setCollisionPoint(thisPos);
				collision.setPlane1segment(thisPathPos.getSegment());
				collision.setPlane2segment(otherPathPos.getSegment());

				return collision;
			}
		}
		return null;
	}

	public Point2D.Double getCollisionPoint(PlanePath otherPath) {
		return getPlaneCollision(otherPath).getCollisionPoint();
	}

	public int getArrivalStep() {
		int totaltime = startTimestep;
		int segment = 0;

		double distance = waypoints.get(segment).distance(
				waypoints.get(segment + 1));
		while (segment < waypoints.size() - 2) {
			totaltime += distance;
			segment++;
			distance = waypoints.get(segment).distance(
					waypoints.get(segment + 1));
		}

		return (int) (totaltime + distance) + 1;
	}

	private int addFrontSmooth(int index, double bearingIn,
			double halfDeltaBearing) {
		Point2D.Double endPoint = waypoints.get(index);
		if (halfDeltaBearing > 0) {
			halfDeltaBearing -= 9.5;
		} else {
			halfDeltaBearing += 9.5;
		}

		double radialBearing = (bearingIn + halfDeltaBearing + 360) % 360;
		radialBearing = (radialBearing - 90) * Math.PI / 180;
		double newx = endPoint.x - (Math.cos(radialBearing) * 1);
		double newy = endPoint.y - (Math.sin(radialBearing) * 1);

		endPoint = new Point2D.Double(newx, newy);
		waypoints.add(index, endPoint);
		if (Math.abs(halfDeltaBearing) > 9.5) {
			return addFrontSmooth(index, bearingIn, halfDeltaBearing) + 1;
		} else {
			return 2;
		}

	}

	private void addAfterSmoothing(int index, double bearingOut,
			double halfDeltaBearing, double current) {
		Point2D.Double endPoint = waypoints.get(index-1);
		if (halfDeltaBearing > 5) {
			current += 9.5;
		} else {
			current -= 9.5;
		}

		double radialBearing = (bearingOut - halfDeltaBearing + current + 360) % 360;

		radialBearing = (radialBearing - 90) * Math.PI / 180;
		double newx = endPoint.x + (Math.cos(radialBearing) * 1);
		double newy = endPoint.y + (Math.sin(radialBearing) * 1);

		endPoint = new Point2D.Double(newx, newy);
		waypoints.add(index, endPoint);
		if (Math.abs(halfDeltaBearing - current) > 9.5) {
			addAfterSmoothing(index + 1, bearingOut, halfDeltaBearing, current);
		}

	}

	// TODO Get this working.
	private void smoothCurves(int index) {
		double bearingIn = calculateBearing(waypoints.get(index - 1),
				waypoints.get(index));
		double bearingOut = calculateBearing(waypoints.get(index),
				waypoints.get(index + 1));

		double deltaBearing = (bearingOut - bearingIn) % 180;
		if (Plane.MAX_BEARING_CHANGE < Math.abs(deltaBearing)) {
			double halfDeltaBearing = deltaBearing / 2;
			
			if (Math.abs(halfDeltaBearing) < 10) {
				return;
			}
			if (halfDeltaBearing > 0) {
				halfDeltaBearing += 5;
			} else {
				halfDeltaBearing -= 5;
			}

			index += addFrontSmooth(index, bearingIn, halfDeltaBearing);
			addAfterSmoothing(index, bearingOut, halfDeltaBearing, 0);
		}
	}

	public double getBearing(int timestep) {
		if (timestep < startTimestep)
			return -1;
		if (timestep > getArrivalStep())
			return calculateBearing(getPlane().getLocation(),
					waypoints.get(waypoints.size() - 1));
		int savedTime = startTimestep;
		int segment = 0;

		double distance = waypoints.get(segment).distance(
				waypoints.get(segment + 1));
		while (savedTime + distance + 0.5 < timestep
				&& segment < waypoints.size() - 2) {
			savedTime += distance;
			segment++;
			distance = waypoints.get(segment).distance(
					waypoints.get(segment + 1));
		}
		return calculateBearing(getPlane().getLocation(),
				waypoints.get(segment + 1));

	}

	public PlanePathPosition getPathPosition(int timestep) {
		if (timestep < startTimestep)
			return null;
		if (timestep > getArrivalStep() + 1)
			return null;
		int savedTime = startTimestep;
		int segment = 0;

		double distance = waypoints.get(segment).distance(
				waypoints.get(segment + 1));
		while (savedTime + distance + 0.5 < timestep
				&& segment < waypoints.size() - 2) {
			savedTime += distance;
			segment++;
			distance = waypoints.get(segment).distance(
					waypoints.get(segment + 1));
		}

		double dx = waypoints.get(segment + 1).x - waypoints.get(segment).x;
		double dy = waypoints.get(segment + 1).y - waypoints.get(segment).y;

		double norm = Math.sqrt(dx * dx + dy * dy);

		dx = dx / norm;
		dy = dy / norm;

		PlanePathPosition pos = new PlanePathPosition();
		pos.setPosition(new Point2D.Double(waypoints.get(segment).x + dx
				* (timestep - savedTime), waypoints.get(segment).y + dy
				* (timestep - savedTime)));
		pos.setSegment(segment);
		return pos;
	}

	public Point2D.Double getPosition(int timestep) {
		PlanePathPosition pos = getPathPosition(timestep);
		return pos == null ? null : pos.getPosition();
	}

	/*
	 * This is a helper method that will calculate the bearing between two
	 * points
	 */
	private static double calculateBearing(Point2D.Double p1, Point2D.Double p2) {

		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;

		double bearing = Math.atan2(dy, dx) * 180 / Math.PI - 90;

		if (bearing < 0)
			bearing += 360;

		return bearing;
	}

	public Plane getPlane() {
		return plane;
	}

	public void setPlane(Plane plane) {
		this.plane = plane;
	}
}
