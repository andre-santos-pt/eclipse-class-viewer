package pt.iscte.eclipse.classviews.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Font;

import pt.iscte.eclipse.classviews.IJTypeStyler;
import pt.iscte.eclipse.classviews.IStereotypePolicy;
import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.VClass;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VType;

class ClassFigure extends Figure {

	//	private CompartmentFigure headerCompartment;
	private CompartmentFigure fieldsCompartment;
	private CompartmentFigure methodsCompartment;


	private VType type;
	private Map<VField, Label> fieldLabels;
	private Map<VOperation, Label> operationLabels;

	private Label nameLabel;

	private IJTypeStyler styler;

	ClassFigure(VType type, IJTypeStyler styler, IStereotypePolicy stereotypePolicy) {
		this.type = type;
		this.styler = styler;

		fieldLabels = new HashMap<>();
		operationLabels = new HashMap<>();

		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);

		setBorder(false);
		setBackgroundColor(styler.getBackgroundColor(type));
		setOpaque(true);

		//		headerCompartment = 
		createHeader(type, stereotypePolicy.getStereotypes(type));

		fieldsCompartment = new CompartmentFigure();
		//		fieldsCompartment.setBorder(new MarginBorder(5,5,5,5));
		add(fieldsCompartment);

		if(type.isClass())
			createFields((VClass) type);




		//		add(headerCompartment);
		methodsCompartment = new CompartmentFigure();
		add(methodsCompartment);

		type.getOperations().forEach((o) -> addOperationLabel(o));
		setSize(-1, -1);
	}

	private void createFields(VClass c) {
		c.getFields().forEach((f) -> {
			if(f.getType().hasProperty("VALUE_TYPE")) {
				String text = f.getName() + " : " + f.getType().getName();
				Label label = f.isStatic() ? new LabelFigure(text) : new Label(text);
				label.setFont(styler.getFieldFont(f));
				label.setBorder(new MarginBorder(1));
				fieldsCompartment.add(label);
				fieldLabels.put(f, label);
			}

		});
	}

	public VType getJType() {
		return type;
	}


	private void setBorder(boolean selected) {
		if(type.isClass() && ((VClass) type).isAbstract())
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


	VType getType() {
		return type;
	}

	VOperation getOperation(Label label) {
		for(Entry<VOperation, Label> e : operationLabels.entrySet())
			if(e.getValue().equals(label))
				return e.getKey();
		return null;
	}

	public void hide(Collection<VOperation> operations) {
		operations.forEach((o) -> hide(o));
	}

	public void hide(VOperation operation) {
		Label label = operationLabels.get(operation);
		if(label != null) {
			methodsCompartment.remove(label);
			operationLabels.remove(label);
		}
	}

	public void hide(VField field) {
		Label label = fieldLabels.get(field);
		if(label != null) {
			fieldsCompartment.remove(label);
			fieldLabels.remove(label);
		}
	}

	public void setFilter(ViewerFilter filter) {
		for(Entry<VOperation, Label> e : operationLabels.entrySet()) {
			if(!filter.select(null, null, e.getKey())) {
				methodsCompartment.remove(e.getValue());
				operationLabels.remove(e.getValue());
			}
		}
	}

	public void mark(VOperation operation) {
		assert operation != null;
		operationLabels.get(operation).setForegroundColor(ColorConstants.blue);
	}

	public void mark(Collection<VOperation> operations) {
		operations.forEach((o) -> mark(o));
	}

	public void removeMarks() {
		operationLabels.values().forEach((l) -> l.setForegroundColor(ColorConstants.black));
	}

	private CompartmentFigure createHeader(final VType type, List<String> stereotypes) {
		if(!stereotypes.isEmpty()) {
			Font font = styler.getStereotypeFont(type);
			Label label = new Label("<<" + String.join(", ", stereotypes) + ">>");
			label.setFont(font);
			add(label);
		}

		nameLabel = new Label(type.getName());
		nameLabel.setFont(styler.getNameFont(type));
		Label toolTipLabel = createToolTipLabel(type.getQualifiedName());
		nameLabel.setBorder(new MarginBorder(3,10,3,10));
		nameLabel.setToolTip(toolTipLabel);

		add(nameLabel);
		return null;
	}



	private String pretty(Map<String, String> properties) {
		return "[" + String.join(",", properties.keySet()) + "]";
	}

	private void addOperationLabel(final VOperation operation) {
		String text = operation.getName() + "()";
		Label label = operation.isStatic() ? new LabelFigure(text) : new Label(text);
		label.setFont(styler.getOperationFont(operation));
		label.setBorder(new MarginBorder(1));

		operationLabels.put(operation, label);

		Label toolTipLabel = createToolTipLabel(operation.getName() + "(...)");

		label.setToolTip(toolTipLabel);
		methodsCompartment.add(label);
	}

	private Label createToolTipLabel(String text) {
		Label toolTipLabel =  new Label(text);
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

	private static class CompartmentFigure extends Figure {

		public CompartmentFigure() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
			layout.setStretchMinorAxis(false);
			layout.setSpacing(2);
			setLayoutManager(layout);
			setBorder(new CompartmentFigureBorder());
		}


		private static class CompartmentFigureBorder extends AbstractBorder {
			public Insets getInsets(IFigure figure) {
				return new Insets(1, 0, 0, 0);
			}

			public void paint(IFigure figure, Graphics graphics, Insets insets) {
				graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), tempRect.getTopRight());
			}
		}

	}

	private static class LabelFigure extends Label {
		private boolean underline = false;
		private boolean strikeThrough = false;

		public LabelFigure(String text) {
			super(text);
			underline = true;
		}


		@Override
		protected void paintFigure(final Graphics graphics) {
			super.paintFigure(graphics);

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
	}
}