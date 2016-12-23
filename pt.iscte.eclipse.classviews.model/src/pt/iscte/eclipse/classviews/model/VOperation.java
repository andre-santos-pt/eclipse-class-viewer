package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class VOperation extends ClassMember {

	
	private static final long serialVersionUID = 1L;

	private VType owner;
	private boolean isAbstract;
	private List<VType> params;
	private VType returnType;
	
	private List<CallDependency> callDependencies;
	
	public VOperation(VType owner, String name, VType returnType, VType ... params) {
		super(name);
		checkNotNull(owner, name);
		this.owner = owner;
		this.returnType = returnType; // TODO
		
		owner.addOperation(this);
		isAbstract = false;
		this.params = new ArrayList<>(params.length);
		for(VType p : params) {
			checkNotNull(p);
			this.params.add(p);
		}
		
		callDependencies = Collections.emptyList();
	}
	
	public VOperation setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
		return this;
	}
	
	public VType getOwner() {
		return owner;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public VType getReturnType() {
		return returnType;
	}
	
	@Override
	public String toString() {
		return owner.getName() + "." + getName() + "(...)";
	}

	public void addDependency2(VOperation o) {
		checkNotNull(o);
		if(callDependencies.isEmpty())
			callDependencies = new ArrayList<>();
		
		callDependencies.add(new CallDependency(this, o));
	}
	
	
	public Collection<CallDependency> getDependencies2() {
		return Collections.unmodifiableList(callDependencies);
	}

	
}

