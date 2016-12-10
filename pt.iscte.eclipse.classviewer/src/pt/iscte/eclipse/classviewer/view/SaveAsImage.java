package pt.iscte.eclipse.classviewer.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class SaveAsImage extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FileDialog dialog = new FileDialog(new Shell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] {"*.png"});
		String filePath = dialog.open();
		if(filePath != null) {
			Image image = JModelViewer.getInstance().getDiagramImage();
			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(filePath, SWT.IMAGE_PNG);
			image.dispose();
		}
		return null;
	}
}

