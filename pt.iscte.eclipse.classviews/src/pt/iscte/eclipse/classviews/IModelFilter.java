package pt.iscte.eclipse.classviews;

import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.JAssociation;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public interface IModelFilter {
	boolean accept(VType type);
	boolean accept(VField field);
	boolean accept(JAssociation association);
	boolean accept(VOperation operation);
	boolean accept(IDependency dependency);
	
	public class Adapter implements IModelFilter {
		public boolean accept(VType type) { return true; }
		public boolean accept(VField field) { return true; }
		public boolean accept(JAssociation association) { return true; }
		public boolean accept(VOperation operation) { return true; }
		public boolean accept(IDependency dependency) { return true; }
	}
	
	
}

