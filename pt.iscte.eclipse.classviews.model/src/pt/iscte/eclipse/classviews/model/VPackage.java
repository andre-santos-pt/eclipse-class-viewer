package pt.iscte.eclipse.classviews.model;

import java.util.Iterator;

public final class VPackage implements Iterable<VType> {

	private final String name;
	private final VModel model;
	
	VPackage(String name, VModel model) {
		Util.checkNotNull(name, model);
		// TODO regex
		this.name = name;
		this.model = model;
	}
	
	@Override
	public Iterator<VType> iterator() {
		return model.getTypes((t) -> t.getPackageName().equals(name));
	}

	public String getName() {
		return isDefault() ? "(default)" : name;
	}
	
	public boolean isDefault() {
		return name.equals("");
	}
	
	@Override
	public String toString() {
		return getName();
	}

	
}
