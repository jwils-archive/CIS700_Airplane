package airplane.g2.waypoint.avoidance;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;

public class AvoidByDelay extends AvoidMethod {
	PlaneIndex planeToDelay;
	int delayAmount;
	
	public AvoidByDelay(PlaneIndex planeToDelay, int delayAmount) {
		this.planeToDelay = planeToDelay;
		this.delayAmount = delayAmount;
	}

	@Override
	public PlanePath[] avoid(PlanePath path1, PlanePath path2,
			PlaneCollision collisonObject) {
		PlanePath outPath1 = new PlanePath(path1);
		PlanePath outPath2 = new PlanePath(path2);
		
		if (planeToDelay == PlaneIndex.PLANE_ONE) {
			outPath1.delay(delayAmount);
		} else {
			outPath2.delay(delayAmount);
		}
		return new PlanePath[]{outPath1, outPath2};
	}

}
