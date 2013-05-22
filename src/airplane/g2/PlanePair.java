package airplane.g2;

import airplane.sim.Plane;

public class PlanePair {
	private Plane first;
	private Plane second;
	private double distance;
	
	public PlanePair(Plane first, Plane second, double distance) {
		setFirst(first);
		setSecond(second);
		setDistance(distance);
	}

	public Plane getFirst() {
		return first;
	}

	public void setFirst(Plane first) {
		this.first = first;
	}

	public Plane getSecond() {
		return second;
	}

	public void setSecond(Plane second) {
		this.second = second;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
}
