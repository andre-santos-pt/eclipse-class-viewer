package pt.iscte.eclipse.classviews.model;

public class ClassMember extends StereotypedElement {
	private static final long serialVersionUID = 1L;

	private boolean isStatic;
	
	ClassMember(String name) {
		super(name);
		isStatic = false;
	}
	
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public boolean isStatic() {
		return isStatic;
	}
	
	
}
