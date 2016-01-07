/*
 * generated by Xtext 2.9.0
 */
package org.nanosite.feamo.tests

import com.google.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.nanosite.feamo.feaMo.Model

@RunWith(XtextRunner)
@InjectWith(FeaMoInjectorProvider)
class FeaMoParsingTest{

	@Inject
	ParseHelper<Model> parseHelper;

	@Test 
	def void loadModel() {
		val result = parseHelper.parse('''
			Hello Xtext!
		''')
		Assert.assertNotNull(result)
	}

}