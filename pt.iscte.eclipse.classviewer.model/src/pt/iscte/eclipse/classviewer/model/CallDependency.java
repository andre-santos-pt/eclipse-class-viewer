package pt.iscte.eclipse.classviewer.model;

public class CallDependency implements IDependency {
	
	private final JOperation source;
	private final JOperation target;
	
	public CallDependency(JOperation source, JOperation target) {
//		super(source.getOwner(), target.getOwner(), Dependency.Kind.CALL);
		Util.checkNotNull(source, target);
		this.source = source;
		this.target = target;
	}
	

	@Override
	public JType getSourceType() {
		return source.getOwner();
	}

	@Override
	public JType getTargetType() {
		return target.getOwner();
	}
	
	public JOperation getSourceOperation() {
		return source;
	}
	
	public JOperation getTargetOperation() {
		return target;
	}
	
	@Override
	public String toString() {
		return source + " -> " + target;
	}

}
