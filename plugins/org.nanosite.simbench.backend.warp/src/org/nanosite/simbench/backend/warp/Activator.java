package org.nanosite.simbench.backend.warp;

import java.io.File;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.nanosite.common.util.file.ResourceExtractor;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.nanosite.simbench.backend.warp"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static boolean extractedToolsDir = false;

	// TODO: Is this really used?
	public static String getToolsDir() {
		String tempDir = System.getProperty("java.io.tmpdir") + "simbench/tools";
		if (extractedToolsDir) {
			// already extracted
			return tempDir;
		}

		// ensure directory is available
		File dir = new File(tempDir);
		if (! (dir.exists() || dir.mkdirs())) {
			System.err.println(plugin.getBundle().getSymbolicName() + ": cannot create temporary directory " + tempDir + "!");
			return "";
		}

//		ResourceExtractor ex = new ResourceExtractor(plugin);
//		if (! ex.extractResource("warp.exe", "tools", tempDir)) {
//			System.err.println(plugin.getBundle().getSymbolicName() + ": cannot copy file warp.exe!");
//			return "";
//		}
//		if (! ex.extractResource("cygwin1.dll", "tools", tempDir)) {
//			System.err.println(plugin.getBundle().getSymbolicName() + ": cannot copy file cygwin1.dll!");
//			return "";
//		}

		extractedToolsDir = true;
		return tempDir;
	}
}
