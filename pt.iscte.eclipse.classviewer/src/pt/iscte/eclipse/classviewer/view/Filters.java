package pt.iscte.eclipse.classviewer.view;

import java.util.Arrays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import pt.iscte.eclipse.classviewer.model.Dependency;
import pt.iscte.eclipse.classviewer.model.NamedElement;

class Filters {

	private static class TagOrFilter extends ViewerFilter {
		final String[] tags;
		final boolean include;

		private TagOrFilter(String[] tags, boolean include) {
			this.tags = tags;
			this.include = include;
		}

		static TagOrFilter ofInclusion(String ... tags) {
			return new TagOrFilter(tags, true);
		}

		static TagOrFilter ofExclusion(String ... tags) {
			return new TagOrFilter(tags, false);
		}

		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(element instanceof NamedElement) {
				for(String tag : tags) {
					if(include && ((NamedElement) element).hasProperty(tag))
						return true;
					if(!include && !((NamedElement) element).hasProperty(tag))
						return false;
				}
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "Elements " + (include ? "without" : "with") + " tags " + Arrays.toString(tags);
		}

	}
	
	private static final ViewerFilter NO_CALLS = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !(element instanceof NamedElement) || !((NamedElement) element).hasProperty("UNUSED");
		}
		public String toString() { return "Unused operations"; };

	};

	private static final ViewerFilter DEPS = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !(element instanceof Dependency) || ((Dependency) element).getKind() != Dependency.Kind.CALL;
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
	
	static final ViewerFilter[] FILTERS = {
			DEPS, 
			NO_CALLS, 
			TagOrFilter.ofExclusion("EXTERNAL"), 
			TagOrFilter.ofInclusion("EXTERNAL_DEPENDENCY", "EXTERNAL")
	};
}
