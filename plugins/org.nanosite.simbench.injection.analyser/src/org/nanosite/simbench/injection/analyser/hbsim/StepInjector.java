package org.nanosite.simbench.injection.analyser.hbsim;

import org.nanosite.simbench.hbsim.Action;
import org.nanosite.simbench.hbsim.HbsimFactory;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.injection.hbinj.InjAction;
import org.nanosite.simbench.injection.hbinj.InjStep;

public class StepInjector extends InjectorBase {

	public void inject (InjStep stepInj, Step step) {
		log("     inject step " + stepInj.getStep().getName());

		ActionInjector injector = new ActionInjector();
		InjAction actionInj = stepInj.getAction();
		if (injector.containsData(actionInj)) {
			if (step==null) {
				injector.inject(actionInj, null);
			} else {
				if (step.getAction().isEmpty()) {
					// new Action object must be created
					HbsimFactory factory = HbsimFactory.eINSTANCE;
					step.getAction().add(factory.createAction());
					touch(step);
					return;
				}

				Action action = step.getAction().get(0);
				injector.inject(actionInj, action);
			}
		}
	}
}
