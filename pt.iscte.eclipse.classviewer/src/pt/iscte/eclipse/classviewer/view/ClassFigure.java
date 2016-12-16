package pt.iscte.eclipse.classviewer.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import pt.iscte.eclipse.classviewer.model.IDependency;
import pt.iscte.eclipse.classviewer.model.JClass;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JType;
import pt.iscte.eclipse.classviewer.model.Stereotype;

public class ClassFigure extends Figure {

	private static final Color CLASS_COLOR = new Color(null, 245, 245, 245);
	private static final Color EXTERNAL_COLOR = new Color(null, 220, 220, 220);

	private static final Font CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD);
	private static final Font ABSTRACT_CLASS_FONT = new Font(null, "Arial", 12, SWT.BOLD | SWT.ITALIC);
	private static final Font INTERFACE_DESC_FONT = new Font(null, "Arial", 12, SWT.NONE);
	private static final Font METHOD_FONT = new Font(null, "Arial", 12, SWT.NONE);
	private static final Font ABSTRACT_METHOD_FONT = new Font(null, "Arial", 12, SWT.ITALIC);
	private static final Color TOOLTIP_COLOR = new Color(null, 255, 255, 206);
	private static final Font TOOLTIP_FONT = new Font(null, "Arial", 11, SWT.NONE);

//	private CompartmentFigure headerCompartment;
	private CompartmentFigure fieldsCompartment;
	private CompartmentFigure methodsCompartment;

	private JType type;
	private Map<JOperation, Label> operationLabels;
	private Label nameLabel;
	

	ClassFigure(JType type) {
		this.type = type;
		operationLabels = new HashMap<>();
		
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);

		setBorder(false);
		setBackgroundColor(type.hasProperty("EXTERNAL") ? EXTERNAL_COLOR : CLASS_COLOR);
		setOpaque(true);

//		headerCompartment = 
				createHeader(type);
				
		fieldsCompartment = new CompartmentFigure();
//		fieldsCompartment.setBorder(new MarginBorder(5,5,5,5));
		add(fieldsCompartment);
		
		if(type.isClass())
			createFields((JClass) type);
		
		
		
				
