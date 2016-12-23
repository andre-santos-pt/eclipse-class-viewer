package pt.iscte.eclipse.classviews.view;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;

import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VType;
import pt.iscte.eclipse.classviews.model.TypeDependency;

class NodeProvider implements IGraphEntityRelationshipContentProvider {

		@Override
		public Object[] getElements(Object input) {
			return ((VModel) input).sortedTypes().stream()
					.filter((t) -> !t.hasProperty("VALUE_TYPE"))
					.toArray();
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getRelationships(Object source, Object target) {
			VType sourceType = (VType) source;
			VType targetType = (VType) target;

//			List<Dependency> dependencies = sourceType.getDependencies(targetType);
//			Iterator<Dependency> iterator = dependencies.iterator();
//			while(iterator.hasNext()) {
//				Dependency d = iterator.next();
//				if(d instanceof FieldDependency && d.getTargetType().isClass()) {
//					FieldDependency fd = (FieldDependency) d;
//					if(fd.getCardinality().isUnary() && ((JClass) fd.getTargetType()).hasNonUnaryDependencyTo(fd.getSourceType()))
//						iterator.remove();
//				}
//			}
			Collection<IDependency> dependencies = sourceType.getDependencies2(targetType);
//			Iterator<IDependency> iterator = dependencies.iterator();
//			while(iterator.hasNext()) {
//				IDependency d = iterator.next();
//				if(d instanceof JField && d.getTargetType().isClass()) {
//					JField fd = (JField) d;
//					if(fd.getCardinality().isUnary() && ((JClass) fd.getTargetType()).hasNonUnaryDependencyTo(fd.getSourceType()))
//						iterator.remove();
//				}
//			}
			
//			iterator = dependencies.iterator();
//			while(iterator.hasNext()) {
//				Dependency d = iterator.next();
//				if(d instanceof CallDependency) {
//					CallDependency cd = (CallDependency) d;
//					boolean remove = false;
//					for(Dependency d2 : dependencies)
//						if(d2 instanceof FieldDependency && ((FieldDependency) d2).getTargetType().equals(cd.getTargetType()))
//							remove = true;
//					
//					if(remove)
//						iterator.remove();
//				}
//			}
			Iterator<IDependency> iterator = dependencies.iterator();
			while(iterator.hasNext()) {
				IDependency d = iterator.next();
				if(d instanceof TypeDependency) {
					TypeDependency cd = (TypeDependency) d;
					boolean remove = false;
					for(IDependency d2 : dependencies)
						if(d2 instanceof VField && 
								(((VField) d2).getTargetType().equals(cd.getTargetType()) ||
								((VField) d2).isStatic()))
							remove = true;
					
					if(remove)
						iterator.remove();
				}
			}
			//			mergeCallDependencies(dependencies);
			return dependencies.toArray();
		}

		
	}
