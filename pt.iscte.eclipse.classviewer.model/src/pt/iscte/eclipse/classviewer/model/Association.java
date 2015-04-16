package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.io.Serializable;

public class Association implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private JClass owner;
	private JType target;
	private boolean unary;
	
	public Association(JClass owner, JType target, boolean unary) {
		checkNotNull(owner);
		checkNotNull(target);
		owner.addAssociation(this);
		this.owner = owner;
		this.target = target;
		this.name = "";
		this.unary = unary;
	}
	
	public JClass getOwner() {
		return owner;
	}
	
	public JType getTarget() {
		return target;
	}

	public void setName(String name) {
		checkNotNull(name);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isUnary() {
		return unary;
	}
}
