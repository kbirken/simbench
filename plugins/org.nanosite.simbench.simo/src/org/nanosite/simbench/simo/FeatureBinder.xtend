package org.nanosite.simbench.simo

import org.nanosite.simbench.simo.simModel.Element
import org.nanosite.simbench.simo.simModel.FeatureConfig
import org.nanosite.simbench.simo.simModel.StepRef
import org.nanosite.simbench.simo.simModel.TriggerCall
import org.nanosite.simbench.simo.simModel.VP_Element
import org.nanosite.simbench.simo.simModel.VP_StepRef
import org.nanosite.simbench.simo.simModel.VP_TriggerCall

import static extension org.nanosite.simbench.simo.FeatureSelectionHelper.*

/**
 * Helper class for binding variation points.
 */
class FeatureBinder {

	def static Element bind(VP_Element vp, FeatureConfig cfg) {
		if (vp.sel==null)
			vp.opt
		else {
			if (vp.sel.matches(cfg))
				vp.opt
			else
				null
		}
	}

//cached IOActivity bind (VP_IOActivity vp, FeatureConfig cfg) :
//	vp.sel==null ? vp.opt :
//		(vp.sel.matches(cfg) ? vp.opt : null);
//
//cached Mapping bind (VP_Mapping vp, FeatureConfig cfg) :
//	vp.sel==null ? vp.opt :
//		(vp.sel.matches(cfg) ? vp.opt : null);
//
//cached Expr bind (VP_Expr vp, FeatureConfig cfg) :
//	vp.must!=null ? vp.must :
//		(let idx = vp.sel.getBindingIndex(cfg) : (idx>=0 ? vp.alt.get(idx) : null));

	def static TriggerCall bind(VP_TriggerCall vp, FeatureConfig cfg) {
		if (vp.must!=null)
			vp.must
		else {
			val idx = vp.sel.getBindingIndex(cfg)
			if (idx>=0)
				vp.alt.get(idx)
			else
				null
		}
	}

	def static StepRef bind(VP_StepRef vp, FeatureConfig cfg) {
		if (vp.must!=null)
			vp.must
		else {
			val idx = vp.sel.getBindingIndex(cfg)
			if (idx>=0)
				vp.alt.get(idx)
			else
				null
		}
	}
	
}
