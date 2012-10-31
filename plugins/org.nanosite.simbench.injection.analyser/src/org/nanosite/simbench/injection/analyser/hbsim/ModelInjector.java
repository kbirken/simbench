package org.nanosite.simbench.injection.analyser.hbsim;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.nanosite.simbench.HbsimModelAccessor;
import org.nanosite.simbench.hbsim.Element;
import org.nanosite.simbench.hbsim.FunctionBlock;
import org.nanosite.simbench.hbsim.Model;
import org.nanosite.simbench.hbsim.VP_Element;
import org.nanosite.simbench.hbsim.impl.FunctionBlockImpl;
import org.nanosite.simbench.injection.hbinj.InjBackground;
import org.nanosite.simbench.injection.hbinj.InjFunctionBlock;
import org.nanosite.simbench.injection.hbinj.InjModel;

public class ModelInjector extends InjectorBase {

	public void inject (InjModel modelInj, Model model) {
		Map<String, FunctionBlock> map = null;
		if (model != null) {
			// prepare hash of function blocks
			List<VP_Element> vpElems = HbsimModelAccessor.getVPElements(model);
			map = new TreeMap<String, FunctionBlock>();
			for(VP_Element vpElem : vpElems) {
				Element elem = vpElem.getOpt();
				if (elem.getClass() == FunctionBlockImpl.class) {
					FunctionBlock fb = (FunctionBlock)elem;
					map.put(fb.getName(), fb);
				}
			}
		}

		// traverse injected function blocks and inject them one after another
		FBInjector injector = new FBInjector();
		int nInjected = 0;
		for(InjFunctionBlock fbInj : modelInj.getFbs()) {
			String name = fbInj.getFb().getName();
			if (model==null) {
				log("Inject: fb " + name);
				injector.inject(fbInj, null);
				nInjected++;
			} else {
				if (! map.containsKey(name)) {
					error(fbInj, "Injected function block " + name + " is not available in the model");
				} else {
					log("Inject: fb " + name);
					injector.inject(fbInj, map.get(name));
					nInjected++;
				}
			}
		}
		log("Injected " + nInjected + " function blocks.");

		// handle global background section
		if (model==null) {
			InjBackground bgInj = modelInj.getBg_action();
			ActionInjector actionInjector = new ActionInjector();
			actionInjector.inject(bgInj.getAction(), null);
		} else {
			// TODO
		}
		log("Injected global background.");
	}
}
