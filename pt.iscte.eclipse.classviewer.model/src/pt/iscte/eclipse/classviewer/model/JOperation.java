package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
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
	private JType returnType;
	
	private List<CallDependency> callDependencies;
	
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
		
		callDependencies = Collections.emptyList();
	}
	
	public JOperation setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return this;
	}
	
	public JOperation setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
		return this;
	}
	
	public JOperation setVisibility(Visibility v) {
		checkNotNull(v);
		visibility = v;
		return this;
	}
	
	public JType getOwner() {
		return owner;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}


	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean isStatic() {
		return isStatic;
	}
	
//	public void addDependency(JOperation o) {
//		checkNotNull(o);
//		deps.add(o);
//	}
	
	
	JOperation copyTo(JType type) {
		// TODO copy deps
		JOperation op = new JOperation(type, getName());
		op.visibility = visibility;
		op.isAbstract = isAbstract;
		op.isStatic = isStatic;
		op.returnType = returnType;
		return op;
	}
	
	
	@Override
	public String toString() {
		return owner.getName() + "." + getName() + "(...)";
	}

	public void addDependency2(JOperation o) {
		checkNotNull(o);
		if(callDependencies.isEmpty())
			callDependencies = new ArrayList<>();
		
		callDependencies.add(new CallDependency(this, o));
	}
	
	
	public Collection<CallDependency> getDependencies2() {
		return Collections.unmodifiableList(callDependencies);
	}

	
}

