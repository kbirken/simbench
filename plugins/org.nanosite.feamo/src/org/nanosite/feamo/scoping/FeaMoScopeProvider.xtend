/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.feamo.scoping

import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.nanosite.feamo.feaMo.FeamoFeatureConfig

import static extension org.nanosite.feamo.ModelExtensions.*

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class FeaMoScopeProvider extends AbstractDeclarativeScopeProvider {

	def IScope scope_FeamoFeatureConfig_selected(FeamoFeatureConfig ctxt, EReference ref) {
		Scopes.scopeFor(ctxt.fm.allFeatures)
	}
}
