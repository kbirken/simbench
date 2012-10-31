
package org.nanosite.simbench;


/**
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
public class HbsimStandaloneSetup extends HbsimStandaloneSetupGenerated {

	public static void doSetup() {
		//System.out.println("HbsimStandaloneSetup.doSetup");
		new HbsimStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

//	@Override
//	public void register(Injector injector)
//	{
//		//System.out.println("HbsimStandaloneSetup.register");
//		super.register(injector);
//		Registry registry = injector.getInstance(IResourceServiceProvider.Registry.class);
//		registry.getExtensionToFactoryMap().put("hbfm", new FeamoResourceServiceProviderImpl());
//	}

}

