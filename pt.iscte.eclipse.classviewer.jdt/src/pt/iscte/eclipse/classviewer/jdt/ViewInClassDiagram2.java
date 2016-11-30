package pt.iscte.eclipse.classviewer.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import pt.iscte.eclipse.classviewer.DiagramListener;
import pt.iscte.eclipse.classviewer.DiagramListener.Event;
import pt.iscte.eclipse.classviewer.JModelDiagram;
import pt.iscte.eclipse.classviewer.model.JClass;
import pt.iscte.eclipse.classviewer.model.JInterface;
import pt.iscte.eclipse.classviewer.model.JModel;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JPackage;
import pt.iscte.eclipse.classviewer.model.JType;
import pt.iscte.eclipse.classviewer.model.Stereotype;

public class ViewInClassDiagram2 implements IObjectActionDelegate {

	private List<IJavaElement> selection = new ArrayList<>();
	private DiagramListener handler;

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
		JModel model = null;
		try {
			model = buildModel();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		JModelDiagram.display(model);

		if(handler != null)
			JModelDiagram.removeListener(handler);

		JModelDiagram.addListener(handler);

	}

	private JModel buildModel() throws JavaModelException, Exception {
		JModel model = new JModel();
		Map<JType, IType> classes = new HashMap<>();
		IJavaElement firstInSelection = selection.get(0);

		for(IJavaElement element : selection) {
			if(firstInSelection instanceof IJavaProject) {
				IJavaProject project = (IJavaProject) element;
				IPackageFragment[] pcks = project.getPackageFragments();
				for(IPackageFragment p : pcks) {
					for(ICompilationUnit u : p.getCompilationUnits()) {
						IType t = u.findPrimaryType();
						if(Flags.isPublic(t.getFlags())) {
							JType jt = handleCompilationUnit(u, model);
							classes.put(jt, t);
						}

					}
				}
			}
			else if(firstInSelection instanceof IPackageFragment) {
				IPackageFragment pack = (IPackageFragment) element;
				for(ICompilationUnit u : pack.getCompilationUnits()) {
					JType jt = handleCompilationUnit(u, model);
					classes.put(jt, u.findPrimaryType());
				}
			}
		}

		handleExtends(classes, model);
		handleImplements(classes, model);
		handleDependencies(classes, model);
		
		for(IType t : classes.values()) {
			
			if(!t.isInterface())
				handleAssociations(t, model);
		}
		return model;
	}


	private JType handleCompilationUnit(ICompilationUnit unit, JModel model)
			throws Exception {

		IType t = unit.findPrimaryType();

		JType owner = null;
		if(t.isInterface()) {
			JInterface ji = new JInterface(t.getFullyQualifiedName());
			owner = ji;
			model.addType(ji);

			//			for(String interfaceName : t.getSuperInterfaceNames()) {
			//				JInterface si = new JInterface(interfaceName);
			//				ji.addSupertype(si);
			//			}
		}
		else {
			JClass jc = new JClass(t.getFullyQualifiedName());
			owner = jc;
			model.addType(jc);
			for(String interfaceName : t.getSuperInterfaceNames()) {
				JInterface si = new JInterface(interfaceName);
				jc.addInterface(si);
			}
		}

		for(IMethod m : t.getMethods())
			new JOperation(owner, m.getElementName());

		return owner;
	}

	private void handleExtends(Map<JType, IType> classes, JModel model) throws JavaModelException {
		for(Entry<JType, IType> e : classes.entrySet()) {
			IType t = e.getValue();
			if(t.isClass() && t.getSuperclassName() != null && !Object.class.getSimpleName().equals(t.getSuperclassName())) {
				String superQName = resolve(t, t.getSuperclassName());
				if(superQName == null)
					superQName = t.getSuperclassName();
				JClass superclass = model.getClass(superQName);
				if(superclass == null) {
					superclass = new JClass(superQName);
					superclass.addStereotype(new Stereotype("external"));
					model.addType(superclass);
				}
				((JClass) e.getKey()).setSuperclass(superclass);
			}
			else if(t.isInterface()) {
				for (String interfaceImpl : t.getSuperInterfaceNames()) {
					String interfaceQName = resolve(t, interfaceImpl);
					if(interfaceQName == null)
						interfaceQName = interfaceImpl;
					JInterface interfaceType = model.getInterface(interfaceQName);
					if(interfaceType == null) {
						interfaceType = new JInterface(interfaceQName);
						interfaceType.addStereotype(new Stereotype("external"));
						model.addType(interfaceType);
					}
				}
			
				
			}
		}
	}

