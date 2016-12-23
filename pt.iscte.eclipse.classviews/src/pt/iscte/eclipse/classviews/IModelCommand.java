package pt.iscte.eclipse.classviews;

import java.util.Collection;

import org.eclipse.swt.graphics.Color;

import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VType;

public interface IModelCommand {

	boolean isEnabled(Collection<VType> selectedTypes, Collection<IDependency> selectedDependencies, VModel model);
	
	void run(Collection<VType> selectedTypes, Collection<IDependency> selectedDependencies, VModel model, UI ui);
	
	interface UI {
		void mark(VType t, Color c);
		void select(VType t);
	}
}
