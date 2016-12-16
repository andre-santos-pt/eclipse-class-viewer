package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JClass extends JType {
	
	private static final long serialVersionUID = 1L;
	
	private boolean isAbstract;
//	private JClass superclass;
	private List<ClassExtension> superclasses;
	private List<JField> fields;
	private List<JAssociation> associations;
	
	public JClass(String name) {
		this(name, false);
	}
	
	public JClass(String name, boolean isAbstract) {
		super(name);
		this.isAbstract = isAbstract;
//		superclass = null;
		superclasses = Collections.emptyList();
		fields = Collections.emptyList();
		associations = Collections.emptyList();
	}
	
	public static JClass createAbstract(String name) {
		return new JClass(name, true);
	}
	
	public List<JField>  getFields() {
		return Collections.unmodifiableList(fields);
	}
	
	public JClass addSuperclass(JClass superclass) {
		if(superclasses.isEmpty())
			superclasses = new ArrayList<>();
		
		superclasses.add(new ClassExtension(this, superclass));
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
	
	void addAssociation(JAssociation a) {
		checkNotNull(a);
		if(associations.isEmpty())
			associations = new ArrayList<JAssociation>(5);
		
		associations.add(a);
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean hasSuperclasses() {
		return !superclasses.isEmpty();
	}
	
	public List<JClass> getSuperclasses() {
		if(superclasses.isEmpty())
			return Collections.emptyList();
		else {
			List<JClass> list = new ArrayList<>();
			for(ClassExtension ext : superclasses)
				list.add((JClass) ext.getTargetType());
			return list;
		}
	}
	public JClass getFirstSuperclass() {
		if(superclasses.isEmpty())
			return null;
		else
			return (JClass) superclasses.get(0).getTargetType();
	}
	

//	@Override
//	public List<Dependency> getDependencies(JType target) {
//		checkNotNull(target);
//		List<Dependency> deps = super.getDependencies(target);
//		if(target.equals(superclass))
//			deps.add(Dependency.ofInheritance(this, target));
//		
//		for(JField f : fields)
//			if(f.getType().equals(target) && !f.getType().hasProperty("VALUE_TYPE"))
//				deps.add(new FieldDependency(this, target, f.getName(), f.getCardinality()));
//		return deps;
//	}
	
	public Collection<IDependency> getDependencies2(JType target) {
		Collection<IDependency> deps = super.getDependencies2(target);
		fields.forEach((f) -> {
			if(target == null || f.getType().equals(target))
				deps.add(f);
		});
		superclasses.forEach((s) -> {
			if(target == null || s.getTargetType().equals(target))
				deps.add(s);
		});
		return deps;
	}
	
	public boolean hasNonUnaryDependencyTo(JType target) {
		for(JField f : fields)
			if(f.getType().equals(target) && !f.getCardinality().isUnary())
				return true;
					
		return false;
	}
	
	

	public List<JAssociation> getAssociations() {
		return Collections.unmodifiableList(associations);
	}

	// TODO
	public boolean compatibleWith(JClass clazz) {
		JClass c = this;
		while(c != null) {
			if(c == clazz)
				return true;
			c = c.getSuperclasses().isEmpty() ? null : c.getSuperclasses().get(0);
		}
		return false;
	}
	
//	public boolean compatibleWith(JClass clazz) {
//		return false;
//	}
	
	
	
}
