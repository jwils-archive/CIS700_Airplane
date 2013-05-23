package airplane.g2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import airplane.g2.util.PlaneUtil;
import airplane.sim.Plane;

public class PlaneInfo extends Plane {
	private int index;
	private Plane originalPlane;
	
	public PlaneInfo(int index, Plane p) {
		super(p);
		setIndex(index);
		setOriginalPlane(p);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int stepsToDestinationIncludingDeparture(int round) {
		return PlaneUtil.stepsToDestinationIncludingDeparture(this, round);
	}
	
	public static ArrayList<PlaneInfo> toPlaneInfo(ArrayList<Plane> planes) {
		ArrayList<PlaneInfo> info = new ArrayList<PlaneInfo>();
		for(int i = 0, count = planes.size(); i < count; i ++) {
			info.add(new PlaneInfo(i, planes.get(i)));
		}
		return info;
	}
	
	public static ArrayList<PlaneInfo> sortedByStepsToDestinationIncludingDeparture(ArrayList<PlaneInfo> planes, final int round) {
		ArrayList<PlaneInfo> sortedPlanes = new ArrayList<PlaneInfo>(planes);
		Collections.sort(sortedPlanes, new Comparator<PlaneInfo>() {
			public int compare(PlaneInfo a, PlaneInfo b) {
				Integer aSteps = a.stepsToDestinationIncludingDeparture(round);
				Integer bSteps = b.stepsToDestinationIncludingDeparture(round);
				return aSteps.compareTo(bSteps);
			}
		});
		return sortedPlanes;
	}

	public Plane getOriginalPlane() {
		return originalPlane;
	}

	public void setOriginalPlane(Plane originalPlane) {
		this.originalPlane = originalPlane;
	}
}
