package pt.iscte.eclipse.classviewer.jdt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import pt.iscte.eclipse.classviews.model.CallDependency;
import pt.iscte.eclipse.classviews.model.VClass;
import pt.iscte.eclipse.classviews.model.VInterface;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public class ModelBuilder {
	public static final String TAG_UNUSED = "UNUSED";
	public static final String TAG_EXTERNAL = "EXTERNAL";
	public static final String TAG_EXTERNAL_DEPENDENCY = "EXTERNAL_DEPENDENCY";
	public static final String TAG_VALUE_TYPE = "VALUE_TYPE";

	static class Result {
		VModel model;
		Map<VType, IType> classes;
	}


	public static Result buildModel(List<IJavaElement> selection, VModel model) throws JavaModelException, Exception {
		Map<VType, IType> classes = new HashMap<>();
		IJavaElement firstInSelection = selection.get(0);

		for(IJavaElement element : selection) {
			if(firstInSelection instanceof IJavaProject) {
				IJavaProject project = (IJavaProject) element;
				IPackageFragment[] pcks = project.getPackageFragments();
				for(IPackageFragment p : pcks) {
					for(ICompilationUnit u : p.getCompilationUnits()) {
						IType t = u.findPrimaryType();
						if(Flags.isPublic(t.getFlags())) { // review
							VType jt = handleType(u.findPrimaryType(), model);
							classes.put(jt, t);
						}
					}
				}
			}
			else if(firstInSelection instanceof IPackageFragment) {
				IPackageFragment pack = (IPackageFragment) element;
				for(ICompilationUnit u : pack.getCompilationUnits()) {
					VType jt = handleType(u.findPrimaryType(), model);
					classes.put(jt, u.findPrimaryType());
				}
				for (IClassFile c : pack.getClassFiles()) {
					VType jt = handleType(c.getType(), model);
					classes.put(jt, c.getType());
				}
			}
			else if(firstInSelection instanceof ICompilationUnit) {
				ICompilationUnit u = (ICompilationUnit) element;
				VType jt = handleType(u.findPrimaryType(), model);
				classes.put(jt, u.findPrimaryType());
			}

		}

		handleExtendsAndImplements(classes, model);
		handleDependencies(classes, model);
		markUsedOperations(model);
		markExternalDependencies(model);
		Result res = new Result();
		res.model = model;
		res.classes = classes;
		return res;
	}



	public static String resolve(IType t, String typeName) {
		String[][] resolveType;
		try {
			resolveType = t.resolveType(typeName);
			if(resolveType != null)
				return resolveType[0][0] + "." + resolveType[0][1];
			else
				return typeName;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return typeName;
	}

	private static VType handleType(IType t, VModel model) throws JavaModelException {
		VType type = t.isInterface() ? new VInterface(t.getFullyQualifiedName()) : new VClass(t.getFullyQualifiedName(), Flags.isAbstract(t.getFlags()));
		model.addType(type);
		if(t.isEnum())
			type.setTagProperty(TAG_VALUE_TYPE);
		for(IMethod m : t.getMethods()) {
			VOperation o = new VOperation(type, m.getElementName(),  null); // TODO return type +  params
			o.setStatic(Flags.isStatic(m.getFlags()));
		}
		return type;
	}



	private static void handleExtendsAndImplements(Map<VType, IType> classes, VModel model) throws JavaModelException {
		for(Entry<VType, IType> e : classes.entrySet()) {
			IType t = e.getValue();
			if(t.isClass() && t.getSuperclassName() != null && !Object.class.getSimpleName().equals(t.getSuperclassName())) {
				String superQName = resolve(t, t.getSuperclassName());
				VClass superclass = model.getClass(superQName);
				if(superclass == null) {
					superclass = new VClass(superQName);
					superclass.setTagProperty(TAG_EXTERNAL);
					model.addType(superclass);
				}
				((VClass) e.getKey()).addSuperclass(superclass);
			}

			for (String interfaceImpl : t.getSuperInterfaceNames()) {
				String interfaceQName = resolve(t, interfaceImpl);
				VInterface interfaceType = model.getInterface(interfaceQName);
				if(interfaceType == null) {
					interfaceType = new VInterface(interfaceQName);
					interfaceType.setTagProperty(TAG_EXTERNAL);
					model.addType(interfaceType);
				}
				e.getKey().addInterface(interfaceType);
			}
		}
	}




	private static void handleDependencies(Map<VType, IType> classes, final VModel model) {
		for(Entry<VType, IType> e : classes.entrySet()) {
			final IType it = e.getValue();
			final VType t = e.getKey();
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			if(it.getCompilationUnit() != null)
				parser.setSource(it.getCompilationUnit());
			else if(it.getClassFile() != null)
				parser.setSource(it.getClassFile());
			else
				continue;
			parser.setResolveBindings(true);
			ASTNode root = parser.createAST(null);
			Visitor visitor = new Visitor(model, t, it);
			root.accept(visitor);
			if(visitor.isValueType())
				t.setTagProperty(TAG_VALUE_TYPE);
		}
	}



	private static void markUsedOperations(VModel model) {
		Set<VOperation> operationsWithDeps = new HashSet<>();

		model.getTypes().forEach((t) -> t.getOperations().forEach((o) -> {
			Collection<CallDependency> deps = o.getDependencies2();
			if(!deps.isEmpty()) {
				operationsWithDeps.add(o);
				deps.forEach((d) -> operationsWithDeps.add(d.getTargetOperation()));
			}
		}));

		model.getTypes().forEach((t) -> t.getOperations().forEach((o) -> {
			if(!operationsWithDeps.contains(o)) { 
				o.setTagProperty(TAG_UNUSED);
			}
		}));
	}

	private static void markExternalDependencies(VModel model) {
		model.getTypes().stream()
		.filter((t) -> !t.hasProperty(TAG_EXTERNAL))
		.forEach((t) -> t.getDependencies2().forEach((d) -> {
			 if(d.getTargetType().hasProperty(TAG_EXTERNAL))
				 t.setTagProperty(TAG_EXTERNAL_DEPENDENCY);
		}));
		
//		model.getTypes().stream().filter((t) -> !t.hasProperty(TAG_EXTERNAL)).forEach((t) -> {
//			t.getInterfaces().forEach((i) -> {
//				if(i.hasProperty(TAG_EXTERNAL))
//					t.setTagProperty(TAG_EXTERNAL_DEPENDENCY);
//			});
//			
//			
//			
//			if(t instanceof JInterface) {
//				((JInterface)t).getSuperInterfaces().forEach((i) -> {
//					if(i.hasProperty(TAG_EXTERNAL))
//						t.setTagProperty(TAG_EXTERNAL_DEPENDENCY);
//				});
//			}
//			else {
//				List<JClass> superclasses = ((JClass) t).getSuperclasses();
//				JClass superclass = superclasses.isEmpty() ? null : superclasses.get(0);
//				if(superclass != null && superclass.hasProperty(TAG_EXTERNAL))
//					t.setTagProperty(TAG_EXTERNAL_DEPENDENCY);
//			}
//		});
	}
}
