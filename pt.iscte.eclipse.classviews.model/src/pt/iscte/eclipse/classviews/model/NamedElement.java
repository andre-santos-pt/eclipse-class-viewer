package pt.iscte.eclipse.classviews.model;

import static pt.iscte.eclipse.classviews.model.Util.checkNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public abstract class NamedElement implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String name;
	private Map<String, String> props;
	
	NamedElement(String name) {
		checkNotNull(name);
		this.name = name;
		props = Collections.emptyMap();
	}
	
	public String getName() {
		return name;
	}

	public String getProperty(String key) {
		checkNotNull(key);
		return props.get(key);
	}
	
	public boolean hasProperty(String key) {
		checkNotNull(key);
		return props.containsKey(key);
	}
	
	public void setTagProperty(String key) {
		setProperty(key, null);
	}
	
	public void setProperty(String key, String value) {
		checkNotNull(key);
//		checkNotNull(value);
		
		if(props.isEmpty())
			props = new HashMap<String, String>();
		
		props.put(key, value);
	}
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(props);
	}
	
}
