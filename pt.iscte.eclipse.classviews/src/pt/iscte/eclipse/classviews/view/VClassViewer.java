package pt.iscte.eclipse.classviews.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.progress.ProgressListener;

import pt.iscte.eclipse.classviews.DiagramListener;
import pt.iscte.eclipse.classviews.IModelFilter;
import pt.iscte.eclipse.classviews.DiagramListener.Event;
import pt.iscte.eclipse.classviews.IResourceHandler;
import pt.iscte.eclipse.classviews.model.CallDependency;
import pt.iscte.eclipse.classviews.model.IDependency;
import pt.iscte.eclipse.classviews.model.VField;
import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VOperation;
import pt.iscte.eclipse.classviews.model.VPackage;
import pt.iscte.eclipse.classviews.model.VType;
import pt.iscte.eclipse.classviews.model.TypeDependency;
import pt.iscte.eclipse.classviews.model.VClass;
import pt.iscte.eclipse.classviews.view.Filters.Filter;

public class VClassViewer extends EditorPart implements IZoomableWorkbenchPart {
	private GraphViewer viewer;
	private Composite filtersBar;

	private List<DiagramListener> listeners;
	private SelectionListener selectionListener;

	private Set<Filter> filters = new HashSet<>();
//	private Map<VPackage, Filter> packageFiltersMap;
	private CheckboxTableViewer packageFilters;

	private Set<IResource> resources;

	private VModel model;

	private boolean dirty;

	public VClassViewer() {
		selectionListener = new SelectionListener();
		listeners = Collections.emptyList();
//		packageFiltersMap = new WeakHashMap<>();
		resources = new HashSet<>();
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2,false));
		createFiltersBar(parent);
		viewer = new GraphViewer(parent, SWT.NONE);
		viewer.getGraphControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new NodeProvider());		
		viewer.setLabelProvider(new FigureProvider(viewer));
