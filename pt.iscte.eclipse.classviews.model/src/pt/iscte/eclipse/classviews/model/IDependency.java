package pt.iscte.eclipse.classviews.model;

public interface IDependency {

	VType getSourceType();
	VType getTargetType();
	
	default boolean isSelfDependency() {
		return getSourceType().equals(getTargetType());
	}
}
