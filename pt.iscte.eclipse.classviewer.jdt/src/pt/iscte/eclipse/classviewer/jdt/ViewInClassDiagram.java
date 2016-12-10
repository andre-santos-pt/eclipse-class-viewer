package pt.iscte.eclipse.classviewer.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import pt.iscte.eclipse.classviewer.DiagramListener;
import pt.iscte.eclipse.classviewer.JModelDiagram;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JType;

public class ViewInClassDiagram implements IObjectActionDelegate {

	private List<IJavaElement> selection = new ArrayList<>();

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	public void selectionChanged(IAction action, ISelection s) {
		selection.clear();
		if(s instanceof IStructuredSelection) {
			for(Object obj : ((IStructuredSelection) s).toList())
				if(obj instanceof IJavaElement)
					selection.add((IJavaElement) obj);
		}
	}

	public void run(IAction action) {
		if(selection.isEmpty())
			return;
		ModelBuilder.Result res = null;
		try {
			res = ModelBuilder.buildModel(selection);
		}
		catch(Exception e) {
			MessageDialog.openError(null, "Error", "Could not load classes");
			e.printStackTrace();
			return;
		}
		new JModelDiagram(res.model)
		.withListener(new Listener(res.classes))
		.display();
	}

	private class Listener extends DiagramListener.Adapter {
		private final Map<JType, IType> classes;

		Listener(Map<JType, IType> classes) {
			this.classes = classes;
		}

		@Override
		public void operationEvent(JOperation op, Event event) {
			if(event.equals(Event.DOUBLE_CLICK)) {
				IType t = classes.get(op.getOwner());
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ITextEditor editor = (ITextEditor) IDE.openEditor(page, (IFile) t.getResource());
					for(IMethod im : t.getMethods()) {
						if(im.getElementName().equals(op.getName())) {
							ISourceRange range =  im.getNameRange();
							editor.selectAndReveal(range.getOffset(), range.getLength());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}

		@Override
		public void classEvent(JType type, Event event) {
			if(event.equals(Event.DOUBLE_CLICK)) {
				IType t = classes.get(type);
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					ITextEditor editor = (ITextEditor) IDE.openEditor(page, (IFile) t.getResource());
					ISourceRange range =  t.getNameRange();
					editor.selectAndReveal(range.getOffset(), range.getLength());
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
	}
}
