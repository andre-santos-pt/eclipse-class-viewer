package pt.iscte.eclipse.classviewer.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.eclipse.classviewer.DiagramListener;
import pt.iscte.eclipse.classviewer.DiagramListener.Event;
import pt.iscte.eclipse.classviewer.model.CallDependency;
import pt.iscte.eclipse.classviewer.model.IDependency;
import pt.iscte.eclipse.classviewer.model.JField;
import pt.iscte.eclipse.classviewer.model.JModel;
import pt.iscte.eclipse.classviewer.model.JOperation;
import pt.iscte.eclipse.classviewer.model.JType;
import pt.iscte.eclipse.classviewer.model.TypeDependency;
import pt.iscte.eclipse.classviewer.view.Filters.Filter;

public class JModelViewer extends ViewPart implements IZoomableWorkbenchPart {

	private static JModelViewer instance;

	public static JModelViewer getInstance() {
		return instance;
	}

	private List<DiagramListener> listeners;
	private GraphViewer viewer;

	private JModel model;
	
	private final SelectionListener listener = new SelectionListener();

	
	public JModelViewer() {
		instance = this;
	}



	
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		createFiltersBar(parent);
		viewer = new GraphViewer(parent, SWT.NONE);
		viewer.getGraphControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new NodeProvider());
		viewer.setLabelProvider(new FigureProvider(viewer));
		viewer.setLayoutAlgorithm(LayoutMode.Spring.layoutAlgorithm(model));
		viewer.setNodeStyle(ZestStyles.NODES_NO_LAYOUT_RESIZE);
		viewer.addSelectionChangedListener(listener);
		createMouseListener(viewer.getGraphControl());
		fillToolBar();
	}

	private void createFiltersBar(Composite parent) {
		Composite filtersBar = new Composite(parent, SWT.BORDER);
		filtersBar.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		RowLayout layout = new RowLayout();
		layout.spacing = 10;
		filtersBar.setLayout(layout);
		new org.eclipse.swt.widgets.Label(filtersBar, SWT.NONE).setText("Filters: ");
		
		for(Filter f : Filters.FILTERS) {
			Button b = new Button(filtersBar, SWT.CHECK);
			b.setText(f.getName());
			b.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(b.getSelection())
						filters.add(f);
					else
						filters.remove(f);
					viewer.setFilters(filters.toArray(new ViewerFilter[filters.size()]));
					applyFilterToFieldsAndOperations(); // TODO Fields
					viewer.applyLayout(); // TODO location memory
				}
			});
		}
	}
	
	private void applyFilterToFieldsAndOperations() {
		Object[] nodeElements = viewer.getNodeElements();
		for(Object o : nodeElements) {
			GraphNode node = (GraphNode) viewer.findGraphItem(o);
			JType type = (JType) node.getData();
			ClassFigure fig = (ClassFigure) node.getNodeFigure();
			for (JOperation operation : type.getOperations()) {
				boolean filter = false;
				for(Filter f : filters) {
					if(!f.select(operation)) {
						filter = true;
						break;
					}
				}
				if(filter)
					fig.hide(operation);
			}
		}
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
				if(sel.getFirstElement() instanceof TypeDependency || sel.getFirstElement() instanceof JField) {
					IDependency d = (IDependency) sel.getFirstElement();
					Collection<CallDependency> deps = d.getSourceType().getCallDependencies(d.getTargetType());
					for(CallDependency cd : deps)
						mark(cd.getSourceOperation());
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
					Point p = new Point(e.x, e.y);
					classFig.translateToRelative(p);
					IFigure underFig = classFig.findFigureAt(p.x, p.y);
					if(underFig instanceof Label) {
						JOperation op = classFig.getOperation((Label) underFig);
						if(op != null) {
							for(DiagramListener l : listeners)
								l.operationEvent(op, event);
							System.out.println(op);
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

	private Set<Filter> filters = new HashSet<>();
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
			mode.createMenuItem(layoutMenu, viewer, model);
		layoutItem.setMenu(layoutMenu);
	}

	private void createFiltersMenu(Menu menu) {
		MenuItem filtersItem = new MenuItem(menu, SWT.CASCADE);
		filtersItem.setText("Filters");
		Menu filtersMenu = new Menu(filtersItem);

		for(Filter f : Filters.FILTERS) {
			MenuItem deps = new MenuItem(filtersMenu, SWT.CHECK);
			deps.setText(f.getName());
			deps.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(deps.getSelection())
						filters.add(f);
					else
						filters.remove(f);
					
					viewer.setFilters(filters.toArray(new ViewerFilter[filters.size()]));
					Object[] nodeElements = viewer.getNodeElements();
					for(Object o : nodeElements) {
						GraphNode node = (GraphNode) viewer.findGraphItem(o);
						JType type = (JType) node.getData();
						ClassFigure fig = (ClassFigure) node.getNodeFigure();
						for (JOperation operation : type.getOperations()) {
							boolean filter = false;
							for(Filter f : Filters.FILTERS) {
								if(!f.select(operation)) {
									filter = true;
									break;
								}
							}
							if(filter)
								fig.hide(operation);
						}
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
		this.model = model;
//		filters.clear();
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