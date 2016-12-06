package pt.iscte.eclipse.classviewer;

import pt.iscte.eclipse.classviewer.model.JModel;
import pt.iscte.eclipse.classviewer.view.JModelViewer;

public class JModelDiagram {

	public static void display(JModel model) {
		JModelViewer.getInstance().displayModel(model, false);
	}
	
	public static void displayIncremental(JModel model) {
		JModelViewer.getInstance().displayModel(model, true);
	}
	
	public static void clearDiagram() {
		//TODO clear
		//		JModelViewer.getInstance().clear();
	}
	
	public static void addListener(DiagramListener listener) {
		JModelViewer.getInstance().addClickHandler(listener);
	}
	
	public static void removeListener(DiagramListener listener) {
		JModelViewer.getInstance().removeClickHandler(listener);
	}

}