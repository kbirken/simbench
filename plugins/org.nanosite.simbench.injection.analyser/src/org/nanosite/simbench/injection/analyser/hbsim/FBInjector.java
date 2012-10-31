package org.nanosite.simbench.injection.analyser.hbsim;

import java.util.Map;
import java.util.TreeMap;

import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.FunctionBlock;
import org.nanosite.simbench.injection.hbinj.InjBehaviour;
import org.nanosite.simbench.injection.hbinj.InjFunctionBlock;

public class FBInjector extends InjectorBase {

	public void inject (InjFunctionBlock fbInj, FunctionBlock fb) {
		// prepare hash of behaviours
		Map<String, Behaviour> map = null;
		if (fb!=null) {
			map = new TreeMap<String, Behaviour>();
			for(Behaviour bhvr : fb.getBehaviour()) {
				map.put(bhvr.getName(), bhvr);
			}
		}

		// traverse injected behaviour instances and inject them one after another
		BehaviourInjector injector = new BehaviourInjector();
		for(InjBehaviour bhvrInj : fbInj.getBehaviour()) {
			if (fb==null) {
				injector.inject(bhvrInj, null);
			} else {
				String name = bhvrInj.getBehaviour().getName();
				if (! map.containsKey(name)) {
					error(bhvrInj, "Injected behaviour " + name + "() is not available in the model");
				} else {
					injector.inject(bhvrInj, map.get(name));
				}
			}
		}
	}
}
