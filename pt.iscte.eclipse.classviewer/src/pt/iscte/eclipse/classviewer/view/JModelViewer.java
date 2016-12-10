package pt.iscte.eclipse.classviewer.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.eclipse.classviewer.DiagramListener;
import pt.iscte.eclipse.classviewer.DiagramListener.Event;
import pt.iscte.eclipse.classviewer.model.CallDependency;
import pt.iscte.eclipse.classviewer.model.Dependency;
import pt.iscte.eclipse.classviewer.model.FieldDependency;
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
	private List<DiagramListener> listeners;
	private GraphViewer viewer;

	
	public JModelViewer() {
		instance = this;
	}

	class NodeProvider implements IGraphEntityRelationshipContentProvider {

		@Override
		public Object[] getElements(Object input) {
			return ((JModel) input).sortedTypes().stream().filter((t) -> !t.hasProperty("VALUE_TYPE")).toArray();
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
			Iterator<Dependency> iterator = dependencies.iterator();
			while(iterator.hasNext()) {
				Dependency d = iterator.next();
				if(d instanceof FieldDependency && d.getTargetType().isClass()) {
					FieldDependency fd = (FieldDependency) d;
					if(fd.getCardinality().isUnary() && ((JClass) fd.getTargetType()).hasNonUnaryDependencyTo(fd.getSourceType()))
						iterator.remove();
				}
			}
			iterator = dependencies.iterator();
			while(iterator.hasNext()) {
				Dependency d = iterator.next();
				if(d instanceof CallDependency) {
					CallDependency cd = (CallDependency) d;
					boolean remove = false;
					for(Dependency d2 : dependencies)
						if(d2 instanceof FieldDependency && ((FieldDependency) d2).getTargetType().equals(cd.getTargetType()))
							remove = true;
					
					if(remove)
						iterator.remove();
				}
			}
			//			mergeCallDependencies(dependencies);
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

	class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {
		@Override
		public IFigure getFigure(Object element) {
			return new ClassFigure((JType) element);
		}

		@Override
		public String getText(Object element) {
			if(element instanceof FieldDependency)
				return ((FieldDependency) element).getFieldName();
			return null;
		}



		@Override
		public IFigure getTooltip(Object entity) {
			return new Label(entity.toString());
		}

		@Override
		public int getConnectionStyle(Object rel) {
			switch(((Dependency) rel).getKind()) {
			case CALL: case INTERFACE: return ZestStyles.CONNECTIONS_DOT;
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
			return 2;
		}

		@Override
		public void selfStyleConnection(Object element, GraphConnection connection) {
			switch(((Dependency) element).getKind()) {
			case INHERITANCE: case INTERFACE: {
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setScale(20, 10);
				decoration.setLineWidth(2);
				decoration.setOpaque(true);
				decoration.setBackgroundColor(ColorConstants.white);
				((PolylineConnection) connection.getConnectionFigure()).setTargetDecoration(decoration);
				break;
			}
			case CALL: {
				PolylineDecoration decoration = new PolylineDecoration();
				decoration.setScale(10, 5);
				decoration.setLineWidth(2);
				((PolylineConnection) connection.getConnectionFigure()).setTargetDecoration(decoration);
				break;
			}
			case FIELD: {
				
				FieldDependency dep = (FieldDependency) element;
				if(dep.getCardinality().isUnary()) {
					PolylineDecoration decoration = new PolylineDecoration();
					decoration.setScale(10, 5);
					decoration.setLineWidth(2);
//					decoration.add(new Label(dep.toString()));
					((PolylineConnection) connection.getConnectionFigure()).setTargetDecoration(decoration);
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
					((PolylineConnection) connection.getConnectionFigure()).setSourceDecoration(decoration);
				}
				break;
			}
			}
		}


		@Override
		public void selfStyleNode(Object element, GraphNode node) {
			node.getNodeFigure().setSize(-1, -1);
		}
	}

	
	


	private final SelectionListener listener = new SelectionListener();
	
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		viewer = new GraphViewer(composite, SWT.NONE);
		viewer.setContentProvider(new NodeProvider());
		viewer.setLabelProvider(new FigureProvider());
		viewer.setLayoutAlgorithm(LayoutMode.Spring.layoutAlgorithm());
		viewer.setNodeStyle(ZestStyles.NODES_NO_LAYOUT_RESIZE);
		viewer.addSelectionChangedListener(listener);
		createMouseListener(viewer.getGraphControl());
		fillToolBar();
	}

	private class SelectionListener implements ISelectionChangedListener {
		private Set<JType> selectedTypes = new HashSet<>();
		private Set<ClassFigure> marked = new HashSet<>();

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			excludeItem.setEnabled(false);
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			marked.forEach((c) -> {c.removeMarks(); c.unselect();});
			marked.clear();
			selectedTypes.clear();
			
			if(!sel.isEmpty()) {
				if(sel.getFirstElement() instanceof CallDependency) {
					CallDependency d = (CallDependency) sel.getFirstElement();
					mark(d.getSourceOperation());
					mark(d.getTargetOperation());
				}
				else if(sel.getFirstElement() instanceof JType) {
					excludeItem.setEnabled(true);
					for(Object o : sel.toArray()) {
						if(o instanceof JType) {
							GraphNode node = (GraphNode) viewer.findGraphItem(o);
							ClassFigure fig = (ClassFigure) node.getNodeFigure();
							fig.select();
							marked.add(fig);
							selectedTypes.add((JType) o);
						}
					}
				}
			}
		}
		
		

		private void mark(JOperation o) {
			GraphNode node = (GraphNode) viewer.findGraphItem(o.getOwner());
			ClassFigure fig = (ClassFigure) node.getNodeFigure();
			fig.mark(o);
			marked.add(fig);
		}
		
		Collection<JType> getSelectedNodes() {
			return selectedTypes;
		}
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
				if(fig instanceof ClassFigure) {
					ClassFigure classFig = (ClassFigure) fig;
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

	private Set<ViewerFilter> filters = new HashSet<>();
	private MenuItem excludeItem;
	private MenuItem viewAsValueItem;
	
	private void createPopupMenu() {
		Graph graph = viewer.getGraphControl();
		Menu menu = new Menu(graph);

		excludeItem = new MenuItem(menu, SWT.PUSH);
		excludeItem.setText("Ignore");
		excludeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (JType t : listener.getSelectedNodes()) {
					viewer.removeNode(t);
				}	
			}
		});
		
		viewAsValueItem = new MenuItem(menu, SWT.PUSH);
		viewAsValueItem.setText("View as Value Type");
		viewAsValueItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (JType t : listener.getSelectedNodes()) {
					t.setTagProperty("VALUE_TYPE");
				}
				displayModel((JModel) viewer.getInput(), false, listeners);
			}
		});
		
		createLayoutsMenu(menu);
		createFiltersMenu(menu);

		graph.setMenu(menu);
	}

	private void createLayoutsMenu(Menu menu) {
		MenuItem layoutItem = new MenuItem(menu, SWT.CASCADE);
		layoutItem.setText("Layout");
		Menu layoutMenu = new Menu(layoutItem);
		for(LayoutMode mode : LayoutMode.values())
			mode.createMenuItem(layoutMenu, viewer);
		layoutItem.setMenu(layoutMenu);
	}

	private void createFiltersMenu(Menu menu) {
		MenuItem filtersItem = new MenuItem(menu, SWT.CASCADE);
		filtersItem.setText("Filters");
		Menu filtersMenu = new Menu(filtersItem);

		for(ViewerFilter v : Filters.FILTERS) {
			MenuItem deps = new MenuItem(filtersMenu, SWT.CHECK);
			deps.setText(v.toString());
			deps.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(deps.getSelection())
						filters.add(v);
					else
						filters.remove(v);
					viewer.setFilters(filters.toArray(new ViewerFilter[filters.size()]));
					Object[] nodeElements = viewer.getNodeElements();
					for(Object o : nodeElements) {
						GraphNode node = (GraphNode) viewer.findGraphItem(o);
						ClassFigure fig = (ClassFigure) node.getNodeFigure();
						filters.forEach((f) -> fig.setFilter(f));
					}
					viewer.applyLayout(); // TODO location memory
				}
			});
		}
		filtersItem.setMenu(filtersMenu);
	}

	public void setFocus() {
		viewer.getGraphControl().setFocus();
	}

	public void displayModel(JModel model, boolean incremental, List<DiagramListener> listeners) {
		filters.clear();
		createPopupMenu();
		viewer.setInput(model);
		this.listeners = listeners;
		
	}


	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}

	public Image getDiagramImage() {
		GC gc = new GC(viewer.getControl());
		Rectangle bounds = viewer.getControl().getBounds();
		Image image = new Image(viewer.getControl().getDisplay(), bounds);
		try {
			gc.copyArea(image, 0, 0);
		} finally {
			gc.dispose();
		}
		return image;
	}

	public void clear() {
		
	}


}