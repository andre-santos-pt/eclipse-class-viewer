package pt.iscte.eclipse.classviewer.model;

import static pt.iscte.eclipse.classviewer.model.Util.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JModel implements Iterable<JType>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Map<String, JType> types;
	
	public JModel() {
		types = new HashMap<String, JType>();
	}
	
	public void addType(JType type) {
		checkNotNull(type);
		types.put(type.getQualifiedName(), type);
	}
	
	public Collection<JType> getTypes() {
		return Collections.unmodifiableCollection(types.values());
	}
	
	public List<JType> sortedTypes() {
		List<JType> list = new ArrayList<>(types.values());
		list.sort(new Comparator<JType>() {
			public int compare(JType a, JType b) {
				if(a instanceof JClass && b instanceof JClass) {
					JClass aa = (JClass) a;
					JClass bb = (JClass) b;
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
	
	public JType getType(String qualifiedName) {
		return types.get(qualifiedName);
	}

	public boolean hasType(String qualifiedName) {
		return types.containsKey(qualifiedName);
	}
	
	public JClass getClass(String qualifiedName) {
		JType t = types.get(qualifiedName);
		return t instanceof JClass ? (JClass) t : null;
	}
	
	public JInterface getInterface(String qualifiedName) {
		JType t = types.get(qualifiedName);
		return t instanceof JInterface ? (JInterface) t : null;
	}
	
	
	
	public int getDepth(JType type) {
		if(type instanceof JClass) {
			JClass c = (JClass) type;
			int max = 0;
			for(JClass s : c.getSuperclasses()) {
				int d = getDepth(s);
				if(d > max)
					max = d;
			}
			return 1 + max;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for(JType t : types.values()) {
			s.append(t + "\n");
//			for(JOperation o : t.getOperations())
//				s.append("\t" + o + "\n");
//			s.append("\n");
		}
		return s.toString();
	}
}
