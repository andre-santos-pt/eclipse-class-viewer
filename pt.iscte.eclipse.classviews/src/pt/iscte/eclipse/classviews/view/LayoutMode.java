package pt.iscte.eclipse.classviews.view;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

import pt.iscte.eclipse.classviews.model.VModel;
import pt.iscte.eclipse.classviews.model.VType;

enum LayoutMode {
		Spring {
			@Override
			LayoutAlgorithm layoutAlgorithm(VModel model) {
				return new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		Radial {
			@Override
			LayoutAlgorithm layoutAlgorithm(VModel model) {
				return new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		
		//		DirectedGraph() {
		//			@Override
		//			LayoutAlgorithm layoutAlgorithm() {
		//				return new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		//			}
		//		},
//		HorizontalLayout("Horizontal") {
//			@Override
//			LayoutAlgorithm layoutAlgorithm() {
//				return new HorizontalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
//			}
//		},
//		VerticalLayout("Vertical") {
//			@Override
//			LayoutAlgorithm layoutAlgorithm() {
//				return new VerticalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
//			}
//		},
		TreeLayout("Tree") {
			@Override
			LayoutAlgorithm layoutAlgorithm(VModel model) {
//				TreeLayoutAlgorithm alg = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				HorizontalTreeLayoutAlgorithm alg = new HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				alg.setComparator(new Comparator<LayoutEntity>() {

					@Override
					public int compare(LayoutEntity a1, LayoutEntity b1) {
						System.out.println(a1.getGraphData().getClass());
						VType a = (VType) ((GraphNode) a1.getGraphData()).getData();
						VType b = (VType)  ((GraphNode) b1.getGraphData()).getData();
						
						return model.getDepth(a) - model.getDepth(b);
//						if(a instanceof JClass && b instanceof JClass) {
//							JClass aa = (JClass) a;
//							JClass bb = (JClass) b;
//							if(aa.compatibleWith(bb))
//								return 1;
//							else if(bb.compatibleWith(aa))
//								return -1;
//							else
//								return 0;
//						}
//						else
//							return 0;
					} 
					
				});
				return alg;
			}
		};

		private LayoutMode() {
			this.name = null;
		}

		private LayoutMode(String name) {
			this.name = name;
		}

		abstract LayoutAlgorithm layoutAlgorithm(VModel model);

		private final String name;
//		private MenuItem item;

		String getText() {
			return name == null ? name() : name;
		}
		
		MenuItem createMenuItem(Menu parent, GraphViewer viewer, VModel model) {
			MenuItem item = new MenuItem(parent, SWT.PUSH);
			item.setText(getText());
//			if(ordinal() == 0)
//				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
//					if(item == e.item && item.getSelection()) {
//						viewer.applyLayout();
//						item.setSelection(true);
//					}
//					else {
						viewer.setLayoutAlgorithm(layoutAlgorithm(model), true);
//						for(LayoutMode mode : values())
//							if(!mode.getText().equals(e.item.getText()))
//						for(MenuItem i : parent.getItems())
//							i.setSelection(i == e.item);
//					}
				}
			});
			return item;
			
		}
	}