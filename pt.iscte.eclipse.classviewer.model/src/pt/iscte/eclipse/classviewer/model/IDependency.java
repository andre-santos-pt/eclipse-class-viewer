package pt.iscte.eclipse.classviewer.model;

public interface IDependency {

	JType getSourceType();
	JType getTargetType();
	
	default boolean isSelfDependency() {
		return getSourceType().equals(getTargetType());
	}
}
