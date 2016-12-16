package pt.iscte.eclipse.classviewer.model;

public interface IModelFilter {
	boolean accept(JType type);
	boolean accept(JField field);
	boolean accept(JAssociation association);
	boolean accept(JOperation operation);
	boolean accept(IDependency dependency);
	String getName();
	
	public class Adapter implements IModelFilter {
		public boolean accept(JType type) { return true; }
		public boolean accept(JField field) { return true; }
		public boolean accept(JAssociation association) { return true; }
		public boolean accept(JOperation operation) { return true; }
		public boolean accept(IDependency dependency) { return true; }

		public String getName() {
			return this.getClass().getSimpleName();
		}
	}
}

