package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JClass extends JType {
	
	private static final long serialVersionUID = 1L;
	
	private boolean isAbstract;
	private JClass superclass;
	private List<JField> fields;
	private Set<Association> associations;
	
	public JClass(String name) {
		this(name, false);
	}
	public JClass(String name, boolean isAbstract) {
		super(name);
		this.isAbstract = isAbstract;
		superclass = null;
		fields = Collections.emptyList();
		associations = Collections.emptySet();
	}
	
	public List<JField>  getFields() {
		return Collections.unmodifiableList(fields);
	}
	
//	public JClass setAbstract(boolean isAbstract) {
//		this.isAbstract = isAbstract;
//		return this;
//	}
	
	public JClass setSuperclass(JClass superclass) {
		this.superclass = superclass;
		return this;
	}
	
	public JClass addStereotype(String stereotypeName) {
		addStereotypeInternal(new Stereotype(stereotypeName));
		return this;
	}
	
	public JClass setVisibility(Visibility visibility) {
		setVisibilityInternal(visibility);
		return this;
	}
	
	void addField(JField field) {
		checkNotNull(field);
		if(fields.isEmpty())
			fields = new ArrayList<JField>(5);
		
		fields.add(field);
	}
	
	void addAssociation(Association a) {
		checkNotNull(a);
		if(associations.isEmpty())
			associations = new HashSet<Association>(5);
		
		associations.add(a);
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean hasSuperclass() {
		return superclass != null;
	}
	
	public JClass getSuperclass() {
		return superclass;
	}
	
	

	@Override
	public List<Dependency> getDependencies(JType target) {
		List<Dependency> deps = super.getDependencies(target);
		if(target.equals(superclass))
			deps.add(Dependency.ofInheritance(this, target));
		
		for(JField f : fields)
			if(f.getType().equals(target) && !f.hasProperty("VALUE_TYPE"))
				deps.add(new FieldDependency(this, target, Cardinality.one()));
		return deps;
	}
	
	
	

	public Iterable<Association> getAssociations() {
		return Collections.unmodifiableSet(associations);
	}

	

	public boolean compatibleWith(JClass clazz) {
		JClass c = this;
		while(c != null) {
			if(c == clazz)
				return true;
			c = c.getSuperclass();
		}
		return false;
	}
	
	

	
	
}
