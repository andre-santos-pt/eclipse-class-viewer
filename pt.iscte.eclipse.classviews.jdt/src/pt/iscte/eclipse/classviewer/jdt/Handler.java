package pt.iscte.eclipse.classviewer.jdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import pt.iscte.eclipse.classviews.IResourceHandler;
import pt.iscte.eclipse.classviews.model.VModel;

public class Handler implements IResourceHandler {

	@Override
	public boolean process(IResource resource, VModel model) {
		try {
			if (resource instanceof IProject && resource.getProject().hasNature(JavaCore.NATURE_ID)) {
				IJavaProject proj = JavaCore.create(resource.getProject());
				//				 IJavaElement element = proj.findElement(resource.getFullPath());
//				System.out.println(proj);
				try {
					List<IJavaElement> list = new ArrayList<>();
					list.add(proj);
					ModelBuilder.buildModel(list, model);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
