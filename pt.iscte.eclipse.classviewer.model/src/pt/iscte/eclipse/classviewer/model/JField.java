package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

public final class JField extends StereotypedElement {
	private static final long serialVersionUID = 1L;
	
	private final JClass owner;
	private final JType type;
	private final Cardinality cardinality;
	private Visibility visibility;
	
	public JField(JClass owner, String name, JType type, Cardinality cardinality) {
		super(name);
		checkNotNull(owner, name, type);
		this.owner = owner;
		this.type = type;
		this.cardinality = cardinality;
		visibility = Visibility.PRIVATE;
		owner.addField(this);
	}
	
	public JType getType() {
		return type;
	}
	
	public Cardinality getCardinality() {
		return cardinality;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}
	
	public JClass getOwner() {
		return owner;
	}
	
	
}
