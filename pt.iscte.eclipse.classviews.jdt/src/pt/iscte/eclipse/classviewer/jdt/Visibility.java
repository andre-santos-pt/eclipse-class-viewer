package pt.iscte.eclipse.classviewer.jdt;

public enum Visibility {
	PRIVATE("-"),
	PACKAGE("~"),
	PROTECTED("#"),
	PUBLIC("+");
	
	private final String symbol;
	
	private Visibility(String symbol) {
		this.symbol = symbol;
	}
	
	public String symbol() {
		return symbol;
	}
}
