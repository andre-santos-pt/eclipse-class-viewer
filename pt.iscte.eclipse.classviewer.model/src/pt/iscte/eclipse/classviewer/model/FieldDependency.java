package pt.iscte.eclipse.classviewer.model;

public class FieldDependency extends Dependency {

	private final String fieldName;
	private final Cardinality cardinality;

	public FieldDependency(JType source, JType target, String fieldName, Cardinality cardinality) {
		super(source, target, Kind.FIELD);
		this.fieldName = fieldName;
		this.cardinality = cardinality;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public Cardinality getCardinality() {
		return cardinality;
	}

}
