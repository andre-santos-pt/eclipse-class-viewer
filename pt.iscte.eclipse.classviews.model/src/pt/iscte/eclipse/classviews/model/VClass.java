package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class VClass extends VType {
	
	private static final long serialVersionUID = 1L;
	
	private boolean isAbstract;
	private List<ClassExtension> superclasses;
	private List<VField> fields;
	private List<JAssociation> associations;
	
	public VClass(String name) {
		this(name, false);
	}
	
	public VClass(String name, boolean isAbstract) {
		super(name);
		this.isAbstract = isAbstract;
		superclasses = Collections.emptyList();
		fields = Collections.emptyList();
		associations = Collections.emptyList();
	}

	
	public List<VField> getFields() {
		return Collections.unmodifiableList(fields);
	}
	
	public VClass addSuperclass(VClass superclass) {
		if(superclasses.isEmpty())
			superclasses = new ArrayList<>();
		
		superclasses.add(new ClassExtension(this, superclass));
		return this;
	}
	
	void addField(VField field) {
		checkNotNull(field);
		if(fields.isEmpty())
			fields = new ArrayList<VField>(5);
		
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
	
	public List<VClass> getSuperclasses() {
		if(superclasses.isEmpty())
			return Collections.emptyList();
		else {
			List<VClass> list = new ArrayList<>();
			for(ClassExtension ext : superclasses)
				list.add((VClass) ext.getTargetType());
			return list;
		}
	}
	public VClass getFirstSuperclass() {
		if(superclasses.isEmpty())
			return null;
		else
			return (VClass) superclasses.get(0).getTargetType();
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
	
	public Collection<IDependency> getDependencies2(VType target) {
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
	
	public boolean hasNonUnaryDependencyTo(VType target) {
		for(VField f : fields)
			if(f.getType().equals(target) && !f.getCardinality().isUnary())
				return true;
					
		return false;
	}
	
	

	public List<JAssociation> getAssociations() {
		return Collections.unmodifiableList(associations);
	}

	// TODO
	public boolean compatibleWith(VClass clazz) {
		VClass c = this;
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
