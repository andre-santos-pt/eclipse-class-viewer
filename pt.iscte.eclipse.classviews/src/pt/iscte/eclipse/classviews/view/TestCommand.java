package pt.iscte.eclipse.classviews.view;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;

import pt.iscte.eclipse.classviews.IModelCommand;
import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.VClass;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VType;

public class TestCommand implements IModelCommand {

	@Override
	public boolean isEnabled(Collection<VType> selectedTypes, Collection<IDependency> selectedDependencies,
			VModel model) {
		return !selectedTypes.isEmpty();
	}
	
	@Override
	public void run(Collection<VType> selectedTypes, Collection<IDependency> selectedDependencies, VModel model, UI ui) {
		for(VType t : selectedTypes)
			ui.mark(t, ColorConstants.red);
		
		Iterator<VType> types = model.getTypes((t) -> t.isClass() && ((VClass) t).getSuperclasses().isEmpty());
	}

}
