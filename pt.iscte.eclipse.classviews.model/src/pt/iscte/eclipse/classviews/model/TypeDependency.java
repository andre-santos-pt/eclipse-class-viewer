package pt.iscte.eclipse.classviews.model;

public class TypeDependency implements IDependency {

	private final VType sourceType;
	private final VType targetType;
	
	public TypeDependency(VType sourceType, VType targetType) {
		Util.checkNotNull(sourceType, targetType);
		this.sourceType = sourceType;
		this.targetType = targetType;
	}

	@Override
	public VType getSourceType() {
		return sourceType;
	}

	@Override
	public VType getTargetType() {
		return targetType;
	}
	
	@Override
	public String toString() {
		return sourceType + " -> " + targetType;
	}

}
