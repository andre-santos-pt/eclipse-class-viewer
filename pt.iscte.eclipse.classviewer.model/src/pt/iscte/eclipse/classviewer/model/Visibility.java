package pt.iscte.eclipse.classviewer.model;

import java.io.Serializable;

public enum Visibility implements Serializable {
	PRIVATE("-"),
	PACKAGE("~"),
	PROTECTED("#"),
	PUBLIC("+");
	
	private String symbol;
	
	private Visibility(String symbol) {
		this.symbol = symbol;
	}
	
	public String symbol() {
		return symbol;
	}
}
