package pt.iscte.eclipse.classviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.JAssociation;
import pt.iscte.eclipse.classviews.model.VClass;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public interface IStereotypePolicy {
	List<String> getStereotypes(VType t);
	List<String> getStereotypes(VField f);
	List<String> getStereotypes(JAssociation t);
	List<String> getStereotypes(VOperation o);
	List<String> getStereotypes(IDependency d);
	
	
	IStereotypePolicy DEFAULT = new DefaultPolicy();
	
	class DefaultPolicy implements IStereotypePolicy {

		@Override
		public List<String> getStereotypes(VType t) {
			List<String> stereotypes = t.getSterotypes();
			if(!stereotypes.isEmpty()) {
				stereotypes =  new ArrayList<>(stereotypes);
				if(t.isInterface())
					stereotypes.add("interface");
				else if(t.isClass() && ((VClass) t).isAbstract())
					stereotypes.add("abstract");
			}
			
			return stereotypes;
		}

		@Override
		public List<String> getStereotypes(VField f) {
			return Collections.emptyList();
		}

		@Override
		public List<String> getStereotypes(JAssociation t) {
			return Collections.emptyList();
		}

		@Override
		public List<String> getStereotypes(VOperation o) {
			return Collections.emptyList();
		}

		@Override
		public List<String> getStereotypes(IDependency d) {
			return Collections.emptyList();
		}
		
	}
}
