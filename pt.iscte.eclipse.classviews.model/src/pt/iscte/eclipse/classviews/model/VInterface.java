package pt.iscte.eclipse.classviews.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class VInterface extends VType {
	
	private static final long serialVersionUID = 1L;

	private Set<VInterface> supertypes;
	
	public VInterface(String name) {
		super(name);
		supertypes = Collections.emptySet();
	}
	 
	public void addSupertype(VInterface type) {
		if(supertypes.isEmpty())
			supertypes = new HashSet<VInterface>(3);
		
		supertypes.add(type);
	}
	
	public Iterable<VInterface> getSuperInterfaces() {
		return Collections.unmodifiableSet(supertypes);
	}
}
