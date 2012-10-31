package org.nanosite.simbench.injection.analyser.hbsim;

import java.util.Iterator;

import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.injection.hbinj.InjBehaviour;
import org.nanosite.simbench.injection.hbinj.InjStep;


public class BehaviourInjector extends InjectorBase {

	public void inject (InjBehaviour bhvrInj, Behaviour bhvr) {
		log("  inject behaviour: " + bhvrInj.getBehaviour().getName());

		if (bhvrInj.getStep().size() == 0) {
			warning(bhvrInj, "Injected behaviour " + bhvrInj.getBehaviour().getName() + "() has no steps");
			return;
		}

		if (bhvr!=null) {
			if (bhvrInj.getStep().size() > bhvr.getPlan().getStep().size()) {
				error(bhvrInj, "Injected behaviour " + bhvrInj.getBehaviour().getName() + "() has too many steps");
				return;
			}
		}

		StepInjector injector = new StepInjector();
		if (bhvr==null) {
			for(InjStep stepInj : bhvrInj.getStep()) {
				injector.inject(stepInj, null);
			}
		} else {
			// iterate through steps, check order and inject steps
			Iterator<InjStep> stepsInj = bhvrInj.getStep().iterator();
			InjStep stepInj = stepsInj.next();
			for(Step step : bhvr.getPlan().getStep()) {
				if (stepInj!=null && stepInj.getStep().getName().equals(step.getName())) {
					// inject data
					injector.inject(stepInj, step);
					stepInj = stepsInj.hasNext() ? stepsInj.next() : null;
				} else {
					// no injected data for this step
					log("     ignore step " + step.getName());
				}
			}
			if (stepInj!=null) {
				error(stepInj, "Step mismatch for step " + stepInj.getStep().getName() +
						" in behaviour " + bhvrInj.getBehaviour().getName() + "()");
			}
		}
	}
}
