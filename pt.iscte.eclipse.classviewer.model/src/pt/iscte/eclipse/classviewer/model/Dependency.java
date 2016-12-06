package pt.iscte.eclipse.classviewer.model;

public class Dependency {
	public enum Kind {
		INHERITANCE, INTERFACE, METHOD, ATTRIBUTE;
	}
	
	private final JType source;
	private final JType target;
	private final Kind kind;
	
	public Dependency(JType source, JType target, Kind depType) {
//		checkNo
		this.source = source;
		this.target = target;
		this.kind = depType;
	}
	
	public static Dependency ofInheritance(JType source, JType target) {
		return new Dependency(source, target, Kind.INHERITANCE);
	}

	public Kind getKind() {
		return kind;
	}
	
	@Override
	public String toString() {
		return source + " -> " + target + " (" + kind + ")";
	}

	public JType getSourceType() {
		return source;
	}
	
	public JType getTargetType() {
		return target;
	}
}
