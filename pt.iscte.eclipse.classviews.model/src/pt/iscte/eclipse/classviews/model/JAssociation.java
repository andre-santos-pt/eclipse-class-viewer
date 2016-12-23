package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.io.Serializable;

public class JAssociation extends StereotypedElement implements Serializable, IDependency {
	private static final long serialVersionUID = 1L;
	
	private VClass owner;
	private VType target;
	private String name;
	private Cardinality cardinality;
	
	public JAssociation(VClass owner, VType target, String name, Cardinality cardinality) {
		super(name);
		checkNotNull(owner, target);
		owner.addAssociation(this);
		this.owner = owner;
		this.target = target;
		this.name = name;
		this.cardinality = cardinality;
	}
	
	public String getName() {
		return name;
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
		return target;
	}
}
