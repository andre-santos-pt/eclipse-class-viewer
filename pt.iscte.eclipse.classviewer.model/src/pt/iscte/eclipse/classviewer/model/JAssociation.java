package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.io.Serializable;

public class JAssociation extends StereotypedElement implements Serializable, IDependency {
	private static final long serialVersionUID = 1L;
	
	private JClass owner;
	private JType target;
	private String name;
	private Cardinality cardinality;
	
	public JAssociation(JClass owner, JType target, String name, Cardinality cardinality) {
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

	public JClass getOwner() {
		return owner;
	}
	
	@Override
	public JType getSourceType() {
		return owner;
	}

	@Override
	public JType getTargetType() {
		return target;
	}
}
