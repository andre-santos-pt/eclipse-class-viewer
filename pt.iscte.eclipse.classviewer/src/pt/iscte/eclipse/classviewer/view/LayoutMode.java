package pt.iscte.eclipse.classviewer.view;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;

enum LayoutMode {
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
		//		DirectedGraph() {
		//			@Override
		//			LayoutAlgorithm layoutAlgorithm() {
		//				return new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		//			}
		//		},
		HorizontalLayout("Horizontal") {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new HorizontalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		VerticalLayout("Vertical") {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new VerticalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		},
		TreeLayout("Tree") {
			@Override
			LayoutAlgorithm layoutAlgorithm() {
				return new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
			}
		};

		private LayoutMode() {
			this.name = null;
		}

		private LayoutMode(String name) {
			this.name = name;
		}

		abstract LayoutAlgorithm layoutAlgorithm();

		private final String name;
//		private MenuItem item;

		String getText() {
			return name == null ? name() : name;
		}
		
		MenuItem createMenuItem(Menu parent, GraphViewer viewer) {
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
						viewer.setLayoutAlgorithm(layoutAlgorithm(), true);
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