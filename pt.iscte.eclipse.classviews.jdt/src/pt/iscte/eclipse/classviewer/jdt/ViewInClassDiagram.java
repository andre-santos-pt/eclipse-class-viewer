package pt.iscte.eclipse.classviewer.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import pt.iscte.eclipse.classviews.DiagramListener;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public class ViewInClassDiagram extends AbstractHandler {

//	private List<IJavaElement> selection = new ArrayList<>();
//
//	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
//
//	}
//
//	public void selectionChanged(IAction action, ISelection s) {
//		selection.clear();
//		if(s instanceof IStructuredSelection) {
//			for(Object obj : ((IStructuredSelection) s).toList())
//				if(obj instanceof IJavaElement)
//					selection.add((IJavaElement) obj);
//		}
//	}

	//	public void run(IAction action) {
	//		if(selection.isEmpty())
	//			return;
	//		ModelBuilder.Result res = null;
	//		try {
	//			res = ModelBuilder.buildModel(selection);
	//		}
	//		catch(Exception e) {
	//			MessageDialog.openError(null, "Error", "Could not load classes");
	//			e.printStackTrace();
	//			return;
	//		}
	//		new ClassViewsDiagram(res.model)
	//		.withListener(new Listener(res.classes))
	//		.display();
	//	}

	private class Listener extends DiagramListener.Adapter {
		private final Map<VType, IType> classes;

		Listener(Map<VType, IType> classes) {
			this.classes = classes;
		}

		@Override
		public void operationEvent(VOperation op, Event event) {
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
		public void classEvent(VType type, Event event) {
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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		List<IJavaElement> selection = new ArrayList<>();
		if(s instanceof IStructuredSelection) {
			for(Object obj : ((IStructuredSelection) s).toList())
				if(obj instanceof IJavaElement)
					selection.add((IJavaElement) obj);
		}

		if(!selection.isEmpty()) {
			VModel model = new VModel();
			ModelBuilder.Result res = null;
			try {
				res = ModelBuilder.buildModel(selection, model);
			}
			catch(Exception e) {
				MessageDialog.openError(null, "Error", "Could not load classes");
				e.printStackTrace();
				return null;
			}
//			new ClassViewsDiagram(res.model)
//			.withListener(new Listener(res.classes))
//			.display();
		}
		return null;
	}
}
