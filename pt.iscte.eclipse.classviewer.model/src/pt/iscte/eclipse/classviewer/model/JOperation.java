package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JOperation extends NamedElement {

	private static final long serialVersionUID = 1L;

	private JType owner;
	private Visibility visibility;
	private boolean isAbstract;
	private boolean isStatic;
	private List<JType> params;
	private Set<JOperation> deps;
	private JType returnType;
	
	public JOperation(JType owner, String name, JType ... params) {
		super(name);
		checkNotNull(owner);
		checkNotNull(name);
		this.owner = owner;
		owner.addOperation(this);
		visibility = Visibility.PUBLIC;
		isAbstract = false;
		isStatic = false;
		this.params = new ArrayList<>(params.length);
		for(JType p : params) {
			checkNotNull(p);
			this.params.add(p);
		}
		
		deps = new HashSet<>();
	}
	
	public JType getOwner() {
		return owner;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility v) {
		checkNotNull(v);
		visibility = v;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean isStatic() {
		return isStatic;
	}
	
	public void addDependency(JOperation o) {
		checkNotNull(o);
		deps.add(o);
	}
	
	public Set<JOperation> getDependencies() {
		return Collections.unmodifiableSet(deps);
	}
	JOperation copyTo(JType type) {
		// TODO copy deps
		JOperation op = new JOperation(type, getName());
		op.visibility = visibility;
		op.isAbstract = isAbstract;
		op.isStatic = isStatic;
		op.returnType = returnType;
		return op;
	}
	
	
	// TODO
	@Override
	boolean equalsInternal(Object obj) {
		return super.equalsInternal(obj);
	}
	
	@Override
	int hashCodeInternal() {
		return super.hashCodeInternal();
	}
	
	@Override
	public String toString() {
		return owner.getName() + "." + getName() + "(...)";
	}
}

