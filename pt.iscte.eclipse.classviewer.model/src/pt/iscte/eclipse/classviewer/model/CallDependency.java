package pt.iscte.eclipse.classviewer.model;

public class CallDependency extends Dependency {
	
	private final JOperation source;
	private final JOperation target;
	
	public CallDependency(JOperation source, JOperation target) {
		super(source.getOwner(), target.getOwner(), Dependency.Kind.CALL);
		this.source = source;
		this.target = target;
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
