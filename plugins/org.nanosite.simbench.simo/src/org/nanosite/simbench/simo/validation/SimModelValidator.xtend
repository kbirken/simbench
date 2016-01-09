/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.simbench.simo.validation

import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.CheckType
import org.nanosite.simbench.simo.simModel.Action
import org.nanosite.simbench.simo.simModel.Behaviour
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
								error("Precondition " + pFullName + " is unreachable from step " + sFullName,
										s, SimModelPackage.Literals.STEP__NAME, -1)
								error("Precondition " + pFullName + " is unreachable from step " + sFullName,
										precondStep, SimModelPackage.Literals.STEP__NAME, -1);
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
