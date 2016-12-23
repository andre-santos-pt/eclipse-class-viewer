package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class StereotypedElement extends NamedElement {

	private static final long serialVersionUID = 1L;
	
	private List<String> stereotypes;
	
	StereotypedElement(String name) {
		super(name);
		stereotypes = Collections.emptyList();
	}
	
	public void addStereotype(String stereotype) {
		checkNotNull(stereotype);
		
		if(stereotypes.isEmpty())
			stereotypes = new ArrayList<>(3);
		
		stereotypes.add(stereotype);
	}
	
	public List<String> getSterotypes() {
		return Collections.unmodifiableList(stereotypes);
	}
}
