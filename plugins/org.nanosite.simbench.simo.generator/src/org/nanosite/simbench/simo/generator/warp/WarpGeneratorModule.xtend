package org.nanosite.simbench.simo.generator.warp

import org.eclipse.xtext.generator.IGenerator2
import org.nanosite.simbench.simo.SimModelRuntimeModule

class WarpGeneratorModule extends SimModelRuntimeModule {

	override Class<? extends IGenerator2> bindIGenerator2() {
        typeof(WarpGenerator)
    }	
}
