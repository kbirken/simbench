
package org.nanosite.feamo;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class HbfmStandaloneSetup extends HbfmStandaloneSetupGenerated{

	public static void doSetup() {
		new HbfmStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

