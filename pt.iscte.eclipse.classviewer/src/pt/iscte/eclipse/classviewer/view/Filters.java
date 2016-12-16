package pt.iscte.eclipse.classviewer.view;

import java.util.Arrays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import pt.iscte.eclipse.classviewer.model.JAssociation;
import pt.iscte.eclipse.classviewer.model.CallDependency;
import pt.iscte.eclipse.classviewer.model.IDependency;
import pt.iscte.eclipse.classviewer.model.IModelFilter;
import pt.iscte.eclipse.classviewer.model.JField;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JType;
import pt.iscte.eclipse.classviewer.model.NamedElement;
import pt.iscte.eclipse.classviewer.model.StereotypedElement;

class Filters {


	static class TagFilter implements IModelFilter {
		private final String tag;
		private final boolean hasTag;
		
		public TagFilter(String tag, boolean hasTag) {
			this.tag = tag;
			this.hasTag = hasTag;
		}
		
		private boolean acceptInternal(NamedElement e) {
			return !e.hasProperty(tag) && hasTag || e.hasProperty(tag) && !hasTag;
		}
		
		@Override
		public boolean accept(JType type) {
			return acceptInternal(type);
		}

		@Override
		public boolean accept(JField field) {
			return acceptInternal(field);
		}

		@Override
		public boolean accept(JAssociation association) {
			return acceptInternal(association);
		}

		@Override
		public boolean accept(JOperation operation) {
			return acceptInternal(operation);
		}

		@Override
		public boolean accept(IDependency dependency) {
			return true;
		}

		@Override
		public String getName() {
			return "Tag '" + tag + "'";
		}
	}
	
	static class InterfaceFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(JType type) {
			return !type.isInterface();
		}
	}
	
	static class OperationFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(JOperation operation) {
			return false;
		}
	}
	
	static class FieldFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(JField field) {
			return false;
		}
	}
	
	static class CallDependencyFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(IDependency dependency) {
			return !(dependency instanceof CallDependency);
		}
	}
	
	
	static class NonBoundaryFilter implements IModelFilter {
		
		@Override
		public boolean accept(JType type) {
			return type.hasProperty(EXTERNAL) || type.hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(JField field) {
			return field.getOwner().hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(JAssociation association) {
			return association.getOwner().hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(JOperation operation) {
			return operation.getOwner().hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(IDependency dependency) {
			return 
					dependency.getSourceType().hasProperty(EXTERNAL_DEP) &&
					dependency.getTargetType().hasProperty(EXTERNAL) 
					||
					dependency.getSourceType().hasProperty(EXTERNAL_DEP) &&
					dependency.getTargetType().hasProperty(EXTERNAL_DEP);
		}

		@Override
		public String getName() {
			return "Non-boundary classes";
		}
	}
	
	static class TagFilterOR implements IModelFilter {
		private final String name;
		private final NonBoundaryFilter[] filters;
		
		public TagFilterOR(String name, NonBoundaryFilter ... filters) {
			this.name = name;
			this.filters = filters;
		}
		
		@Override
		public boolean accept(JType type) {
			for(NonBoundaryFilter f : filters)
				if(f.accept(type))
					return true;
			return false;
		}

		@Override
		public boolean accept(JField field) {
			for(NonBoundaryFilter f : filters)
				if(f.accept(field))
					return true;
			return false;
		}

		@Override
		public boolean accept(JAssociation association) {
			for(NonBoundaryFilter f : filters)
				if(f.accept(association))
					return true;
			return false;
		}

		@Override
		public boolean accept(JOperation operation) {
			for(NonBoundaryFilter f : filters)
				if(f.accept(operation))
					return true;
			return false;
		}

		@Override
		public boolean accept(IDependency dependency) {
			for(NonBoundaryFilter f : filters)
				if(f.accept(dependency))
					return true;
			return false;
		}

		@Override
		public String getName() {
			return name;
		}
		
	}
	
	
	private static final ViewerFilter NO_CALLS = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !(element instanceof NamedElement) || !((NamedElement) element).hasProperty("UNUSED");
		}
		public String toString() { return "Unused operations"; };

	};
	
	static class UnusedOperationsFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(JOperation operation) {
			return !operation.hasProperty("UNUSED");
		}
		@Override
		public String getName() {
			return "Unused operations";
		}
	}
	
	

	static class Filter extends ViewerFilter {
		final IModelFilter mFilter;
		Filter(IModelFilter mFilter) {
			this.mFilter = mFilter;
		}

		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(element instanceof JType) {
				return mFilter.accept((JType) element);
			}
			else if(element instanceof JField) {
				return mFilter.accept((JField) element);
			}
			else if(element instanceof JAssociation) {
				return mFilter.accept((JAssociation) element);
			}
			else if(element instanceof JOperation) {
				return mFilter.accept((JOperation) element);
			}
			else if(element instanceof IDependency) {
				return mFilter.accept((IDependency) element);
			}
			return true;
		}
		
		public boolean select(JOperation operation) {
			return mFilter.accept(operation);
		}

		public String getName() {
			return mFilter.getName();
		}
	}
	
	
	private static final ViewerFilter DEPS = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !(element instanceof CallDependency);
		}

		public String toString() { return "Dependencies"; };
	};


	static class IgnoreFilter extends ViewerFilter {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	static final String EXTERNAL = "EXTERNAL";
	static final String EXTERNAL_DEP = "EXTERNAL_DEPENDENCY";
	
	
	static final Filter[] FILTERS = {
			new Filter(new CallDependencyFilter()),
			new Filter(new FieldFilter()),
			new Filter(new OperationFilter()),
			
			new Filter(new UnusedOperationsFilter()),
			new Filter(new TagFilter(EXTERNAL, true)),
			new Filter(new NonBoundaryFilter()),
			new Filter(new InterfaceFilter())
	};
	
	
}
