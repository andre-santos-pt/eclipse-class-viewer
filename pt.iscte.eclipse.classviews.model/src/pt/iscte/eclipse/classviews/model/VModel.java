package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.function.Predicate;

public class VModel extends Observable implements Iterable<VType>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Map<String, VType> types;
	private final Map<String, VPackage> packages;
	
	public VModel() {
		types = new HashMap<String, VType>();
		packages = new HashMap<String, VPackage>();
	}
	
	public void addType(VType type) {
		checkNotNull(type);
		types.put(type.getQualifiedName(), type);
		String packageName = type.getPackageName();
		if(!packages.containsKey(packageName))
			packages.put(packageName, new VPackage(packageName, this));
		
		setChanged();
		notifyObservers(type);
	}
	
	public Collection<VType> getTypes() {
		return Collections.unmodifiableCollection(types.values());
	}
	
	public List<VType> sortedTypes() {
		List<VType> list = new ArrayList<>(types.values());
		list.sort(new Comparator<VType>() {
			public int compare(VType a, VType b) {
				if(a instanceof VClass && b instanceof VClass) {
					VClass aa = (VClass) a;
					VClass bb = (VClass) b;
					if(aa.compatibleWith(bb))
						return -1;
					else if(bb.compatibleWith(aa))
						return 1;
					else
						return 0;
				}
				else
					return 0;
			};
		});
		return list;
	}
	
	
	@Override
	public Iterator<VType> iterator() {
		List<VType> list = new ArrayList<>(types.values());
		list.sort(new Comparator<VType>() {
			public int compare(VType a, VType b) {
				if(a instanceof VClass && b instanceof VClass) {
					VClass aa = (VClass) a;
					VClass bb = (VClass) b;
					if(aa.compatibleWith(bb))
						return 1;
					else if(bb.compatibleWith(aa))
						return -1;
					else
						return 0;
				}
				else
					return 0;
			};
		});
		return list.iterator();
	}
	
	public VType getType(String qualifiedName) {
		return types.get(qualifiedName);
	}

	public boolean hasType(String qualifiedName) {
		return types.containsKey(qualifiedName);
	}
	
	public VClass getClass(String qualifiedName) {
		VType t = types.get(qualifiedName);
		return t instanceof VClass ? (VClass) t : null;
	}
	
	public VInterface getInterface(String qualifiedName) {
		VType t = types.get(qualifiedName);
		return t instanceof VInterface ? (VInterface) t : null;
	}
	
	
	
	public int getDepth(VType type) {
		if(type instanceof VClass) {
			VClass c = (VClass) type;
			int max = 0;
			for(VClass s : c.getSuperclasses()) {
				int d = getDepth(s);
				if(d > max)
					max = d;
			}
			return 1 + max;
		}
		return 0;
	}
	
	
	public Collection<VPackage> getPackages() {
		return packages.values();
	}
	
	public VPackage getPackage(String name) {
		return packages.get(name);
	}

	
	public Collection<String> getTags(Class<? extends NamedElement> type) {
		Set<String> tags = new HashSet<>();
		
		if(type.equals(VType.class)) {
			for(VType t : types.values())
				tags.addAll(t.getProperties().keySet());
		}
		else if(type.equals(VOperation.class)) {
			for(VType t : types.values())
				for(VOperation o : t.getOperations())
					tags.addAll(o.getProperties().keySet());
		}
		else
			throw new IllegalArgumentException("unsupported");
		return tags;
	}
	
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for(VType t : types.values()) {
			s.append(t + "\n");
//			for(JOperation o : t.getOperations())
//				s.append("\t" + o + "\n");
//			s.append("\n");
		}
		return s.toString();
	}
	
	public Iterator<VType> getTypesOfPackage(String packageName) {
		return new FilterIterator<>(types.values(), t -> t.getPackageName().equals(packageName));
	}
	
	public Iterator<VType> getTypes(Predicate<VType> predicate) {
		return new FilterIterator<>(types.values(), predicate);
	}
	
	private static class FilterIterator<T> implements Iterator<T> {
		private final Predicate<T> predicate;
		private final Iterator<T> iterator;
		private T next;
		
		public FilterIterator(Iterable<T> iterable, Predicate<T> predicate) {
			this.predicate = predicate;
			iterator = iterable.iterator();
			iterate(predicate);
		}

		private void iterate(Predicate<T> predicate) {
			while(iterator.hasNext() && next == null) {
				T e = iterator.next();
				if(predicate.test(e))
					next = e;
			}
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public T next() {
			if(next == null)
				throw new IllegalStateException();

			T tmp = next;
			iterate(predicate);
			return tmp;
		}
		
	}

}
