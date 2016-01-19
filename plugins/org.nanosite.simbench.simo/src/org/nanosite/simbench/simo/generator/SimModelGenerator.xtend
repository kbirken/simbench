/*
 * generated by Xtext 2.9.0
 */
package org.nanosite.simbench.simo.generator

import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.IGeneratorContext

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class SimModelGenerator extends AbstractGenerator {

	static List<IGenerator2> generators = newArrayList

	def static addGenerator(IGenerator2 generator) {
		println("SimModelGenerator: added " + generator)
		generators.add(generator);
	}

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		for(gen : generators) {
			gen.doGenerate(resource, fsa, context)
		}
	}
}
