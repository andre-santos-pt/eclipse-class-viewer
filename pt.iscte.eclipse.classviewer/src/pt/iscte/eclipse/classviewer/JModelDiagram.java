package pt.iscte.eclipse.classviewer;

import java.util.ArrayList;
import java.util.List;

import pt.iscte.eclipse.classviewer.model.JModel;
import pt.iscte.eclipse.classviewer.view.JModelViewer;

public class JModelDiagram {

	private JModel model;
	private boolean incremental;
	private List<DiagramListener> listeners;
	
//	private List<E> filters;
	
	public JModelDiagram(JModel model) {
		checkNotNull(model);
		this.model = model;
		incremental = false;
		listeners = new ArrayList<DiagramListener>();
	}
	
	private static void checkNotNull(Object o) {
		if(o == null)
			throw new NullPointerException("arg cannot be null");
	}
	
	public JModelDiagram incremental() {
		incremental = true;
		return this;
	}
			
	public JModelDiagram withListener(DiagramListener listener) {
		checkNotNull(listener);
		listeners.add(listener);
		return this;
	}
	
	public void display() {
		JModelViewer.getInstance().displayModel(model, incremental, listeners);
	}
	
	public static void clearDiagram() {
		//TODO clear
		//		JModelViewer.getInstance().clear();
	}
	


}