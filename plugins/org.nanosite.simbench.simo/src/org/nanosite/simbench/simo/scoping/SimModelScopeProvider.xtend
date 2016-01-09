/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.simbench.simo.scoping

import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.nanosite.simbench.simo.simModel.ResourceInterfaceRef
import org.nanosite.simbench.simo.simModel.StepRef
import org.nanosite.simbench.simo.simModel.TriggerCall

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class SimModelScopeProvider extends AbstractDeclarativeScopeProvider {

	def IScope scope_StepRef_step(StepRef sr, EReference ref) {
		val List<EObject> scopes = newArrayList
		for(b : sr.fb.behaviour) {
			scopes.addAll(b.plan.step)
		}
		Scopes.scopeFor(scopes)
	}

	def IScope scope_ResourceInterfaceRef_interface(ResourceInterfaceRef rir, EReference ref) {
		Scopes.scopeFor(rir.resource.interface)
	}

	def IScope scope_TriggerCall_trigger(TriggerCall tr, EReference ref) {
		val List<EObject> scopes = newArrayList
		for(b : tr.fb.behaviour) {
			scopes.add(b)
			if (b.repeat?.until!=null) {
				scopes.add(b.repeat?.until)
			}
		}
		Scopes.scopeFor(scopes)
	}


	/*
	 * COMMENTS COPIED FROM OLD HBSIM DSL, SHOULD THIS BE MIGRATED TO SIMO DSL?
	public IScope scope_FunctionBlock(FunctionBlock fb, EClass clazz) {
		EList<Step> steps = fb.getPlan().getStep();
		final List<IScopedElement> scopes = new ArrayList<IScopedElement>(10);
		for (Iterator<Step> i= steps.iterator(); i.hasNext();) {
			Step s = (Step)i.next();
			scopes.add(ScopedElement.create(s.getName(), s));
		}
		return new SimpleScope(IScope.NULLSCOPE, scopes);
	}
	*/

	/*
	 * COMMENTS COPIED FROM OLD HBSIM DSL, SHOULD THIS BE MIGRATED TO SIMO DSL?
	private IScope createStepsScope(FunctionBlock fb) {
		return new SimpleScope(IScope.NULLSCOPE,
				CollectionUtils.map(fb.getPlan().getStep(),
						new Function<Step, IScopedElement>() {
							//public IScopedElement exec(Step step) {
							//	return ScopedElement.create(step.getName(), step);
							//}
						}
				)
		);
	}
	*/

}
