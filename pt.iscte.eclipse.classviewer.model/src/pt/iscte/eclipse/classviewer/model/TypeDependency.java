package pt.iscte.eclipse.classviewer.model;

public class TypeDependency implements IDependency {

	private final JType sourceType;
	private final JType targetType;
	
	public TypeDependency(JType sourceType, JType targetType) {
		Util.checkNotNull(sourceType, targetType);
		this.sourceType = sourceType;
		this.targetType = targetType;
	}

	@Override
	public JType getSourceType() {
		return sourceType;
	}

	@Override
	public JType getTargetType() {
		return targetType;
	}
	
	@Override
	public String toString() {
		return sourceType + " -> " + targetType;
	}

}
