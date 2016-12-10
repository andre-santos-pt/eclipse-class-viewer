package pt.iscte.eclipse.classviewer.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

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
			((JClass) type).getFields().forEach((f) -> {
				if(f.hasProperty("VALUE_TYPE"))
					fieldsCompartment.add(new Label("- " + f.getName() + " : " + f.getType().getName()));
			});
		

		
				
//		add(headerCompartment);
		methodsCompartment = new CompartmentFigure();
		add(methodsCompartment);
		
		type.getOperations().forEach((o) -> addMethodLabel(o));
		setSize(-1, -1);
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
	
	public void setFilter(ViewerFilter filter) {
		for(Entry<JOperation, Label> e : operationLabels.entrySet()) {
			if(!filter.select(null, null, e.getKey())) {
//				e.getValue().setSize(e.getValue().getBounds().width, 0);
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
			Label label = createLabel("<<interface>>", INTERFACE_DESC_FONT);
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
			Label label = createLabel("<<" + s.getName() + ">>", INTERFACE_DESC_FONT);
//			header.add(label);
			add(label);
		}
			

		nameLabel = createLabel(type.getName(), font);
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

		Font font = null;

		if(operation.isAbstract()) { //Abstract
			font = ABSTRACT_METHOD_FONT;
		}
		else { //Method
			font = METHOD_FONT;
		}

		Label methodLabel = createLabel(text, font);
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

	
	private Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}

	private Label createToolTipLabel(String text) {
		Label toolTipLabel = createLabel(text, TOOLTIP_FONT);
		toolTipLabel.setBackgroundColor(TOOLTIP_COLOR);
		toolTipLabel.setOpaque(true);
		return toolTipLabel;
	}
	
}