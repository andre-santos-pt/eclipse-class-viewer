package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

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
import java.util.Set;

public class JModel implements Iterable<JType>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Set<JPackage> packages;
	
	private final Map<String, JType> types;
	private String description;
	
	public JModel() {
		types = new HashMap<String, JType>();
		packages = new HashSet<JPackage>();
	}
	
	public JModel(String description) {
		this();
		this.description = description;
	}
	
	public void addType(JType type) {
		checkNotNull(type);
		types.put(type.getQualifiedName(), type);
	}
	
	public void addPackage(JPackage pkg) {
		checkNotNull(pkg);
		packages.add(pkg);
	}
	
	public Collection<JType> getTypes() {
		return Collections.unmodifiableCollection(types.values());
	}
	
	
	@Override
	public Iterator<JType> iterator() {
		List<JType> list = new ArrayList<>(types.values());
		list.sort(new Comparator<JType>() {
			public int compare(JType a, JType b) {
				if(a instanceof JClass && b instanceof JClass) {
					JClass aa = (JClass) a;
					JClass bb = (JClass) b;
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
	
	public Iterator<JPackage> getPackages() {
		return Collections.unmodifiableSet(packages).iterator();
	}
	
	public Iterator<JClass> classes() {
		
		return new Iterator<JClass>() {
			
			private JType[] array = types.values().toArray(new JType[types.values().size()]);
			private int i = 0;
			
			@Override
			public boolean hasNext() {
				if(i == array.length)
					return false;
				else
					for(int j = i; j < array.length; j++)
						if(array[j] instanceof JClass)
							return true;
				
				return false;
			}

			@Override
			public JClass next() {
				if(!hasNext())
					throw new IllegalStateException("iterator !hasNext");
				
				while(!(array[i] instanceof JClass) && i != array.length)
					i++;
				
				return (JClass) array[i];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean hasDescription() {
		return description != null;
	}

	public JType getType(String qualifiedName) {
		return types.get(qualifiedName);
	}

	public JClass getClass(String qualifiedName) {
		JType t = types.get(qualifiedName);
		return t instanceof JClass ? (JClass) t : null;
	}
	
	public JInterface getInterface(String qualifiedName) {
		JType t = types.get(qualifiedName);
		return t instanceof JInterface ? (JInterface) t : null;
	}
	
	public boolean hasType(String qualifiedName) {
		return types.containsKey(qualifiedName);
	}
	
	public Collection<JType> merge(JModel model) {
		Collection<JType> excluded = new ArrayList<JType>();
		for(JType ft : model) {
			String qName = ft.getQualifiedName();
			if(hasType(qName)) {
				JType t = getType(qName);
				if(t.equals(ft))
					t.merge(ft);
				else
					excluded.add(ft);
			}
			else {
				addType(ft);
			}
		}
		return excluded;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		for(JType t : types.values()) {
			s.append(t + "\n");
			for(JOperation o : t)
				s.append("\t" + o + "\n");
			s.append("\n");
		}
		return s.toString();
	}
	
	// TODO
	public String getCommonSubPackageName() {
		return "";
	}

}
