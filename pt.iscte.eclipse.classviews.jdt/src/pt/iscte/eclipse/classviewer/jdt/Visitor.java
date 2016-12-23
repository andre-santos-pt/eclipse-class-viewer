package pt.iscte.eclipse.classviewer.jdt;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import pt.iscte.eclipse.classviews.model.Cardinality;
import pt.iscte.eclipse.classviews.model.VClass;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public class Visitor extends ASTVisitor {
	private VModel model;
	private VType type;
	private IType astType;

	private boolean definesEquals;
	private boolean definesHashcode;
	
	
	public Visitor(VModel model, VType type, IType astType) {
		this.model = model;
		this.type = type;
		this.astType = astType;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if(type.isClass() && node.getParent() instanceof TypeDeclaration) {
			VariableDeclarationFragment var = (VariableDeclarationFragment) node.fragments().get(0);
			ITypeBinding tBind = node.getType().resolveBinding();
			boolean isCollection = tBind == null ? false : isCollection(tBind);

			String tName = ModelBuilder.resolve(astType, node.getType().toString());
			VType dep = model.getType(tName);
			if(isCollection && tBind != null) {
				if(tBind.isParameterizedType()) {
					for(ITypeBinding ta : tBind.getTypeArguments()){
						String qName = qualifiedNameWithoutGenerics(ta.getQualifiedName());
						dep = model.getType(qName);
					}
				}
			}

			if(dep == null) {
				dep = new VClass(tName);
				dep.setTagProperty(ModelBuilder.TAG_EXTERNAL);
				if(node.getType().isPrimitiveType() || tName.equals(String.class.getName()) || (tBind != null && tBind.isEnum()))
					dep.setTagProperty(ModelBuilder.TAG_VALUE_TYPE);
				model.addType(dep);
			}

			Cardinality card = Cardinality.zeroOne();
			if(isCollection || node.getType().isArrayType())
				card = Cardinality.zeroMany();

			new VField((VClass) type, var.getName().toString(), dep, card)
				.setStatic(Flags.isStatic(node.getFlags()));


		}
		return true;
	}

	private String qualifiedNameWithoutGenerics(String qName) {
		int i = qName.indexOf('<');
		return i == -1 ? qName : qName.substring(0, i);
	}

	private boolean isCollection(ITypeBinding t) {
		String qName = qualifiedNameWithoutGenerics(t.getQualifiedName());
		if(qName.equals(Collection.class.getName()) || qName.equals(Map.class.getName()))
			return true;
		else
			for(ITypeBinding si : t.getInterfaces())
				if(isCollection(si))
					return true;
		return false;

	}

	public boolean visit(MethodInvocation inv) {
		ASTNode parent = inv.getParent();
		while(!(parent instanceof MethodDeclaration) && parent != null) {
			parent = parent.getParent();
		}
		if(parent instanceof MethodDeclaration) {
			String callerMethodName = ((MethodDeclaration) parent).getName().getFullyQualifiedName();
			Expression exp = inv.getExpression();
			if(exp instanceof SimpleName) {
				ITypeBinding typeBinding = ((SimpleName) exp).resolveTypeBinding();
				if(typeBinding != null) {
					String targetTypeName = ModelBuilder.resolve(astType, typeBinding.isParameterizedType() ? typeBinding.getErasure().getName() : typeBinding.getName());
					VType targetType = model.getType(targetTypeName);
					if(targetType != null && !type.equals(targetType)) {
						VOperation sourceOperation = type.getOperation(callerMethodName);
						VOperation targetOperation = targetType.getOperation(inv.getName().getFullyQualifiedName());
						if(sourceOperation != null && targetOperation != null) {
							sourceOperation.addDependency2(targetOperation);
						}
					}
				}
			}
		}
		return true;
	}
	
	public boolean visit(MethodDeclaration node) {
		if(node.getName().toString().equals("equals") && 
			node.parameters().size() == 1 && node.parameters().get(0) != null) {
			definesEquals = true;
		}
		else if(node.getName().toString().equals("hashCode") && node.parameters().size() == 0) {
			definesHashcode = true;
		}
		return true;
	}
	
	public boolean isValueType() {
		return definesEquals && definesHashcode;
	}
}

