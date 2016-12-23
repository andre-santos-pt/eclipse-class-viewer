package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class VType extends StereotypedElement {
	private static final long serialVersionUID = 1L;

	private final String qualifiedName;
	private List<InterfaceImplementation> supertypes;
	private List<VOperation> operations;
	
	
	VType(String qualifiedName) {
		super(qualifiedName);
		checkNotNull(qualifiedName);
		// TODO check pattern
		this.qualifiedName = qualifiedName;
		supertypes = Collections.emptyList();
		operations = Collections.emptyList();	
	}

	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public String getName() {
		int i = qualifiedName.lastIndexOf('.'); 
		if(i == -1)
			return qualifiedName;
		else
			return qualifiedName.substring(i+1);
	}
	
	public String getPackageName() {
		int i = qualifiedName.lastIndexOf('.'); 
		if(i == -1)
			return "";
		else
			return qualifiedName.substring(0, i);
	}
	
	public boolean hasPackage() {
		return qualifiedName.indexOf('.') != -1;
	}
	
	
	void addOperation(VOperation operation) {
		checkNotNull(operation);
		
		if(operations.isEmpty())
			operations = new ArrayList<>();
		
		operations.add(operation);
	}
	
	public void addInterface(VInterface type) {
		checkNotNull(type);
		if(supertypes.isEmpty())
			supertypes = new ArrayList<>(3);
		
		supertypes.add(new InterfaceImplementation(this, type));
	}
	public boolean implementsInterfaces() {
		return !supertypes.isEmpty();
	}
	
	public List<VInterface> getInterfaces() {
		List<VInterface> list = new ArrayList<>(supertypes.size());
		for(InterfaceImplementation i : supertypes)
			list.add((VInterface) i.getTargetType());
		return list;
	}
	
//	public void addDependency(JType type) {
//		checkNotNull(type);
//		
//		if(dependencies.isEmpty())
//			dependencies = new ArrayList<JType>();
//		
//		dependencies.add(type);
//	}
	
//	public List<Dependency> getDependencies(JType target) {
//		ArrayList<Dependency> deps = new ArrayList<>();
//		operations.forEach((o) -> o.getDependencies().stream()
//				.filter((d) -> !d.equals(this) && d.getOwner().equals(target))
//				.forEach((d) -> deps.add(new CallDependency(o, d))));
//		
//		if(supertypes.contains(target))
//			deps.add(Dependency.ofInterface(this, target));
//
//		return deps;
//	}
	
	public Collection<IDependency> getDependencies2() {
		return getDependencies2(null);
	}
	
	public Collection<IDependency> getDependencies2(VType target) {
		ArrayList<IDependency> deps = new ArrayList<>();
		//TODO dependencies
		supertypes.forEach((s) -> {
			if(target == null || s.getTargetType().equals(target))
				deps.add(s);
		});
		
		operations.forEach((o) -> o.getDependencies2().forEach((d) -> {
			if(target == null || d.getTargetType().equals(target)) {
				boolean duplicate = false;
				for(IDependency t : deps)
					if(t instanceof TypeDependency && t.getSourceType().equals(this) && t.getTargetType().equals(d.getTargetType()))
						duplicate = true;
				if(!duplicate)
					deps.add(new TypeDependency(this, d.getTargetType()));
			}
		}));
		
		return deps;
	}
	
	public Collection<CallDependency> getCallDependencies() {
		ArrayList<CallDependency> deps = new ArrayList<>();
		operations.forEach((o) -> deps.addAll(o.getDependencies2()));
		return deps;
	}

	public Collection<CallDependency> getCallDependencies(VType target) {
		ArrayList<CallDependency> deps = new ArrayList<>();
		operations.forEach((o) -> o.getDependencies2().forEach((d) -> {
			if(d.getTargetType().equals(target)) {
				deps.add(d);
			}
		}));
		return deps;
	}
	
	public boolean isInterface() {
		return getClass().equals(VInterface.class);
	}

	public boolean isClass() {
		return getClass().equals(VClass.class);
	}
	
	public Collection<VOperation> getOperations() {
		return Collections.unmodifiableCollection(operations);
	}
	
	// TODO params
	public VOperation getOperation(String name) {
		for(VOperation o : operations)
			if(o.getName().equals(name))
				return o;
		
		return null;
	}
	
	
	@Override
	public String toString() {
		return qualifiedName + " " + (isClass() ? "[class]" : "[interface]");
	}
	
}
