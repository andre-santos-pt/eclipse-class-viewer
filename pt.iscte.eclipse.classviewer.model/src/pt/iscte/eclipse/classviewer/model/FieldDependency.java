package pt.iscte.eclipse.classviewer.model;

public class FieldDependency extends Dependency {

	private Cardinality cardinality;

	public FieldDependency(JType source, JType target, Cardinality cardinality) {
		super(source, target, Kind.FIELD);
		this.cardinality = cardinality;
	}

}
