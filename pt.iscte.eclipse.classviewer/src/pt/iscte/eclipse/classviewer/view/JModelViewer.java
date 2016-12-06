package pt.iscte.eclipse.classviewer.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;

import pt.iscte.eclipse.classviewer.DiagramListener;
import pt.iscte.eclipse.classviewer.DiagramListener.Event;
import pt.iscte.eclipse.classviewer.model.Dependency;
import pt.iscte.eclipse.classviewer.model.JClass;
import pt.iscte.eclipse.classviewer.model.JModel;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JType;

public class JModelViewer extends ViewPart implements IZoomableWorkbenchPart {

	private static JModelViewer instance;

	public static JModelViewer getInstance() {
		return instance;
	}

	private Composite composite;
	//	private Graph graph;

	//	private LayoutMode layoutMode;

	//	private Map<JType, UMLNode> nodesMapper;


	private HashSet<DiagramListener> listeners = new HashSet<DiagramListener>();

	private GraphViewer viewer;

	public void addClickHandler(DiagramListener handler) {
		listeners.add(handler);
	}

	public void removeClickHandler(DiagramListener handler) {
		listeners.remove(handler);
	}

	public JModelViewer() {
		instance = this;
		//		nodesMapper = new HashMap<JType, UMLNode>();
		//		layoutMode = LayoutMode.Spring;
	}

	class NodeProvider implements IGraphEntityRelationshipContentProvider {

		@Override
		public Object[] getElements(Object input) {
			return ((JModel) input).getTypes().toArray();
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getRelationships(Object source, Object target) {
			JType sourceType = (JType) source;
			JType targetType = (JType) target;
			
			List<Dependency> dependencies = sourceType.getDependencies(targetType);
			mergeCallDependencies(dependencies);
			return dependencies.toArray();
		}

		private void mergeCallDependencies(List<Dependency> list) {
			
			Map<JType, JType> map = new HashMap<>();
			Set<Dependency> dups = new HashSet<>();
			for(Dependency d : list) {
				if(map.get(d.getSourceType()).equals(d.getTargetType()))
					dups.add(d);
				else
					map.put(d.getSourceType(), d.getTargetType());
			}
			
			for(Dependency d : dups) {
				list.remove(d);
			}
		}
	}

	class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider {
		@Override
		public IFigure getFigure(Object element) {
			return new UMLClassFigure((JType) element);
		}

		@Override
		public String getText(Object element) {
			if(element instanceof Dependency && !((Dependency) element).getKind().equals(Dependency.Kind.INHERITANCE))
				return ((Dependency) element).getKind().name();
			return null;
		}



		@Override
		public IFigure getTooltip(Object entity) {
			return new Label(entity.toString());
		}

