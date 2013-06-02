package airplane.g2;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import airplane.g2.util.PlaneUtil;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.waypoint.pathcalc.LockPathCalculator;
import airplane.g2.waypoint.pathcalc.PathCalculator;
import airplane.g2.waypoint.pathcalc.SimplePathCalculator;
import airplane.sim.Plane;

public class Group2WaypointPlayer extends airplane.sim.Player {
	
	HashMap<Plane, PlanePath> waypointHash;
	PathCalculator pathCalculator = new LockPathCalculator();
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public String getName() {
		return "Waypoint Player";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		waypointHash = new HashMap<Plane, PlanePath>();
		int id = 0;
		
		for (Plane plane : planes) {
			waypointHash.put(plane, new PlanePath(plane));
			plane.id = id;
			id++;
		}
		pathCalculator.calculatePaths(waypointHash);
	}

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round,
			double[] bearings) {
		for (int i = 0; i < planes.size(); i++) { 
			if (bearings[i] == -2) continue;
			PlanePath path = waypointHash.get(planes.get(i));
			double newBearing = path.getBearing(round);
			
			// override the bearing if it has been going for more than 1000 cycles and
			// it is very close to the destination
			Plane p = path.getPlane();
			if(round - p.getDepartureTime() > 500 &&
					PlaneUtil.distanceToDestination(path.getPlane()) < 5) {
				logger.info(String.format("Overriding plane %f from destination", PlaneUtil.distanceToDestination(path.getPlane())));
				newBearing = PlaneUtil.normalizedBearing(
						calculateBearing(p.getLocation(), p.getDestination()));
			}
			
			
//			if (waypointHash.get(planes.get(i)).getPosition(1).x > 70 &&  waypointHash.get(planes.get(i)).getPosition(1).y > 70) {
//				logger.error("Bearing should be =" + newBearing + " at round " + round);
//			}
			double adjust = 9.99;
			if (bearings[i] >= 0 && Math.abs(newBearing - bearings[i]) > adjust) {
				if ( (newBearing > bearings[i] && newBearing - bearings[i] < 180) || (newBearing < bearings[i] && newBearing - bearings[i] > 180)) {
					bearings[i] = (bearings[i] + adjust) % 360;
				} else {
					bearings[i] = (bearings[i] - adjust + 360) % 360;
				}
			} else {
				bearings[i] = newBearing;
			}
			//logger.error("bearing set to " + bearings[i]);
		}
		return bearings;
	}
}
