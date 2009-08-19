package org.freeplane.plugin.svg;

import java.util.Hashtable;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.view.swing.addins.filepreview.PreviewController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(final ModeController modeController) {
				    final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				    final Controller controller = modeController.getController();
				    menuBuilder.addAnnotatedAction(new ExportPdf(controller));
				    menuBuilder.addAnnotatedAction(new ExportSvg(controller));
				    PreviewController extension = (PreviewController) modeController.getExtension(PreviewController.class);
				    extension.addFactory(new SvgViewerFactory());
			    }
		    }, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
