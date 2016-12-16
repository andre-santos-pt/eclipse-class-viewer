package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

public final class JField extends StereotypedElement implements IDependency, IStaticProperty {
	private static final long serialVersionUID = 1L;
	
	private final JClass owner;
	private final JType type;
	private final Cardinality cardinality;
	private boolean isStatic;
	private Visibility visibility;
	
	public JField(JClass owner, String name, JType type) {
		this(owner, name, type, Cardinality.zeroOne());
	}
	
	public JField(JClass owner, String name, JType type, Cardinality cardinality) {
		super(name);
		checkNotNull(owner, name, type);
		this.owner = owner;
		this.type = type;
		this.cardinality = cardinality;
		isStatic = false;
		visibility = Visibility.PRIVATE;
		owner.addField(this);
	}
	
	public JType getType() {
		return type;
	}
	
	public JField setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return this;
	}
	
	@Override
	public boolean isStatic() {
		return isStatic;
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

	@Override
	public JType getSourceType() {
		return owner;
	}

	@Override
	public JType getTargetType() {
		return type;
	}
	
	@Override
	public String toString() {
		return owner.getName() + "." + getName();
	}
}
