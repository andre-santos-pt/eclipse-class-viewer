package pt.iscte.eclipse.classviews.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import pt.iscte.eclipse.classviews.IResourceHandler;

public class Activator extends AbstractUIPlugin {

	private static Activator instance;

	private List<IResourceHandler> dropHandlers;

	public Activator() {
		instance = this;
	}

	public static Activator getInstance() {
		return instance;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		loadDropHandlers();

	}

	private void loadDropHandlers() {
		dropHandlers = new ArrayList<>();
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("pt.iscte.eclipse.classviews", "resourceHandlers");
		IExtension[] extensions = ep.getExtensions();
		for(IExtension e : extensions) {
			for(IConfigurationElement c : e.getConfigurationElements()) {
				try {
					IResourceHandler h = (IResourceHandler) c.createExecutableExtension("class");
					dropHandlers.add(h);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public List<IResourceHandler> getDropHandlers() {
		return Collections.unmodifiableList(dropHandlers);
	}
}
