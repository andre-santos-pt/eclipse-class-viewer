package pt.iscte.eclipse.classviews.model;

public class CallDependency implements IDependency {
	
	private final VOperation source;
	private final VOperation target;
	
	public CallDependency(VOperation source, VOperation target) {
//		super(source.getOwner(), target.getOwner(), Dependency.Kind.CALL);
		Util.checkNotNull(source, target);
		this.source = source;
		this.target = target;
	}
	

	@Override
	public VType getSourceType() {
		return source.getOwner();
	}

	@Override
	public VType getTargetType() {
		return target.getOwner();
	}
	
	public VOperation getSourceOperation() {
		return source;
	}
	
	public VOperation getTargetOperation() {
		return target;
	}
	
	@Override
	public String toString() {
		return source + " -> " + target;
	}

}
