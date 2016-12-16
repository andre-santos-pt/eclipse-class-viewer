package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public  abstract class JType extends StereotypedElement {
	private static final long serialVersionUID = 1L;

	private final String qualifiedName;
	private Visibility visibility;
	private List<InterfaceImplementation> supertypes;
	private List<JOperation> operations;
	private List<JType> dependencies;
	
	
	JType(String qualifiedName) {
		super(qualifiedName);
		checkNotNull(qualifiedName);
		// TODO check pattern
		this.qualifiedName = qualifiedName;
		visibility = Visibility.PUBLIC;
		supertypes = Collections.emptyList();
		operations = Collections.emptyList();
		dependencies = Collections.emptyList();
		
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
	
	public String getPackage() {
		int i = qualifiedName.indexOf('.'); 
		if(i == -1)
			return "";
		else
			return qualifiedName.substring(0, i);
	}
	
	public boolean hasPackage() {
		return qualifiedName.indexOf('.') != -1;
	}
	
	
	void addOperation(JOperation operation) {
		checkNotNull(operation);
		
		if(operations.isEmpty())
			operations = new ArrayList<>();
		
		operations.add(operation);
	}
	
	public void addInterface(JInterface type) {
		checkNotNull(type);
		if(supertypes.isEmpty())
			supertypes = new ArrayList<>(3);
		
		supertypes.add(new InterfaceImplementation(this, type));
	}
	public boolean implementsInterfaces() {
		return !supertypes.isEmpty();
	}
	
	public List<JInterface> getInterfaces() {
		List<JInterface> list = new ArrayList<>(supertypes.size());
		for(InterfaceImplementation i : supertypes)
			list.add((JInterface) i.getTargetType());
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
	
	public Collection<IDependency> getDependencies2(JType target) {
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

	public Collection<CallDependency> getCallDependencies(JType target) {
		ArrayList<CallDependency> deps = new ArrayList<>();
		operations.forEach((o) -> o.getDependencies2().forEach((d) -> {
			if(d.getTargetType().equals(target)) {
				deps.add(d);
			}
		}));
		return deps;
	}
	
	void setVisibilityInternal(Visibility visibility) {
		checkNotNull(visibility);
		
		this.visibility = visibility;
	}
	
	public Visibility getVisibility() {
		return visibility;
	}
	
	public boolean isInterface() {
		return getClass().equals(JInterface.class);
	}

	public boolean isClass() {
		return getClass().equals(JClass.class);
	}
	
	public Collection<JOperation> getOperations() {
		return Collections.unmodifiableCollection(operations);
	}
	
	// TODO params
	public JOperation getOperation(String name) {
		for(JOperation o : operations)
			if(o.getName().equals(name))
				return o;
		
		return null;
	}
	
	
	@Override
	public String toString() {
		return qualifiedName + " " + (isClass() ? "[class]" : "[interface]");
	}
	
}
