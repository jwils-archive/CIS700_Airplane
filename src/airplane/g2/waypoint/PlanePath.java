package airplane.g2.waypoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import airplane.sim.Plane;

public class PlanePath {
	private Logger logger = Logger.getLogger(this.getClass());
	double INTERSECTION_DISTANCE = 5;
	
	ArrayList<Point2D.Double> waypoints;
	Plane plane;
	int startTimestep;
	
	public PlanePath(Plane p) {
		startTimestep = p.getDepartureTime();
		
		waypoints = new ArrayList<Point2D.Double>();
		waypoints.add(p.getLocation());
		waypoints.add(p.getDestination());
		
		plane = p;		
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
	}
	
	public int getStartTimestep() {
		return startTimestep;
	}
	
	public Point2D.Double getCollisionPoint(PlanePath otherPath) {
		for (int i = Math.max(startTimestep, otherPath.getStartTimestep()); i <= Math.min(getArrivalStep(), otherPath.getArrivalStep()); i++) {
			Point2D.Double thisPos = getPosition(i);
			Point2D.Double otherPos = otherPath.getPosition(i);
			logger.warn(thisPos);
			logger.warn(i);
			if (thisPos.distance(otherPos) < INTERSECTION_DISTANCE) {
				logger.warn(thisPos);
				logger.warn(i);
				return thisPos;
			}
		}
		return null;
	}
	
	public int getArrivalStep() {
		int totaltime = startTimestep;
		int segment = 0;
		
		double distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		while(segment < waypoints.size() - 2) {
			totaltime += distance;
			segment++;
			distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		}
		
		return (int) (totaltime + distance) + 1;
	}
	
	
	//TODO Get this working.
	private void smoothCurves() {
		int currentIndex = 1;
		while (currentIndex < waypoints.size() - 1) {
			
			double bearingIn = calculateBearing(waypoints.get(currentIndex - 1), waypoints.get(currentIndex));
			double bearingOut = calculateBearing(waypoints.get(currentIndex), waypoints.get(currentIndex + 1));
			
			double deltaBearing = Math.abs(bearingIn - bearingOut);
			
			if (Plane.MAX_BEARING_CHANGE >  deltaBearing) {
				double halfDeltaBearing = deltaBearing/2 + 5;
				
				double bearingInTemp = halfDeltaBearing;
				Point2D.Double endPoint = waypoints.get(currentIndex);
				Point2D.Double startPoint = waypoints.get(currentIndex);
				
				while(bearingInTemp > plane.MAX_BEARING_CHANGE) {
					double dx = endPoint.x - startPoint.x;
					double dy = endPoint.y -startPoint.y;
					
					
					
					double norm = Math.sqrt(dx*dx + dy*dy);
					
					dx = dx/norm;
					dy = dy/norm;
					
					//addWaypoint(currentIndex, Point2D.Double())
				}
				
				//TODO
			}
			
			
		}
	}
	
	public double getBearing(int timestep) {
		if (timestep < startTimestep) return -1;
		if (timestep > getArrivalStep()) return calculateBearing(plane.getLocation(), waypoints.get(waypoints.size() - 1));
		int savedTime = startTimestep;
		int segment = 0;
		
		double distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		while(savedTime + distance + 0.5 < timestep && segment < waypoints.size() - 2) {
			savedTime += distance;
			segment++;
			distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		}
		
		return calculateBearing(plane.getLocation(), waypoints.get(segment + 1));
		
	}
	
	
	public Point2D.Double getPosition(int timestep) {
		if (timestep < startTimestep) return null;
		if (timestep > getArrivalStep() + 1) return null;
		int savedTime = startTimestep;
		int segment = 0;
		
		double distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		while(savedTime + distance + 0.5 < timestep && segment < waypoints.size() - 2) {
			savedTime += distance;
			segment++;
			distance = waypoints.get(segment).distance(waypoints.get(segment + 1));
		}
		
		double dx = waypoints.get(segment + 1).x - waypoints.get(segment).x;
		double dy = waypoints.get(segment + 1).y - waypoints.get(segment).y;
		
		double norm = Math.sqrt(dx*dx + dy*dy);
		
		dx = dx/norm;
		dy = dy/norm;
		
		return new Point2D.Double(waypoints.get(segment).x + dx * (timestep - savedTime), waypoints.get(segment).y + dy * (timestep - savedTime));
	}

	/*
	 * This is a helper method that will calculate the bearing between two points
	 */
    private static double calculateBearing(Point2D.Double p1, Point2D.Double p2) {

		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;

		double bearing = Math.atan2(dy, dx) * 180 / Math.PI - 90;

		if (bearing < 0) bearing += 360;

		return bearing;
    }
}
