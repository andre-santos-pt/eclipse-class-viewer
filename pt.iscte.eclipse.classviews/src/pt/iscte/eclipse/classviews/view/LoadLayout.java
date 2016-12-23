package pt.iscte.eclipse.classviews.view;

import org.eclipse.zest.layouts.algorithms.AbstractLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;

public class LoadLayout extends AbstractLayoutAlgorithm {

	public LoadLayout(int styles) {
		super(styles);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setLayoutArea(double x, double y, double width, double height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isValidConfiguration(boolean asynchronous, boolean continuous) {
		return true;
	}

	@Override
	protected void applyLayoutInternal(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider,
			double boundsX, double boundsY, double boundsWidth, double boundsHeight) {

//		for(InternalNode n : entitiesToLayout)
//			n.getLayoutEntity().get
//			n.setSize(100, 100);
//			n.setLocation(0, 0);
			
	}

	@Override
	protected void preLayoutAlgorithm(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider,
			double x, double y, double width, double height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void postLayoutAlgorithm(InternalNode[] entitiesToLayout,
			InternalRelationship[] relationshipsToConsider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getTotalNumberOfLayoutSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getCurrentLayoutStep() {
		// TODO Auto-generated method stub
		return 0;
	}

}
