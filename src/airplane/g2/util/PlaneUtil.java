package airplane.g2.util;

import java.util.ArrayList;

import airplane.g2.PlanePair;
import airplane.sim.Plane;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class PlaneUtil {
	/**
	 * Returns the distance of the plane from its current location to its destination.
	 * 
	 * @param plane
	 * @return
	 */
	public static double distanceToDestination(Plane plane) {
		return plane.getLocation().distance(plane.getDestination());
	}
	
	/**
	 * Returns the distance between two planes.
	 * @param a
	 * @param b
	 * @return
	 */
	public static double distanceBetweenPlanes(Plane a, Plane b) {
		return a.getLocation().distance(b.getLocation());
	}
	
	/**
	 * Returns the distance from the plane's current location to its destination
	 * truncated to nearest integer.
	 * 
	 * @param plane
	 * @return The steps to the destination
	 */
	public static int stepsToDestination(Plane plane) {
		return (int) distanceToDestination(plane);
	}
	
	/**
	 * Returns the number of rounds until departure. 0 if the departure time has
	 * already passed.
	 * @param plane
	 * @param round
	 * @return
	 */
	public static int roundsUntilDeparture(Plane plane, int round) {
		int delta = plane.getDepartureTime() - round;
		return delta < 0 ? 0 : delta;
	}
	
	/**
	 * Returns the number of steps until departure, that is the number of rounds times
	 * the number of steps per rounds.
	 * @param plane
	 * @param round
	 * @return
	 */
	public static int stepsUntilDeparture(Plane plane, int round) {
		return roundsUntilDeparture(plane, round) * stepsPerRound();
	}
	
	/**
	 * Returns the number of steps to the destination factoring in the steps that 
	 * could've been taken if the plane had started moving immediately. 
	 * 
	 * @param plane
	 * @param round
	 * @return
	 */
	public static int stepsToDestinationIncludingDeparture(Plane plane, int round) {
		return stepsToDestination(plane) +
				stepsUntilDeparture(plane, round);
	}
	
	/**
	 * The number of steps that can be taken each round.
	 * @return Returns the number of steps that can be taken each round. 
	 */
	public static int stepsPerRound() {
		return 1;
	}
	
	/**
	 * Returns the plane that will land first given their current locations.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Plane getFirstToLand(Plane p1, Plane p2) {
		return stepsToDestination(p1) > stepsToDestination(p2) ? p2 : p1;
	}
		
	/**
	 * Returns the plane that will land first given the current plane location 
	 * and its departure time.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Plane getFirstToLandGivenDeparture(Plane p1, Plane p2, int round) {
		return stepsToDestinationIncludingDeparture(p1, round) > 
			stepsToDestinationIncludingDeparture(p2, round)? p2 : p1;
	}
	
	
	/**
	 * Returns a two-dimensional array of distances.
	 * 
	 * @param planes
	 * @return
	 */
	public static double[][] getDistances(ArrayList<Plane> planes) {
		int count = planes.size();
		double[][] distances = new double[count][count];
		
		for(int i = 0; i < count; i ++) {
			for(int j = i; j < count; j++) {
				distances[i][j] = (i == j) ? 
						0 : distanceBetweenPlanes(planes.get(i), planes.get(j));
				// mirrored
				distances[j][i] = distances[i][j];
			}
		}
		return distances;
	}
	
	/**
	 * Returns a list of pairs of colliding planes.
	 * 
	 * @param planes
	 * @param distanceThreshold
	 * @return
	 */
	public static ArrayList<PlanePair> detectCollisions(ArrayList<Plane> planes, double distanceThreshold) {
		double[][] distances = getDistances(planes);
		int count = planes.size();
		ArrayList<PlanePair> collisionPairs = new ArrayList<PlanePair>();
		for(int i = 0; i < count; i ++) {
			for(int j = i + 1; j < count; j++) {
				double dist = distances[i][j];
				if(dist > distanceThreshold) continue;
				
				collisionPairs.add(new PlanePair(i, planes.get(i), j, planes.get(j), dist));
			}
		}
		return collisionPairs;
	}	
	
	public static double normalizedBearing(double bearing) {
		return (bearing + 360) % 360;
	}
	
	public static double bearingAway(Plane planeToAvoid, Plane planeThatAvoids, double deltaBearing) {
		return bearingAway(planeToAvoid.getBearing(), planeThatAvoids.getBearing(), deltaBearing);
	}
	
	public static double bearingAway(double bearingToAvoid, double currentBearing, double deltaBearing) {
		double bearingA = normalizedBearing(currentBearing + deltaBearing);
		double bearingB = normalizedBearing(currentBearing - deltaBearing);
		
		double diffA = normalizedBearing(bearingToAvoid - bearingA);
		double diffB = normalizedBearing(bearingToAvoid - bearingB);
		
		return diffA > diffB ? bearingA : bearingB;
	}
	
	public static Point2D.Double planeOrigin(Plane a) {
		return a.getHistory().isEmpty() ? null : a.getHistory().get(0);
	}
	
	public static Boolean planesAreEqual(Plane a, Plane b) {
		return a.id == b.id;
	}
	
	public static Point2D.Double midpointBetweenPlanes(Plane a, Plane b) {
		return PointUtil.midpoint(a.getLocation(), b.getLocation());
	}
}
