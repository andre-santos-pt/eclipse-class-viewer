package pt.iscte.eclipse.classviewer.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupedDependency extends Dependency {

	private Map<JOperation, JOperation> callMap;
	
	public GroupedDependency(JType source, JType target) {
		super(source, target, Dependency.Kind.METHOD);
		callMap = new HashMap<JOperation, JOperation>();
	}

	
	

}
