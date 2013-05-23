package airplane.g2.waypoint.avoidance;


import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;

public abstract class AvoidMethod {
	public enum PlaneIndex {PLANE_ONE, PLANE_TWO}
	
	public abstract PlanePath[] avoid(PlanePath path1, PlanePath path2, PlaneCollision collisionObject);

}
