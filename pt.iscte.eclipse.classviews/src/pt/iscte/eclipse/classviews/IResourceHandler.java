package pt.iscte.eclipse.classviews;

import org.eclipse.core.resources.IResource;

import pt.iscte.eclipse.classviews.model.VModel;

public interface IResourceHandler {

	boolean process(IResource resource, VModel model);
}
