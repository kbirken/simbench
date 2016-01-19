package org.nanosite.simbench.simo.ui;

import org.nanosite.simbench.simo.generator.SimModelGenerator;
import org.nanosite.simbench.simo.generator.warp.WarpGenerator;
import org.nanosite.simbench.simo.ui.internal.SimoActivator;
import org.osgi.framework.BundleContext;

public class SimoActivator2 extends SimoActivator {

	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		SimModelGenerator.addGenerator(new WarpGenerator());
	}


}
