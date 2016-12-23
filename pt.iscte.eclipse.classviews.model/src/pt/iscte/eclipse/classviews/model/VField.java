package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

public final class VField extends ClassMember implements IDependency {
	private static final long serialVersionUID = 1L;
	
	private final VClass owner;
	private final VType type;
	private final Cardinality cardinality;
	
	public VField(VClass owner, String name, VType type) {
		this(owner, name, type, Cardinality.zeroOne());
	}
	
	public VField(VClass owner, String name, VType type, Cardinality cardinality) {
		super(name);
		checkNotNull(owner, name, type);
		this.owner = owner;
		this.type = type;
		this.cardinality = cardinality;
		owner.addField(this);
	}
	
	public VType getType() {
		return type;
	}

	public Cardinality getCardinality() {
		return cardinality;
	}
	
	public VClass getOwner() {
		return owner;
	}

	@Override
	public VType getSourceType() {
		return owner;
	}

	@Override
	public VType getTargetType() {
		return type;
	}
	
	@Override
	public String toString() {
		return owner.getName() + "." + getName();
	}
}
