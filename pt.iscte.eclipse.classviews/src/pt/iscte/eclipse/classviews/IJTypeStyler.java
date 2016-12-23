package pt.iscte.eclipse.classviews;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

public interface IJTypeStyler {
	String getNameText(VType t);
	String getToolip(VType t);
	Color getBackgroundColor(VType e);
	Color getForegroundColor(VType e);
	
	Font getNameFont(VType t);
	Font getStereotypeFont(VType t);
	Font getFieldFont(VField f);
	Font getOperationFont(VOperation o);
	
	
	Color getLineColor(VType e);
	int getLineStyle(VType e);
	
	IJTypeStyler DEFAULT = new DefaultStyle();
	
	class DefaultStyle implements IJTypeStyler {

		private static final Color CLASS_COLOR = new Color(null, 245, 245, 245);
		private static final Color EXTERNAL_COLOR = new Color(null, 220, 220, 220);
		private static final Font CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD);
		private static final Font FONT = new Font(null, "Arial", 10, SWT.NONE);
		
		@Override
		public String getNameText(VType t) {
			return t.getName();
		}

		@Override
		public String getToolip(VType t) {
			return t.getQualifiedName();
		}

		@Override
		public Color getBackgroundColor(VType t) {
			return t.hasProperty("EXTERNAL") ? EXTERNAL_COLOR : CLASS_COLOR;
		}

		@Override
		public Color getForegroundColor(VType t) {
			return ColorConstants.black;
		}
		

		@Override
		public Color getLineColor(VType t) {
			return ColorConstants.black;
		}

		@Override
		public int getLineStyle(VType t) {
			return Graphics.LINE_SOLID;
		}

		@Override
		public Font getNameFont(VType t) {
			return CLASS_FONT;
		}
		
		@Override
		public Font getStereotypeFont(VType t) {
			return FONT;
		}
		
		@Override
		public Font getFieldFont(VField f) {
			return FONT;
		}

		@Override
		public Font getOperationFont(VOperation o) {
			return FONT;
		}

		
		
	}
}
