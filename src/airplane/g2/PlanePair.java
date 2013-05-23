package airplane.g2;

import airplane.sim.Plane;
import airplane.g2.util.PlaneUtil;

public class PlanePair {
	private Plane first;
	private Plane second;
	private int firstIndex;
	private int secondIndex;
	private double distance;
	
	public PlanePair(int firstIndex, Plane first, int secondIndex, Plane second, double distance) {
		setFirst(first);
		setSecond(second);
		setDistance(distance);
		setFirstIndex(firstIndex);
		setSecondIndex(secondIndex);
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
	
	public Boolean contains(Plane p) {
		return PlaneUtil.planesAreEqual(p, getFirst()) ||
				PlaneUtil.planesAreEqual(p, getSecond());
	}
	
	public Boolean contains(int index) {
		return index == getFirstIndex() || index == getSecondIndex();
	}
	
	
	public Integer planeNot(int index) {
		return index == getFirstIndex() ? getSecondIndex() : getFirstIndex();
	}
	
	public Plane planeNot(Plane p) {
		return PlaneUtil.planesAreEqual(p, getFirst()) ? getSecond() : getFirst();
	}
	
	public String toString() {
		return String.format("(%d: %s, %d: %s, %f)", getFirstIndex(), getFirst(), getSecondIndex(), getSecond(), getDistance());
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public void setFirstIndex(int firstIndex) {
		this.firstIndex = firstIndex;
	}

	public int getSecondIndex() {
		return secondIndex;
	}

	public void setSecondIndex(int secondIndex) {
		this.secondIndex = secondIndex;
	}
}
