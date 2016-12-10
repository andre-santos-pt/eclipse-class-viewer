package pt.iscte.eclipse.classviewer.model;

public class Cardinality {

	private final int lowerBound;
	private final int upperBound;
	
	private Cardinality(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public static Cardinality zeroOne() {
		return new Cardinality(0, 1);
	}
	
	public static Cardinality zeroMany() {
		return new Cardinality(0, -1);
	}
	
	public static Cardinality zeroTo(int upperBound) {
		if(upperBound < 1)
			throw new IllegalArgumentException();
		return new Cardinality(0, -1);
	}
	
	public static Cardinality one() {
		return new Cardinality(1, 1);
	}
	
	public static Cardinality oneMany() {
		return new Cardinality(1, -1);
	}
	
	public static Cardinality oneTo(int upperBound) {
		if(upperBound < 1)
			throw new IllegalArgumentException();
		return new Cardinality(1, upperBound);
	}
	
	@Override
	public String toString() {
		if(lowerBound == 0 && upperBound == 1)
			return "?";
		else if(lowerBound == 0 && upperBound == -1)
			return "*";
		else if(lowerBound == 1 && upperBound == 1)
			return "1";
		else if(lowerBound == 1 && upperBound == -1)
			return "+";
		else
			return lowerBound + ".." + (upperBound == -1 ? "*" : upperBound);
	}
}
