package pt.iscte.eclipse.classviewer.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import pt.iscte.eclipse.classviewer.JModelDiagram;


public class ClearContentHandler extends AbstractHandler {

	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JModelViewer.getInstance().clear();
		return null;
	}

}