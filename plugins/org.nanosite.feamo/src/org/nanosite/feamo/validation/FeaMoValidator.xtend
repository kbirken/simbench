/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.feamo.validation

import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.validation.Check
import org.nanosite.feamo.feaMo.FeaMoPackage
import org.nanosite.feamo.feaMo.FeatureConstraint
import org.nanosite.feamo.feaMo.FeatureModel

import static extension org.nanosite.feamo.ModelExtensions.*
import static extension org.nanosite.feamo.validation.ValidationHelpers.*

/**
 * This class contains custom validation rules for the FeaMo language. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class FeaMoValidator extends AbstractFeaMoValidator {
	
	@Check
	def checkFeatureModel(FeatureModel fm) {
		fm.checkFeatureNamesAreUnique
		fm.checkFeatureDefsAreUnique
	}
	
	def private checkFeatureNamesAreUnique(FeatureModel fm) {
		val Map<EObject, String> names = newHashMap
		for(f : fm.allFeatures) {
			names.put(f, f.name)						
		}
		checkDuplicates(names, FeaMoPackage.Literals.FEATURE__NAME)
	}

	def private checkFeatureDefsAreUnique(FeatureModel fm) {
		val Map<EObject, String> names = newHashMap
		for(fdef : fm.defs) {
			names.put(fdef, fdef.feature.name)						
		}
		checkDuplicates(names, FeaMoPackage.Literals.FEATURE_DEF__FEATURE)
	}


	@Check
	def checkSelfRestriction(FeatureConstraint it) {
		if (feature1.name == feature2.name) {
			error("Features must be distinct in constraint", it,
				FeaMoPackage.Literals.FEATURE_CONSTRAINT__FEATURE1
			)
		}
	}

}
