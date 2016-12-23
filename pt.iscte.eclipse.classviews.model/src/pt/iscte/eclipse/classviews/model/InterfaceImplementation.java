package pt.iscte.eclipse.classviews.model;

public class InterfaceImplementation implements IDependency {

	private final VType implementerType;
	private final VInterface interfaceType;
	
	public InterfaceImplementation(VType implementerType, VInterface interfaceType) {
		Util.checkNotNull(interfaceType, implementerType);
		this.implementerType = implementerType;
		this.interfaceType = interfaceType;
	}

	@Override
	public VType getSourceType() {
		return implementerType;
	}

	@Override
	public VType getTargetType() {
		return interfaceType;
	}

}
