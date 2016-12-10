package pt.iscte.eclipse.classviewer.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class JInterface extends JType {
	
	private static final long serialVersionUID = 1L;

	private Set<JInterface> supertypes;
	
	public JInterface(String name) {
		super(name);
		supertypes = Collections.emptySet();
	}
	 
	public void addSupertype(JInterface type) {
		if(supertypes.isEmpty())
			supertypes = new HashSet<JInterface>(3);
		
		supertypes.add(type);
	}
	
	public Iterable<JInterface> getSuperInterfaces() {
		return Collections.unmodifiableSet(supertypes);
	}
	
	public JInterface addStereotype(String stereotypeName) {
		addStereotypeInternal(new Stereotype(stereotypeName));
		return this;
	}
	
	
}