	private static String resolve(IType t, String typeName) {
		String[][] resolveType;
		try {
			resolveType = t.resolveType(typeName);
			if(resolveType != null)
				return resolveType[0][0] + "." + resolveType[0][1];
			else
				return null;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private void handleImplements(Map<JType, IType> classes, JModel model)  {


	}

	private void handleDependencies(Map<JType, IType> classes, final JModel model) {
		for(Entry<JType, IType> e : classes.entrySet()) {
			final IType it = e.getValue();
			final JType t = e.getKey();
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(e.getValue().getCompilationUnit());
			ASTNode root = parser.createAST(null);
			root.accept(new ASTVisitor() {
				@Override
				public boolean visit(FieldDeclaration node) {
					String tName = resolve(it, node.getType().toString());
					if(tName == null)
						System.out.println("NULL: " + node.getType());
					else {
						JType dep = model.getType(tName);
						if(dep != null && !t.equals(dep))
							t.addDependency(dep);
					}
					return true;
				}
			});
		}
	}

	private void handleAssociations(IType t, JModel model) {

		try {
			for (IField f : t.getFields()) {
				String[] typeArguments = Signature.getTypeArguments(f.getTypeSignature());
				System.out.println(Arrays.toString(typeArguments));
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		try {
			for(IMethod m : t.getMethods()) {
				//			m.get
				//			IMethodBinding  method=m.resolveMethodBinding();
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	private void handleAssociations(IType t, JModel model) {
	//		 JClass jc = (JClass) model.getType(t.getFullyQualifiedName());
	//		 
	//		
	//		for(IField field : t.getFields()) {
	//
	//
	//			System.out.println(field);
	//			
	//			
	//			if(Collection.class.isAssignableFrom(fieldType)) { //Collection field
	////				Type genericFieldType = field.getGenericType();
	////
	////				if(!ParameterizedType.class.isAssignableFrom(genericFieldType.getClass())) continue; //If the collection does not have a parametrized type
	////
	////				Type aux = ((ParameterizedType)genericFieldType).getActualTypeArguments()[0];
	//
	//				if(!Class.class.isAssignableFrom(aux.getClass())) continue; //If the parametrized type is not a class type, i.e., Collection<Collection<Type>>
	//
	//				Class<?> actualFieldType = (Class<?>)aux;
	//
	//				if(actualFieldType.isArray()) continue; //If the parametrized type is an array
	//
	//				new Association(jc, model.getType(actualFieldType.getName()), false);
	//	
	////				if(set.contains(actualFieldType)) {
	//					//createMultipleConnection(clazz, actualFieldType, field);
	////				}
	//			}
	//			else if(fieldType.isArray()) { //Array field
	//				Class<?> componentFieldType = fieldType.getComponentType();
	//
	//				if(componentFieldType.isArray()) continue; //If the component type of the array is another array
	//
	////				if(set.contains(componentFieldType)) {
	//					//createMultipleConnection(clazz, componentFieldType, field);
	////				}
	//				new Association(jc, model.getType(componentFieldType.getName()), false);
	//			}
	//			else if(model.getType(fieldType.getName()) != null) { //Class field
	////				if(set.contains(fieldType)) {
	//					//createSimpleConnection(clazz, fieldType, field);
	////				}
	//				new Association(jc, model.getType(fieldType.getName()), true);
	//			}
	//
	//		}
	//	}


	private class Listener extends DiagramListener.Adapter {
		private final Map<JType, IType> classes;

		Listener(Map<JType, IType> classes) {
			this.classes = classes;
		}

		@Override
		public void operationEvent(JOperation op, Event event) {
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

		@Override
		public void classEvent(JType type, Event event) {
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
