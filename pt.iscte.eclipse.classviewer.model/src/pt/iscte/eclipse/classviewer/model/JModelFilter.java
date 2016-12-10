package pt.iscte.eclipse.classviewer.model;

public interface JModelFilter {

	boolean accept(JType type);
	boolean accept(JOperation operation);
	boolean accept(JPackage pck);
}
