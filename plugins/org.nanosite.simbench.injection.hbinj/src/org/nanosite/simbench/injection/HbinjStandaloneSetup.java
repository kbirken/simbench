
package org.nanosite.simbench.injection;


/**
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
public class HbinjStandaloneSetup extends HbinjStandaloneSetupGenerated{

	public static void doSetup() {
		new HbinjStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

//	@Override
//	public void register(Injector injector)
//	{
//		System.out.println("HbinjStandaloneSetup.register");
//		super.register(injector);
//		Registry registry = injector.getInstance(IResourceServiceProvider.Registry.class);
//		registry.getExtensionToFactoryMap().put("hbsim", new HbsimResourceServiceProviderImpl());
//	}

}

