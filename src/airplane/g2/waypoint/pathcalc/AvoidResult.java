package airplane.g2.waypoint.pathcalc;

import airplane.g2.waypoint.PlaneCollision;
import airplane.g2.waypoint.PlanePath;
import airplane.g2.waypoint.avoidance.AvoidMethod;

public class AvoidResult {
	private PlanePath[] paths;
	private AvoidMethod avoidMethod;
	private int steps;
	private PlaneCollision previousCollision;
	private PlaneCollision nextCollision;
	private PlaneCollision nextGlobalCollision;
	private Double heuristicValue;
	
	public AvoidResult(AvoidMethod avoidMethod, PlanePath[] paths, 
			int steps, 
			PlaneCollision previousCollision, 
			PlaneCollision nextCollision,
			PlaneCollision nextGlobalCollision) {
		setSteps(steps);
		setAvoidMethod(avoidMethod);
		setPaths(paths);
		setPreviousCollision(previousCollision);
		setNextCollision(nextCollision);
	}
	
	public PlanePath[] getPaths() {
		return paths;
	}
	public void setPaths(PlanePath[] paths) {
		this.paths = paths;
	}
	public AvoidMethod getAvoidMethod() {
		return avoidMethod;
	}
	public void setAvoidMethod(AvoidMethod avoidMethod) {
		this.avoidMethod = avoidMethod;
	}
	public int getSteps() {
		return steps;
	}
	public void setSteps(int steps) {
		this.steps = steps;
	}

	public PlaneCollision getNextCollision() {
		return nextCollision;
	}

	public void setNextCollision(PlaneCollision nextCollision) {
		this.nextCollision = nextCollision;
	}

	public PlaneCollision getPreviousCollision() {
		return previousCollision;
	}

	public void setPreviousCollision(PlaneCollision previousCollision) {
		this.previousCollision = previousCollision;
	}
	
	public Boolean isBetterThanPreviousCollision() {
		return getNextCollision() == null ||
				getPreviousCollision().getRound() < getNextCollision().getRound();
	}

	public Double getHeuristicValue() {
		return heuristicValue;
	}

	public void setHeuristicValue(Double heuristicValue) {
		this.heuristicValue = heuristicValue;
	}
	
	public String toString() {
		return String.format("AvoidResult (Steps: %d, Heuristic: %f) %s", steps, getHeuristicValue(), getAvoidMethod().toString());
	}

	public PlaneCollision getNextGlobalCollision() {
		return nextGlobalCollision;
	}

	public void setNextGlobalCollision(PlaneCollision nextGlobalCollision) {
		this.nextGlobalCollision = nextGlobalCollision;
	}
}
