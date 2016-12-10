package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public  abstract class JType extends StereotypedElement implements Iterable<JOperation> {
	private static final long serialVersionUID = 1L;

	private final String qualifiedName;
	private Visibility visibility;
	private Set<JInterface> supertypes;
	private List<JOperation> operations;
	private List<JType> dependencies;
	
	
	JType(String qualifiedName) {
		super(qualifiedName);
		checkNotNull(qualifiedName);
		// TODO check pattern
		this.qualifiedName = qualifiedName;
		visibility = Visibility.PUBLIC;
		supertypes = Collections.emptySet();
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
			operations = new ArrayList<JOperation>();
		
		operations.add(operation);
	}
	
	public void addInterface(JInterface type) {
		checkNotNull(type);
		if(supertypes.isEmpty())
			supertypes = new HashSet<JInterface>(3);
		
		supertypes.add(type);
		
	}
	public boolean implementsInterfaces() {
		return !supertypes.isEmpty();
	}
	
	public Iterable<JInterface> getInterfaces() {
		return Collections.unmodifiableSet(supertypes);
	}
	
	public void addDependency(JType type) {
		checkNotNull(type);
		
		if(dependencies.isEmpty())
			dependencies = new ArrayList<JType>();
		
		dependencies.add(type);
	}
	
	public List<Dependency> getDependencies(JType target) {
		ArrayList<Dependency> deps = new ArrayList<>();
//		dependencies.stream()
//		.filter((d) -> !d.equals(this) && d.equals(target))
//		.forEach((d) -> deps.add(new Dependency(this, d, Dependency.Kind.ATTRIBUTE)));
		
//		GroupedDependency
		operations.forEach((o) -> o.getDependencies().stream()
				.filter((d) -> !d.equals(this) && d.getOwner().equals(target))
				.forEach((d) -> deps.add(new CallDependency(o, d))));
		
		if(supertypes.contains(target))
			deps.add(Dependency.ofInterface(this, target));

		return deps;
	}
	
	
	
//	public Iterable<MMethod> getOperations() {
//		return Collections.unmodifiableList(operations);
//	}

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
	public Iterator<JOperation> iterator() {
		return Collections.unmodifiableList(operations).iterator();
	}
	
	@Override
	public String toString() {
		return qualifiedName + " " + (isClass() ? "[class]" : "[interface]");
	}

	

	


	
}
