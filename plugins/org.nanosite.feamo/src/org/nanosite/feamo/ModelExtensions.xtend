/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.feamo

import java.util.List
import org.nanosite.feamo.feaMo.Feature
import org.nanosite.feamo.feaMo.FeatureDetails
import org.nanosite.feamo.feaMo.FeatureModel
import org.nanosite.feamo.feaMo.SimpleFeature

class ModelExtensions {
	
	def static getAllFeatures(FeatureModel fm) {
		val List<Feature> all = fm.details.allFeatures
		for(fdef : fm.defs) {
			all.addAll(fdef.details.allFeatures)
		}
		all
	}
	
	def private static getAllFeatures(FeatureDetails fd) {
		val List<Feature> all = newArrayList
		for(fg : fd.groups) {
			if (fg.simple!=null) {
				all.add(fg.simple.feature)
			} else {
				val iter = if (fg.or.empty) fg.alt.iterator else fg.or.iterator
				while (iter.hasNext) {
					all.add(iter.next.feature)
				} 
			}
		} 
		all
	}
	
	def private static feature(SimpleFeature sf) {
		if (sf.mandatory!=null)
			sf.mandatory
		else
			sf.optional
	}
}