//		add(headerCompartment);
		methodsCompartment = new CompartmentFigure();
		add(methodsCompartment);
		
		type.getOperations().forEach((o) -> addMethodLabel(o));
		setSize(-1, -1);
	}

	private void createFields(JClass c) {
		c.getFields().forEach((f) -> {
			if(f.getType().hasProperty("VALUE_TYPE")) {
				String text = f.getVisibility().symbol() + " " + f.getName() + " : " + f.getType().getName();
				Label label = f.isStatic() ? new LabelFigure(text) : new Label(text);
				label.setFont(METHOD_FONT);
				label.setBorder(new MarginBorder(1));
				fieldsCompartment.add(label);
			}
				
		});
	}

	public JType getJType() {
		return type;
	}

	
	private void setBorder(boolean selected) {
		if(type.isClass() && ((JClass) type).isAbstract())
			setBorder(new LineBorder(selected ? ColorConstants.blue : ColorConstants.gray, 2, Graphics.LINE_DASH));
		else
			setBorder(new LineBorder(selected ? ColorConstants.blue : ColorConstants.black, 2));
	}
	public void select() {
		setBorder(true);
		nameLabel.setForegroundColor(ColorConstants.blue);
//		setBackgroundColor(TOOLTIP_COLOR);
	}
	
	public void unselect() {
		setBorder(false);
		nameLabel.setForegroundColor(ColorConstants.black);
//		setBackgroundColor(type.hasProperty("EXTERNAL") ? EXTERNAL_COLOR : CLASS_COLOR);
	}


	JType getType() {
		return type;
	}

	JOperation getOperation(Label label) {
		for(Entry<JOperation, Label> e : operationLabels.entrySet())
			if(e.getValue().equals(label))
				return e.getKey();
		return null;
	}
	
	public void hide(Collection<JOperation> operations) {
		operations.forEach((o) -> hide(o));
	}
	
	public void hide(JOperation operation) {
		Label label = operationLabels.get(operation);
		methodsCompartment.remove(label);
		operationLabels.remove(label);
	}
	
	public void setFilter(ViewerFilter filter) {
		for(Entry<JOperation, Label> e : operationLabels.entrySet()) {
			if(!filter.select(null, null, e.getKey())) {
				methodsCompartment.remove(e.getValue());
				operationLabels.remove(e.getValue());
			}
		}
	}
	
	public void mark(JOperation operation) {
		assert operation != null;
		operationLabels.get(operation).setForegroundColor(ColorConstants.blue);
	}
	
	public void mark(Collection<JOperation> operations) {
		operations.forEach((o) -> mark(o));
	}
	
	public void removeMarks() {
		operationLabels.values().forEach((l) -> l.setForegroundColor(ColorConstants.black));
	}
	
	private CompartmentFigure createHeader(final JType type) {
		Font font = null;
//		CompartmentFigure header = new CompartmentFigure();
//		header.setBorder(null);
//		header.setLayoutManager(new ToolbarLayout());
		if(type.isInterface()) {
			Label label = new Label("<<interface>>");
			label.setFont(INTERFACE_DESC_FONT);
//			header.add(label);
			add(label);
			font = CLASS_FONT;
		}
		else if (type.isClass() && ((JClass) type).isAbstract()) { //Abstract
			font = ABSTRACT_CLASS_FONT;
		}
		else { //Class
			font = CLASS_FONT;
			
		}
		
		for(Stereotype s : type.getSterotypes()) {
			Label label = new Label("<<" + s.getName() + ">>");
			label.setFont(INTERFACE_DESC_FONT);
			add(label);
		}
			

		nameLabel = new Label(type.getName());
		nameLabel.setFont(font);
		Label toolTipLabel = createToolTipLabel(type.getQualifiedName() + "\n" + pretty(type.getProperties()));
		nameLabel.setBorder(new MarginBorder(3,10,3,10));
//		header.setToolTip(toolTipLabel);
		nameLabel.setToolTip(toolTipLabel);
		
//		nameLabel.setBackgroundColor(new Color(null, 255, 0, 0));
//		header.add(nameLabel);
		add(nameLabel);
		return null;
	}


	
	private String pretty(Map<String, String> properties) {
		return "[" + String.join(",", properties.keySet()) + "]";
	}

	private void addMethodLabel(final JOperation operation) {
		String text = operation.getVisibility().symbol() + " " + operation.getName() + "()";
		Label methodLabel = operation.isStatic() ? new LabelFigure(text) : new Label(text);
		methodLabel.setFont(operation.isAbstract() ? ABSTRACT_METHOD_FONT : METHOD_FONT);
		methodLabel.setBorder(new MarginBorder(1));
	
		operationLabels.put(operation, methodLabel);
		
		String methodDesc = getMethodDescription(operation);
		Label toolTipLabel = createToolTipLabel(methodDesc);

		methodLabel.setToolTip(toolTipLabel);
		methodsCompartment.add(methodLabel);
	}

	private String getMethodDescription(JOperation operation) {
		StringBuilder builder = new StringBuilder();
		builder.append(operation.getName()).append("(...)");
		return builder.toString();
	}


		
	private Label createToolTipLabel(String text) {
		Label toolTipLabel =  new Label(text);
		toolTipLabel.setFont(TOOLTIP_FONT);
		toolTipLabel.setBackgroundColor(TOOLTIP_COLOR);
		toolTipLabel.setOpaque(true);
		return toolTipLabel;
	}

	
	private class Anchor extends AbstractConnectionAnchor {

		private boolean isExtendsOrImplements;
		
		public Anchor(boolean isExtendsOrImplements) {
			super(ClassFigure.this);
			this.isExtendsOrImplements = isExtendsOrImplements;
		}

		@Override
		public Point getLocation(Point reference) {
			org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
			r.setBounds(getOwner().getBounds());
			r.translate(0, 0);
			r.resize(1, 1);
			getOwner().translateToAbsolute(r);
			Point[] anchors = isExtendsOrImplements ? createMiddlePoints(r) : createPoints(r, 2);
			Point p = minDistance(reference, anchors);
			return p;
		}

		private Point minDistance(Point reference, Point[] anchors) {
			Point p = anchors[0];
			for(int i = 1; i < anchors.length; i++)
				if(reference.getDistance(anchors[i]) < reference.getDistance(p))
					p = anchors[i];
			return p;
		}
		
		private Point[] createMiddlePoints(org.eclipse.draw2d.geometry.Rectangle r) {
			return new Point[] {
					new Point(r.x + r.width/2, r.y),
					new Point(r.x + r.width/2, r.y + r.height),
					new Point(r.x, r.y + r.height/2),
					new Point(r.x + r.width, r.y + r.height/2)
			};
		}
		
		private Point[] createPoints(org.eclipse.draw2d.geometry.Rectangle r, int nSide) {
			Point[] points = new Point[nSide*4];
			int xStep = r.width/(nSide+1);
			int yStep = r.height/(nSide+1);

			int w = 0;
			for(int i = 0, x = r.x + xStep, y = r.y + yStep; i < nSide; i++, x += xStep, y += yStep) {
				points[w++] = new Point(x, r.y);
				points[w++] = new Point(x, r.y + r.height);
				points[w++] = new Point(r.x, y);
				points[w++] = new Point(r.x + r.width, y);				
			}
			
			return points;
		}
		
	}

	enum Position {
		TOP {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y);
			}
		},
		RIGHT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width, r.y + r.height/2);
			}
		},
		BOTTOM {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y + r.height);
			}
		},
		LEFT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + r.height/2);
			}
		};
		
		abstract Point getPoint(org.eclipse.draw2d.geometry.Rectangle r);
	}
	
	private class PositionAnchor extends AbstractConnectionAnchor {

		private Position position;
		
		public PositionAnchor(Position position) {
			super(ClassFigure.this);
			this.position = position;
		}

		@Override
		public Point getLocation(Point reference) {
			org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
			r.setBounds(getOwner().getBounds());
			r.translate(0, 0);
			r.resize(1, 1);
			getOwner().translateToAbsolute(r);
			return position.getPoint(r);
		}

	}
	
	PositionAnchor[] pos = {
			new PositionAnchor(Position.BOTTOM),
			new PositionAnchor(Position.LEFT),
			new PositionAnchor(Position.TOP),
			new PositionAnchor(Position.RIGHT),
	};
	
	int i = 0;
	
	public ConnectionAnchor createIncomingAnchor(IDependency d) {
//		ConnectionAnchor a = new PositionAnchor(Position.values()[i]);
//		i = (i + 1) % pos.length;
//		return a;
//		if(d.isSelfDependency())
			return new PositionAnchor(Position.BOTTOM);
//		return new Anchor(d instanceof ClassExtension || d instanceof InterfaceImplementation);
	}
	
	public ConnectionAnchor createOutgoingAnchor(IDependency d) {
//		if(d.isSelfDependency())
			return new PositionAnchor(Position.TOP);
		
//		return new Anchor(d instanceof ClassExtension || d instanceof InterfaceImplementation);
	}
	
}