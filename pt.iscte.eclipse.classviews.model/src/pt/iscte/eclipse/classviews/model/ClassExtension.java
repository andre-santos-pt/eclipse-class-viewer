package pt.iscte.eclipse.classviews.model;

public class ClassExtension implements IDependency {

	private final VClass subclass;
	private final VClass superclass;
	
	public ClassExtension(VClass subclass, VClass superclass) {
		Util.checkNotNull(subclass, subclass);
		this.subclass = subclass;
		this.superclass = superclass;
	}

	@Override
	public VType getSourceType() {
		return subclass;
	}

	@Override
	public VType getTargetType() {
		return superclass;
	}

}
