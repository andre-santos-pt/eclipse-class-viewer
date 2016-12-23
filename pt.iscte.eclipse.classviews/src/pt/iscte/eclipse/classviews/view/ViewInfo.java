package pt.iscte.eclipse.classviews.view;

import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;

import pt.iscte.eclipse.classviews.model.VModel;

class ViewInfo {

	Set<String> pluginIds;
	Set<String> packages;
	Set<String> filterIds;
	
	ViewInfo(VModel model) {
		
	}
	private Map<String, Point> locations;
}
