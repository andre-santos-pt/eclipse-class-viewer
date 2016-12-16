package pt.iscte.eclipse.classviewer.view;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Font;

/**
 * Label that can be underlined and stroked through.
 */
public class LabelFigure extends Label {
    private boolean underline = false;

    private boolean strikeThrough = false;

    public LabelFigure(String text) {
    	super(text);
    	underline = true;
	}

    @Override
    protected void paintFigure(final Graphics graphics) {
        super.paintFigure(graphics);
        
        // Draw the underline
        if (this.underline || this.strikeThrough) {
            Dimension textSize = getSubStringTextSize();
            Point p1 = getBounds().getTopLeft();
            p1.translate(getTextLocation());
            if (this.underline) {
                int y = p1.y + textSize.height - 1;
        
                graphics.drawLine(p1.x, y, p1.x + textSize.width, y);
            }
            if (this.strikeThrough) {
                int y = p1.y + (textSize.height / 2) - 1;
        
                graphics.drawLine(p1.x, y, p1.x + textSize.width, y);
            }
        }
    }

    /**
     * Set whether the main label is underlined.
     * @param underline true to underline the main label
     */
    public void setUnderline(final boolean underline) {
        if (this.underline != underline) {
            this.underline = underline;
            repaint();
        }
    }

    /**
     * Set whether the main label is underlined.
     * @param strikeThrough true to strike the label
     */
    public void setStrikeThrough(final boolean strikeThrough) {
        if (this.strikeThrough != strikeThrough) {
            this.strikeThrough = strikeThrough;
            repaint();
        }
    }

}