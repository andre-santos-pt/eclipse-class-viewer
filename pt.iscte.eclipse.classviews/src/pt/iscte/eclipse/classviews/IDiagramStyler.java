package pt.iscte.eclipse.classviews;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.VType;
import pt.iscte.eclipse.classviews.model.NamedElement;

public interface IDiagramStyler {

	String getText(NamedElement e);
	String getToolip(NamedElement e);
	Color getBackgroundColor(NamedElement e);
	Color getForegroundColor(NamedElement e);
	Font getFont(NamedElement e);
	
	Color getLineColor(VType e);
	int getLineStyle(VType e);
	
	Color getLineColor(IDependency d);
	Font getFont(IDependency e);
}
