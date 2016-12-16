package pt.iscte.eclipse.classviewer.model;

public class InterfaceImplementation implements IDependency {

	private final JType implementerType;
	private final JInterface interfaceType;
	
	public InterfaceImplementation(JType implementerType, JInterface interfaceType) {
		Util.checkNotNull(interfaceType, implementerType);
		this.implementerType = implementerType;
		this.interfaceType = interfaceType;
	}

	@Override
	public JType getSourceType() {
		return implementerType;
	}

	@Override
	public JType getTargetType() {
		return interfaceType;
	}

}
