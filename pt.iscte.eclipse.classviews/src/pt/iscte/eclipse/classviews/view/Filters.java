package pt.iscte.eclipse.classviews.view;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import pt.iscte.eclipse.classviews.IModelFilter;
import pt.iscte.eclipse.classviews.model.CallDependency;
import pt.iscte.eclipse.classviews.model.ClassExtension;
import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.InterfaceImplementation;
import pt.iscte.eclipse.classviews.model.JAssociation;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VPackage;
import pt.iscte.eclipse.classviews.model.VType;
import pt.iscte.eclipse.classviews.model.NamedElement;

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
		public boolean accept(VType type) {
			return acceptInternal(type);
		}

		@Override
		public boolean accept(VField field) {
			return acceptInternal(field);
		}

		@Override
		public boolean accept(JAssociation association) {
			return acceptInternal(association);
		}

		@Override
		public boolean accept(VOperation operation) {
			return acceptInternal(operation);
		}

		@Override
		public boolean accept(IDependency dependency) {
			return true;
		}
	}
	
	static class InterfaceFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(VType type) {
			return !type.isInterface();
		}
	}
	
	static class OperationFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(VOperation operation) {
			return false;
		}
	}
	
	static class FieldFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(VField field) {
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
		public boolean accept(VType type) {
			return type.hasProperty(EXTERNAL) || type.hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(VField field) {
			return field.getOwner().hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(JAssociation association) {
			return association.getOwner().hasProperty(EXTERNAL_DEP);
		}

		@Override
		public boolean accept(VOperation operation) {
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
	}
	
	static class TagFilterOR implements IModelFilter {
		private final NonBoundaryFilter[] filters;
		
		public TagFilterOR(NonBoundaryFilter ... filters) {
			this.filters = filters;
		}
		
		@Override
		public boolean accept(VType type) {
			for(NonBoundaryFilter f : filters)
				if(f.accept(type))
					return true;
			return false;
		}

		@Override
		public boolean accept(VField field) {
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
		public boolean accept(VOperation operation) {
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

		
		
	}
	
	
	private static final ViewerFilter NO_CALLS = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !(element instanceof NamedElement) || !((NamedElement) element).hasProperty("UNUSED");
		}
		public String toString() { return "Unused operations"; };

	};
	
	static class UnusedOperationsFilter extends IModelFilter.Adapter {
		@Override
		public boolean accept(VOperation operation) {
			return !operation.hasProperty("UNUSED");
		}
	}
	
	

	static class Filter extends ViewerFilter {
		final IModelFilter mFilter;
		final String name;
		final boolean positive;
		
		Filter(IModelFilter mFilter, String name, boolean positive) {
			this.mFilter = mFilter;
			this.name = name;
			this.positive = positive;
		}

		Filter(IModelFilter mFilter) {
			this(mFilter, mFilter.getClass().getSimpleName(), true);
		}
		
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(element instanceof VType) {
				return positive == mFilter.accept((VType) element);
			}
			else if(element instanceof VField) {
				return positive ==  mFilter.accept((VField) element);
			}
			else if(element instanceof JAssociation) {
				return positive == mFilter.accept((JAssociation) element);
			}
			else if(element instanceof VOperation) {
				return positive == mFilter.accept((VOperation) element);
			}
			else if(element instanceof IDependency) {
				return positive == mFilter.accept((IDependency) element);
			}
			return true;
		}
		
		public boolean select(VOperation operation) {
			return positive == mFilter.accept(operation);
		}

		public boolean select(VField field) {
			return positive == mFilter.accept(field);
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	
	static class PackageFilter extends IModelFilter.Adapter {
		private final Collection<String> packagesToFilter;
		
//		public PackageFilter(Collection<String> collection) {
//			this.packagesToFilter = packagesToFilter;
//		}
		
		public PackageFilter(Collection<VPackage> collection) {
			this.packagesToFilter =  new ArrayList<>(collection.size());
			for(VPackage p : collection)
				packagesToFilter.add(p.getName());
		}
		
		public PackageFilter(String ... packages) {
			packagesToFilter = new ArrayList<>(packages.length);
			for(String p : packages)
				packagesToFilter.add(p);
		}
		
		public PackageFilter(VPackage pck) {
			this.packagesToFilter = new ArrayList<>(1);
			packagesToFilter.add(pck.getName());
		}
		
		@Override
		public boolean accept(VType type) {
			return packagesToFilter.contains(type.getPackageName());
		}
	}


	static class IgnoreFilter extends ViewerFilter {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	static class OnlyHierarchy extends IModelFilter.Adapter {
		@Override
		public boolean accept(IDependency dependency) {
			return dependency instanceof ClassExtension || dependency instanceof InterfaceImplementation;
		}
		
		@Override
		public boolean accept(VField field) {
			return false;
		}
		
		@Override
		public boolean accept(VOperation operation) {
			return false;
		}
		
		@Override
		public boolean accept(JAssociation association) {
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
			new Filter(new InterfaceFilter()),
			new Filter(new OnlyHierarchy())
	};
	
	
}
