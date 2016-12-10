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
	
	public boolean isUnary() {
		return upperBound == 1;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lowerBound;
		result = prime * result + upperBound;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cardinality other = (Cardinality) obj;
		if (lowerBound != other.lowerBound)
			return false;
		if (upperBound != other.upperBound)
			return false;
		return true;
	}
}
