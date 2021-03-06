/*
 * generated by Xtext
 */
package org.nanosite.simbench.injection.scoping;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;

import org.nanosite.simbench.injection.hbinj.InjBehaviour;
import org.nanosite.simbench.injection.hbinj.InjFunctionBlock;

/**
 * This class contains custom scoping description.
 *
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#scoping
 * on how and when to use it
 *
 */
public class HbinjScopeProvider extends AbstractDeclarativeScopeProvider {

//	public IScope scope_InjContext_config (InjContext ctxt, EReference ref) {
//		return Scopes.scopeFor(ctxt.getFb().getBehaviour());
//	}

	public IScope scope_InjBehaviour_behaviour (InjFunctionBlock ctxt, EReference ref) {
//		Model model = (Model)ctxt.getFb().eContainer().eContainer();
//		System.out.println("config:       " + model.getMain().getConfig().getName());
//		System.out.println("scenario:     " + model.getMain().getScenario().getName());
//		System.out.println("partitioning: " + model.getMain().getPartitioning().getName());

		return Scopes.scopeFor(ctxt.getFb().getBehaviour());
	}

	public IScope scope_InjStep_step (InjBehaviour ctxt, EReference ref) {
		return Scopes.scopeFor(ctxt.getBehaviour().getPlan().getStep());
	}

}
