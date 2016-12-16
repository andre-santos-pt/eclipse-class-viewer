package pt.iscte.eclipse.classviewer.view;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.eclipse.classviewer.model.JAssociation;
import pt.iscte.eclipse.classviewer.model.CallDependency;
import pt.iscte.eclipse.classviewer.model.ClassExtension;
import pt.iscte.eclipse.classviewer.model.IDependency;
import pt.iscte.eclipse.classviewer.model.InterfaceImplementation;
import pt.iscte.eclipse.classviewer.model.JField;
import pt.iscte.eclipse.classviewer.model.JType;
import pt.iscte.eclipse.classviewer.model.TypeDependency;

class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {
		
		private GraphViewer viewer;
		
		FigureProvider(GraphViewer viewer) {
			this.viewer = viewer;
		}
		
		@Override
		public IFigure getFigure(Object element) {
			return new ClassFigure((JType) element);
		}

		@Override
		public String getText(Object element) {
			if(element instanceof JAssociation)
				return ((JAssociation) element).getName();
			return null;
		}



		@Override
		public IFigure getTooltip(Object entity) {
			return new Label(entity.toString());
		}

		@Override
		public int getConnectionStyle(Object rel) {
			IDependency d = (IDependency) rel;
			if(d instanceof CallDependency || d instanceof TypeDependency || d instanceof InterfaceImplementation)
				return ZestStyles.CONNECTIONS_DOT;
			else
				return ZestStyles.CONNECTIONS_SOLID;
		}

		@Override
		public Color getColor(Object rel) {
			return ColorConstants.black;
		}

		@Override
		public Color getHighlightColor(Object rel) {
			return ColorConstants.blue;
		}

		@Override
		public int getLineWidth(Object rel) {
			return 2;
		}

		@Override
		public void selfStyleConnection(Object element, GraphConnection connection) {
			IDependency d = (IDependency) element;
			PolylineConnection connectionFig = (PolylineConnection) connection.getConnectionFigure();
			
//			GraphNode sourceNode = (GraphNode) viewer.findGraphItem(d.getSourceType());
//			ClassFigure sourceFig = (ClassFigure) sourceNode.getNodeFigure();
//			connectionFig.setSourceAnchor(sourceFig.createOutgoingAnchor(d));
//			
//			GraphNode targetNode = (GraphNode) viewer.findGraphItem(d.getTargetType());
//			ClassFigure targetFig = (ClassFigure) targetNode.getNodeFigure();
//			connectionFig.setTargetAnchor(targetFig.createIncomingAnchor(d));

			
			if(d instanceof InterfaceImplementation || d instanceof ClassExtension) {
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setScale(20, 10);
				decoration.setLineWidth(2);
				decoration.setOpaque(true);
				decoration.setBackgroundColor(ColorConstants.white);
				connectionFig.setTargetDecoration(decoration);
			}
			else if(d instanceof CallDependency || d instanceof TypeDependency) {
				PolylineDecoration decoration = new PolylineDecoration();
				decoration.setScale(10, 7);
				decoration.setLineWidth(2);
				connectionFig.setTargetDecoration(decoration);
			}
			else if(d instanceof JField) {
				JField f = (JField) element;
				if(f.getCardinality().isUnary()) {
					PolylineDecoration decoration = new PolylineDecoration();
					decoration.setScale(10, 5);
					decoration.setLineWidth(2);
//					decoration.add(new Label(dep.toString()));
					connectionFig.setTargetDecoration(decoration);
				}
				else {
					PolygonDecoration decoration = new PolygonDecoration();
					PointList points = new PointList();
					points.addPoint(0,0);
					points.addPoint(-2,2);
					points.addPoint(-4,0);
					points.addPoint(-2,-2);
					decoration.setTemplate(points);
					decoration.setScale(7, 3);
					decoration.setLineWidth(2);
					decoration.setOpaque(true);
					decoration.setBackgroundColor(ColorConstants.white);
					connectionFig.setSourceDecoration(decoration);
				}
			}
			
		}


		@Override
		public void selfStyleNode(Object element, GraphNode node) {
			node.getNodeFigure().setSize(-1, -1);
		}
	}
