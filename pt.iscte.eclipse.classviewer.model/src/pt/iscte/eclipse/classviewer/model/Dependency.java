package pt.iscte.eclipse.classviewer.model;

public class Dependency {
	public enum Kind {
		INHERITANCE, INTERFACE, CALL, FIELD;
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

	public static Dependency ofInterface(JType source, JType target) {
		return new Dependency(source, target, Kind.INTERFACE);
	}
	
	public static Dependency ofField(JType source, JType target) {
		return new Dependency(source, target, Kind.FIELD);
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
