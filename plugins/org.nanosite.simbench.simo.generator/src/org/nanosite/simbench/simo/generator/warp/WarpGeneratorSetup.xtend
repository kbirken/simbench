package org.nanosite.simbench.simo.generator.warp

import com.google.inject.Guice
import com.google.inject.Injector
import org.nanosite.simbench.simo.SimModelStandaloneSetup

class WarpGeneratorSetup extends SimModelStandaloneSetup {

	override Injector createInjector() {
		Guice.createInjector(new WarpGeneratorModule)
	}
	
}