		@Override
		public int getConnectionStyle(Object rel) {
			switch(((Dependency) rel).getKind()) {
			case METHOD: case INTERFACE: return ZestStyles.CONNECTIONS_DASH;
			default: return ZestStyles.CONNECTIONS_SOLID;
			}
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
			return 1;
		}
	}


	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		viewer = new GraphViewer(composite, SWT.NONE);
		viewer.setContentProvider(new NodeProvider());
		viewer.setLabelProvider(new FigureProvider());
		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(ZestStyles.NODES_NO_LAYOUT_RESIZE));
		viewer.applyLayout();
		createPopupMenu(viewer.getGraphControl());
		createMouseListener(viewer.getGraphControl());
		fillToolBar();
	}

	private void createMouseListener(Graph graph) {
		graph.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				handleEvent(e, Event.SELECT);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				handleEvent(e, Event.DOUBLE_CLICK);
			}

			private void handleEvent(MouseEvent e, Event event) {
				IFigure fig = viewer.getGraphControl().getFigureAt(e.x, e.y);
				if(fig instanceof UMLClassFigure) {
					UMLClassFigure classFig = (UMLClassFigure) fig;
					IFigure underFig = classFig.findFigureAt(e.x, e.y);
					if(underFig instanceof Label) {
						JOperation op = classFig.getOperation((Label) underFig);
						if(op != null) {
							for(DiagramListener l : listeners)
								l.operationEvent(op, event);
						}
						else {
							JType type = classFig.getJType();
							for(DiagramListener l : listeners)
								l.classEvent(type, event);
						}
					}
				}
			}
		});
	}

	private void fillToolBar() {
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(
				this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);

	}


	private void createPopupMenu(Graph graph) {
		Menu menu = new Menu(graph);
		MenuItem layoutItem = new MenuItem(menu, SWT.CASCADE);
		layoutItem.setText("Layout");
		Menu layoutMenu = new Menu(layoutItem);
		for(LayoutMode mode : LayoutMode.values())
			mode.createMenuItem(layoutMenu, graph, this);
		layoutItem.setMenu(layoutMenu);

		graph.setMenu(menu);
	}

	public void setFocus() {
		viewer.getGraphControl().setFocus();
	}

	public void displayModel(JModel model, boolean incremental) {
		for(JType t : model) {

			if(t instanceof JClass)
				System.out.println(t + " -> " + ((JClass) t).getSuperclass());
			else
				System.out.println(t);
		}

		viewer.setInput(model);
		Object[] connectionElements = viewer.getConnectionElements();
		for(Object o : connectionElements) {
			if(((Dependency) o).getKind().equals(Dependency.Kind.INHERITANCE)) {
				GraphConnection item = (GraphConnection) viewer.findGraphItem(o);
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setScale(20, 10);
				decoration.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				((PolylineConnection) item.getConnectionFigure()).setTargetDecoration(decoration);
			}
		}
		//		if(!incremental)
		//			clear();
		//
		//		if(model.hasDescription())
		//			graph.setToolTipText(model.getDescription());
		//
		//		graph.getDisplay().syncExec(new ShowFragmentAgent(model)); //Agent will call showFragmentAction
	}

	//	private void showFragmentAction(Iterable<JType> types) {
	//		Set<JType> newTypes = new HashSet<JType>(); //A set with all added classes
	//
	//		for(JType type : types) {
	//			UMLNode node = nodesMapper.get(type);
	//
	//			if(node == null) {
	//				node = createNode(type);
	//				newTypes.add(type);
	//			}
	//			((UMLClassFigure)node.getNodeFigure()).addOperations(type);
	//		}
	//
	//		setConnections(nodesMapper.keySet(), newTypes);
	//
	//		layoutMode.layout(graph);
	//	}




	//	private UMLNode createNode(JType type) {
	//		UMLClassFigure figure = new UMLClassFigure(type);
	//		UMLNode node = new UMLNode(graph, SWT.NONE, figure);
	//		nodesMapper.put(type, node);
	//		return node;
	//	}
	//
	//
	//	private void setConnections(Iterable<JType> previousTypes, Collection<JType> newTypes) {
	//		for(JType prev : previousTypes) { //Sets connections between the previous classes and the added classes
	//			if(prev.isClass()) {
	//				setInheritanceConnectionsClass((JClass) prev, newTypes);
	//				setAssociations((JClass) prev, newTypes);
	//			}
	//			else
	//				setInheritanceConnectionsInterface((JInterface) prev, newTypes);
	//			
	//			for(JType dep : prev.getDependencies())
	//				createDependencyConnection(prev, dep);
	//		}
	//
	//		for(JType neww : newTypes) { //Sets connections between the added classes and all the classes
	//			if(neww.isClass()) {
	//				setInheritanceConnectionsClass((JClass) neww, nodesMapper.keySet());
	//				setAssociations((JClass) neww, nodesMapper.keySet());
	//			}
	//			else
	//				setInheritanceConnectionsInterface((JInterface) neww, nodesMapper.keySet());
	//		}
	//	}

	//	private void setInheritanceConnectionsClass(JClass jc, Collection<JType> set) {
	//		JClass superClass = jc.getSuperclass(); //extends
	//		if(superClass != null && set.contains(superClass))
	//			createExtendsConnection(jc, superClass);
	//
	//		for(JInterface ji : jc.getInterfaces()) {
	//			if(set.contains(ji))
	//				createImplementsConnection(jc, ji);
	//		}
	//	}
	//
	//	private void setInheritanceConnectionsInterface(JInterface ji, Collection<JType> set) {
	//		for(JInterface i : ji.getSuperInterfaces()) {
	//			if(set.contains(i))
	//				createExtendsConnection(ji, i);
	//		}
	//	}
	//
	//	private void setAssociations(JClass jc, Collection<JType> set) {
	//		for(Association a : jc.getAssociations()) {
	//			if(a.isUnary())
	//				createSimpleConnection(jc, a.getTarget(), a.getName());
	//			else
	//				createMultipleConnection(jc, a.getTarget(), a.getName());
	//		}
	//	}

	//	private void createExtendsConnection(JType source, JType target) {
	//		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
	//		connection.setLineColor(ColorConstants.black);
	//
	//		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;
	//
	//		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();
	//
	//		PointList decorationPointList = new PointList();
	//		decorationPointList.addPoint(-2,2);
	//		decorationPointList.addPoint(0,0);
	//		decorationPointList.addPoint(-2,-2);
	//
	//		PolygonDecoration decoration = new PolygonDecoration();
	//		decoration.setBackgroundColor(ColorConstants.white);
	//		decoration.setTemplate(decorationPointList);
	//
	//		connectionFigure.setTargetDecoration(decoration);
	//	}
	//
	//	
	//	private void createDependencyConnection(JType source, JType target) {
	//		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
	//		connection.setLineColor(ColorConstants.red);
	//
	//		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;
	//
	//		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();
	//
	//		PointList decorationPointList = new PointList();
	//		decorationPointList.addPoint(-2,2);
	//		decorationPointList.addPoint(0,0);
	//		decorationPointList.addPoint(-2,-2);
	//
	//		PolylineDecoration decoration = new PolylineDecoration();
	////		decoration.setBackgroundColor(ColorConstants.white);
	//		decoration.setTemplate(decorationPointList);
	//
	//		connectionFigure.setTargetDecoration(decoration);
	//		connectionFigure.setLineStyle(Graphics.LINE_DASH);
	//	}
	//	
	//	private void createImplementsConnection(JType source, JType target) {
	//		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
	//		connection.setLineColor(ColorConstants.black);
	//		connection.setHighlightColor(ColorConstants.darkGreen);
	//
	//		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;
	//
	//		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();
	//
	//		PointList decorationPointList = new PointList();
	//		decorationPointList.addPoint(-2,2);
	//		decorationPointList.addPoint(0,0);
	//		decorationPointList.addPoint(-2,-2);
	//
	//		PolygonDecoration decoration = new PolygonDecoration();
	//		decoration.setBackgroundColor(ColorConstants.white);
	//		decoration.setTemplate(decorationPointList);
	//
	//		connectionFigure.setTargetDecoration(decoration);
	//		connectionFigure.setLineStyle(Graphics.LINE_DASHDOTDOT);
	//	}
	//
	//	
	//
	//	private void createSimpleConnection(JClass source, JType target, String name) {
	//		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
	//		connection.setLineColor(ColorConstants.black);
	//		connection.setHighlightColor(ColorConstants.darkGreen);
	//
	//		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;
	//
	//		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();
	//
	//		PointList decorationPointList = new PointList();
	//		decorationPointList.addPoint(-2,2);
	//		decorationPointList.addPoint(0,0);
	//		decorationPointList.addPoint(-2,-2);
	//
	//		PolylineDecoration decoration = new PolylineDecoration();
	//		decoration.setTemplate(decorationPointList);
	//
	//		connectionFigure.setTargetDecoration(decoration);
	//
	//		ConnectionEndpointLocator targetEndpointLocator = new ConnectionEndpointLocator(connectionFigure, true);
	//		targetEndpointLocator.setVDistance(15);
	//		Label targetMultiplicityLabel = new Label(name);
	//
	//		connectionFigure.add(targetMultiplicityLabel, targetEndpointLocator);
	//	}
	//
	//	private void createMultipleConnection(JClass source, JType target, String name) {
	//		GraphConnection connection = new GraphConnection(graph, ZestStyles.NONE, nodesMapper.get(source), nodesMapper.get(target));
	//		connection.setLineColor(ColorConstants.black);
	//		connection.setHighlightColor(ColorConstants.red);
	//
	//		if(!PolylineConnection.class.isAssignableFrom(connection.getConnectionFigure().getClass())) return;
	//
	//		PolylineConnection connectionFigure = (PolylineConnection)connection.getConnectionFigure();
	//
	//		PointList decorationPointList = new PointList();
	//		decorationPointList.addPoint(0,0);
	//		decorationPointList.addPoint(-2,2);
	//		decorationPointList.addPoint(-4,0);
	//		decorationPointList.addPoint(-2,-2);
	//
	//		PolygonDecoration decoration = new PolygonDecoration();
	//		decoration.setBackgroundColor(ColorConstants.white);
	//		decoration.setTemplate(decorationPointList);
	//
	//		connectionFigure.setSourceDecoration(decoration);
	//
	//		ConnectionEndpointLocator targetEndpointLocator = new ConnectionEndpointLocator(connectionFigure, true);
	//		targetEndpointLocator.setVDistance(15);
	//		Label targetMultiplicityLabel = new Label(name);
	//
	//		connectionFigure.add(targetMultiplicityLabel, targetEndpointLocator);
	//	}
	//
	//	public void clear() {
	//		graph.getDisplay().syncExec(new ClearAgent()); //Agent will call clearAction
	//	}
	//
	//	private void clearAction() {
	//		Object[] connections = graph.getConnections().toArray();
	//		for(int i = 0; i < connections.length; i++) { //Clear connections
	//			GraphConnection conn = (GraphConnection)connections[i];
	//			conn.dispose();
	//		}
	//
	//		Object[] nodes = graph.getNodes().toArray();
	//		for(int i = 0; i < nodes.length; i++) { //Clear nodes
	//			GraphNode node = (GraphNode)nodes[i];
	//			node.dispose();
	//		}
	//
	//		nodesMapper.clear(); //Clear mapper
	//	}

	//	private class ShowFragmentAgent implements Runnable {
	//
	//		private Iterable<JType> types;
	//
	//		private ShowFragmentAgent(Iterable<JType> types) {
	//			this.types = types;
	//		}
	//
	//		public void run() {
	//			showFragmentAction(types);
	//		}
	//
	//	}

	//	private class ClearAgent implements Runnable {
	//
	//		public void run() {
	//			clearAction();
	//		}
	//
	//	}

	//	private class UMLNode extends GraphNode {
	//
	//		private UMLClassFigure figure;
	//
	//		public UMLNode(IContainer graphModel, int style, UMLClassFigure figure) {
	//			super(graphModel, style, figure);
	//			this.figure = figure;
	//		}
	//
	//		protected IFigure createFigureForModel() {
	//			return (IFigure) this.getData();
	//		}
	//		@Override
	//		public void highlight() {
	//			figure.select();
	//		}
	//
	//
	//		@Override
	//		public void unhighlight() {
	//			figure.unselect();
	//		}
	//	}



	private enum LayoutMode {
		Spring {
			@Override
			LayoutAlgorithm layoutAlgorithm() {

				return new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		Radial {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		DirectedGraph {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		HorizontalLayout {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new HorizontalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		VerticalLayout {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new VerticalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		TreeLayout {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		};

		abstract LayoutAlgorithm layoutAlgorithm();

		private MenuItem item;

		void createMenuItem(Menu parent, final Graph graph, final JModelViewer viewer) {
			item = new MenuItem(parent, SWT.CHECK);
			item.setText(name());
			if(ordinal() == 0)
				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					//					viewer.layoutMode = LayoutMode.this;
					graph.setLayoutAlgorithm(layoutAlgorithm(), true);
					for(LayoutMode mode : values())
						if(mode.item != item) {
							mode.item.setSelection(false);
						}
				}
			});
		}

		void layout(Graph graph) {
			graph.setLayoutAlgorithm(layoutAlgorithm(), true);
		}
	}



	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}



}