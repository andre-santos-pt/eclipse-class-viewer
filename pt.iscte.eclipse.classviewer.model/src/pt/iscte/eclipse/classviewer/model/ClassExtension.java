package pt.iscte.eclipse.classviewer.model;

public class ClassExtension implements IDependency {

	private final JClass subclass;
	private final JClass superclass;
	
	public ClassExtension(JClass subclass, JClass superclass) {
		Util.checkNotNull(subclass, subclass);
		this.subclass = subclass;
		this.superclass = superclass;
	}

	@Override
	public JType getSourceType() {
		return subclass;
	}

	@Override
	public JType getTargetType() {
		return superclass;
	}

}
