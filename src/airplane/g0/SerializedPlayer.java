package airplane.g0;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import airplane.sim.Plane;



public class SerializedPlayer extends airplane.sim.Player {

	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	
	@Override
	public String getName() {
		return "Serialized Player";
	}
	

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		logger.info("Starting new game!");

	}
	
	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
				
		// if any plane is in the air, then just keep things as-is
		for (Plane p : planes) {
		    if (p.getBearing() != -1 && p.getBearing() != -2) return bearings;
		}

		// if no plane is in the air, find the one with the earliest 
		// departure time and move that one in the right direction
		int minTime = 10000;
		int minIndex = 10000;
		for (int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
		    if (p.getDepartureTime() < minTime && p.getBearing() == -1) {
				minIndex = i;
				minTime = p.getDepartureTime();
		    }
		}
		
		// if it's not too early, then take off!
		if (round >= minTime) {
		    Plane p = planes.get(minIndex);
		    bearings[minIndex] = calculateBearing(p.getLocation(), p.getDestination());
		}
		
		
		return bearings;
	}


}
