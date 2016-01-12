/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.simbench.simo.validation

import java.util.Collection
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.CheckType
import org.nanosite.simbench.simo.simModel.Action
import org.nanosite.simbench.simo.simModel.Behaviour
import org.nanosite.simbench.simo.simModel.Import
import org.nanosite.simbench.simo.simModel.Model
import org.nanosite.simbench.simo.simModel.SimModelPackage

import static extension org.nanosite.simbench.simo.FeatureBinder.*
import static extension org.nanosite.simbench.simo.ModelExtensions.*

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class SimModelValidator extends AbstractSimModelValidator {
	
	@Check
	def checkImportPath(Import imp) {
		val path = imp.importURI
		if (path.empty) {
			error("Please specify a valid filename", imp, SimModelPackage.eINSTANCE.import_ImportURI)
			return
		}

		if (! (path.endsWith(".smd") || path.endsWith(".fmd"))) {
			error("Imported file extension should be 'smd' or 'fmd'",
				imp, SimModelPackage.eINSTANCE.import_ImportURI
			)
		}
		
		// TODO: check that imported model file exists
		
//		Collection collection = (Collection)module.eContainer();
//		URI uri = null;
//		if (collection.getPath()==null) {
//			// collection without path specification, we check against local folder
//
//			// get resource URI where module is located, remove the final part
//			uri = module.eResource().getURI().trimSegments(1);
//		} else {
//			// collection with path specified, combine this with module's file
//			uri = URI.createFileURI(collection.getPath());
//			System.out.println("uri = " + uri.toString());
//		}
//
//		// construct URI for module file
//		URI uri2 = uri.appendSegments(module.getFile().split("/"));
//		
//		ExtensibleURIConverterImpl conv = new ExtensibleURIConverterImpl();
//		if (! conv.exists(uri2, null)) {
//			error("File doesn't exist", module, RemixPackage.eINSTANCE.getModule_File());
//		}
	}

	@Check(CheckType.FAST)
	def checkFB (Behaviour b) {
		if (b.plan==null || b.plan.step.empty) {
			error("Behavior " + b.name + " doesn't have steps", b,
					SimModelPackage.Literals.BEHAVIOUR__NAME, -1)
		} else {
//			ValidationHelpers.checkDuplicates(this, b.getPlan().getStep(),
//					SimModelPackage.Literals.PLAN__STEP, "step")
		}
	}

	@Check(CheckType.FAST)
	def checkAction (Action a) {
		if (a.consumedCPU!=null && a.cpu==null) {
			warning("Specifying cpu consumption without reference to CPU is deprecated.",
					a, SimModelPackage.Literals.ACTION__CONSUMED_CPU, -1)
		}
	}


	@Check(CheckType.NORMAL) // TODO: maybe EXPENSIVE
	def checkModel (Model model) {
		if (model.main==null)
			return

		val cfg = model.main.config
		val behaviors = model.allReachableBehaviours 
		for(bhvr : behaviors) {
			if (bhvr.plan!=null) {
				for(s : bhvr.plan.step) {
					for(prec : s.precondition) {
						val stepRef = prec.ref.bind(cfg)
						if (stepRef!=null) {
							val precondStep = stepRef.step
							if (! behaviors.contains(precondStep.getBehaviour)) {
								val sFullName = s.fullName
								val pFullName = precondStep.fullName
//								error("Precondition " + pFullName + " is unreachable from step " + sFullName,
//										s, SimModelPackage.Literals.STEP__NAME, -1)
//								error("Precondition " + pFullName + " is unreachable from step " + sFullName,
//										precondStep, SimModelPackage.Literals.STEP__NAME, -1);
								// WORKAROUND: cannot report error for another resource
								error("Precondition " + pFullName + " is unreachable from step " + sFullName,
										model.main, SimModelPackage.Literals.MAIN__CONFIG, -1)
							}
						}
					}
				}
			}
//			println("bhvr: " + bhvr.bhvr.name
		}

//		Scenario scenario = model.getMain().getScenario()
//		for(UseCase uc : scenario.getUsecase()) {
//			for(VP_TriggerCall vpTC : uc.getPart()) {
//				TriggerCall tc = HbsimHelper.bind(vpTC, config)
//			}
//		}
	}

	
}
