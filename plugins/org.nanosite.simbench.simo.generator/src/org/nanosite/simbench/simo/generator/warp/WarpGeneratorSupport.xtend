package org.nanosite.simbench.simo.generator.warp

import org.eclipse.xtext.resource.generic.AbstractGenericResourceSupport

class WarpGeneratorSupport extends AbstractGenericResourceSupport {
	
	override protected createGuiceModule() {
		new WarpGeneratorModule
	}
	
}