//		viewer.setLayoutAlgorithm(LayoutMode.Spring.layoutAlgorithm(model));
		viewer.setNodeStyle(ZestStyles.NODES_NO_LAYOUT_RESIZE);
		viewer.addSelectionChangedListener(selectionListener);
		viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { ResourceTransfer.getInstance() }, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				loadModel((Object[]) event.data);
			}
		});

		createMouseListener(viewer.getGraphControl());
		createPopupMenu();
		viewer.setLayoutAlgorithm(new LoadLayout(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		model = new VModel();
		Map<String, Point> points = loadDataFromFile();
		viewer.setInput(model);
		layout(points);
	}

	private void layout(Map<String, Point> points) {
		for(Object o : viewer.getGraphControl().getNodes()) {
			GraphNode node = (GraphNode) o;
			VType type = (VType) node.getData();
			Point p = points.get(type.getQualifiedName());
			if(p != null)
				node.setLocation(p.x, p.y);
		}
	}

	private void loadModel(Object ... array) {
		for(Object o : array) {
			IResource r = (IResource) o;
			for(IResourceHandler h : Activator.getInstance().getDropHandlers())
				if(h.process(r, model))
					resources.add(r);
		}

		resourcesTable.setInput(resources);
		packageFilters.setInput(model.getPackages());
		
		for(VPackage p : model.getPackages())
			packageFilters.setChecked(p, true);
//			Filter filter = new Filter(new Filters.PackageFilter(p), p.getName(), true);
//			packageFiltersMap.put(p, filter);
//			packageFilters.setChecked(p, filters.contains(o));
//		}

		filtersBar.pack();
		//		viewer.applyLayout();
		setDirty(true);
	}
	
	private void setFilters() {
		ViewerFilter[] v = filters.toArray(new ViewerFilter[filters.size() + 1]);
		Object[] selectedPackages = packageFilters.getCheckedElements();
		List<VPackage> list = new ArrayList<>();
		for(Object o : selectedPackages)
			list.add((VPackage) o);
		
		v[v.length-1] = new Filter(new Filters.PackageFilter(list), "Package Filter", true);
		viewer.setFilters(v);
		viewer.applyLayout(); 
	}

	private void createFiltersBar(Composite parent) {
		filtersBar = new Composite(parent, SWT.BORDER);
		filtersBar.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.spacing = 10;
		filtersBar.setLayout(new GridLayout());
		createInputTable(filtersBar);
		createPackagesTable(filtersBar);
		createFiltersTable(filtersBar);
	}
	
	private void createInputTable(Composite parent) {
		new org.eclipse.swt.widgets.Label(parent, SWT.NONE).setText("Input:");
		resourcesTable = new TableViewer(parent);
		resourcesTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		resourcesTable.setContentProvider(new ArrayContentProvider());
		resourcesTable.addDropSupport(DND.DROP_MOVE, new Transfer[] { ResourceTransfer.getInstance() }, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				loadModel((Object[]) event.data);
			}
		});
		Menu menu = new Menu(resourcesTable.getControl());
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Remove");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resources.remove(resourcesTable.getStructuredSelection().getFirstElement());
				resourcesTable.setInput(resources);
				setDirty(true);
			}
		});
		resourcesTable.getControl().setMenu(menu);
	}

	private void createPackagesTable(Composite parent) {
		new org.eclipse.swt.widgets.Label(parent, SWT.NONE).setText("Visible packages:");

		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		packageFilters = CheckboxTableViewer.newCheckList(scroll, SWT.BORDER);
		scroll.setContent(packageFilters.getControl());
		packageFilters.setContentProvider(new ArrayContentProvider());
		packageFilters.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		packageFilters.setComparator(new ViewerComparator(String.CASE_INSENSITIVE_ORDER));
		packageFilters.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
//				VPackage pck = (VPackage) event.getElement();
//				if(event.getChecked())
//					filters.remove(packageFiltersMap.get(pck));
//				else
//					filters.add(packageFiltersMap.get(pck));

//				viewer.setFilters(filters.toArray(new ViewerFilter[filters.size()]));
				
//				viewer.applyLayout();
				setFilters();
				setDirty(true);
			}
		});
		packageFilters.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Collection<VType> types = new ArrayList<VType>();
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				System.out.println(s);
				//				Iterator iterator = s.iterator();
				//				while(iterator.hasNext())
				//					((JPackage) iterator.next()).forEach((t) -> types.add(t));
				//					
				//				viewer.setSelection(new StructuredSelection(types));
			}
		});
	}

	private void createFiltersTable(Composite parent) {
		new org.eclipse.swt.widgets.Label(parent, SWT.NONE).setText("Filters: ");

		for(Filter f : Filters.FILTERS) {
			Button b = new Button(filtersBar, SWT.CHECK);
			b.setText(f.getName());
			b.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(b.getSelection())
						filters.add(f);
					else
						filters.remove(f);
//					viewer.setFilters(filters.toArray(new ViewerFilter[filters.size()]));
					setFilters();
					applyFilterToFieldsAndOperations(); // TODO Fields
				}
			});
		}
	}

	

	private void applyFilterToFieldsAndOperations() {
		Object[] nodeElements = viewer.getNodeElements();
		for(Object o : nodeElements) {
			GraphNode node = (GraphNode) viewer.findGraphItem(o);
			VType type = (VType) node.getData();
			ClassFigure fig = (ClassFigure) node.getNodeFigure();
			
			for (VOperation operation : type.getOperations()) {
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
			
			if(type.isClass()) {
				VClass c = (VClass) type;
				for (VField field : c.getFields()) {
					boolean filter = false;
					for(Filter f : filters) {
						if(!f.select(field)) {
							filter = true;
							break;
						}
					}
					if(filter)
						fig.hide(field);
				}
			}
		}
	}


	private class SelectionListener implements ISelectionChangedListener {
		private Set<VType> selectedTypes = new HashSet<>();
		private Set<ClassFigure> marked = new HashSet<>();

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			excludeItem.setEnabled(false);
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			marked.forEach((c) -> {c.removeMarks(); c.unselect();});
			marked.clear();
			selectedTypes.clear();

			if(!sel.isEmpty()) {
				if(sel.getFirstElement() instanceof TypeDependency || sel.getFirstElement() instanceof VField) {
					IDependency d = (IDependency) sel.getFirstElement();
					Collection<CallDependency> deps = d.getSourceType().getCallDependencies(d.getTargetType());
					for(CallDependency cd : deps)
						mark(cd.getSourceOperation());
				}
				else if(sel.getFirstElement() instanceof VType) {
					excludeItem.setEnabled(true);
					for(Object o : sel.toArray()) {
						if(o instanceof VType) {
							GraphNode node = (GraphNode) viewer.findGraphItem(o);
							ClassFigure fig = (ClassFigure) node.getNodeFigure();
							fig.select();
							marked.add(fig);
							selectedTypes.add((VType) o);
						}
					}
					packageFilters.setSelection(new StructuredSelection(sel.getFirstElement()));
					setDirty(true);
				}
			}
		}



		private void mark(VOperation o) {
			GraphNode node = (GraphNode) viewer.findGraphItem(o.getOwner());
			ClassFigure fig = (ClassFigure) node.getNodeFigure();
			fig.mark(o);
			marked.add(fig);
		}

		Collection<VType> getSelectedNodes() {
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
						VOperation op = classFig.getOperation((Label) underFig);
						if(op != null) {
							for(DiagramListener l : listeners)
								l.operationEvent(op, event);
							System.out.println(op);
						}
						else {
							VType type = classFig.getJType();
							for(DiagramListener l : listeners)
								l.classEvent(type, event);
						}
					}
				}
			}
		});
	}

	


	private MenuItem excludeItem;
	private MenuItem viewAsValueItem;
	private TableViewer resourcesTable;

	private void createPopupMenu() {
		Graph graph = viewer.getGraphControl();
		Menu menu = new Menu(graph);

		excludeItem = new MenuItem(menu, SWT.PUSH);
		excludeItem.setText("Ignore");
		excludeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (VType t : selectionListener.getSelectedNodes()) {
					viewer.removeNode(t);
				}	
			}
		});

		viewAsValueItem = new MenuItem(menu, SWT.PUSH);
		viewAsValueItem.setText("View as Value Type");
		viewAsValueItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (VType t : selectionListener.getSelectedNodes()) {
					t.setTagProperty("VALUE_TYPE");
				}
				//				displayModel((JModel) viewer.getInput(), false, listeners);
			}
		});

		MenuItem saveImage = new MenuItem(menu, SWT.PUSH);
		saveImage.setText("Save as image...");
		saveImage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(new Shell(), SWT.SAVE);
				dialog.setFilterExtensions(new String[] {"*.png"});
				String filePath = dialog.open();
				if(filePath != null) {
					Image image = getDiagramImage();
					ImageLoader imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] { image.getImageData() };
					imageLoader.save(filePath, SWT.IMAGE_PNG);
					image.dispose();
				}
			}
		});


		createLayoutsMenu(menu);
		//		createFiltersMenu(menu);

		ZoomContributionViewItem zoomItem = new ZoomContributionViewItem(this);

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



	public void setFocus() {
		viewer.getGraphControl().setFocus();
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


	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName().substring(0, input.getName().lastIndexOf('.')));
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		IFileEditorInput input = (IFileEditorInput) getEditorInput();
		IFile file = input.getFile();
		StringBuffer buffer = new StringBuffer();

		for(IResource r : resources)
			buffer.append("R " + r.getProject().getName() + "\n");

		for (VPackage p : model.getPackages())
			buffer.append("P " + p.getName() + " " + packageFilters.getChecked(p) + "\n");

		for(Object o : viewer.getGraphControl().getNodes()) {
			GraphNode node = (GraphNode) o;
			Point p = node.getLocation();
			VType type = (VType) node.getData();
			buffer.append("N " + type.getQualifiedName() + " " + p.x + " " + p.y + "\n");
		}

		for(Filter f : filters) {
			buffer.append("F " + f.getName() + "\n");
		}

		InputStream source = new ByteArrayInputStream(buffer.toString().getBytes());
		try {
			file.setContents(source, true, false, monitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		setDirty(false);
		System.out.println(buffer);
	}

	private Map<String, Point> loadDataFromFile() {
		IFileEditorInput input = (IFileEditorInput) getEditorInput();
		IFile file = input.getFile();
		Map<String, Point> points = new HashMap<>();
		try {
			Scanner scanner = new Scanner(file.getContents());
			while(scanner.hasNextLine()) {
				Scanner lineScan = new Scanner(scanner.nextLine());
				if(lineScan.hasNext()) {
					switch(lineScan.next()) {
					case "R":
						IWorkspace workspace = file.getProject().getWorkspace();
						IProject project = workspace.getRoot().getProject(lineScan.next());
						loadModel(project);
						break;
					case "N":
						points.put(lineScan.next(), new Point(lineScan.nextInt(), lineScan.nextInt()));
						break;
					case "P":		
						VPackage p = model.getPackage(lineScan.next());
						if(p != null) {
							boolean selected = Boolean.parseBoolean(lineScan.next());
							packageFilters.setChecked(p, selected);
//							if(!selected)
//								filters.add(packageFiltersMap.get(p));
						}
						break;
					}
				}
				lineScan.close();
			}
			scanner.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return points;
	}

	private void setDirty(boolean dirty) {
		this.dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}



	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {

	}
	
//	private void fillToolBar() {
//		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(
//				this);
//		IActionBars bars = getEditorSite().getActionBars();
//		bars.getMenuManager().add(toolbarZoomContributionViewItem);
//
//	}
}