package org.nanosite.simbench.injection.analyser.hbsim;

import java.util.Iterator;

import org.nanosite.feamo.hbfm.Feature;
import org.nanosite.simbench.hbsim.AddExpr;
import org.nanosite.simbench.hbsim.AtomicExpr;
import org.nanosite.simbench.hbsim.Expr;
import org.nanosite.simbench.hbsim.FSelAndExpr;
import org.nanosite.simbench.hbsim.FSelAtomicExpr;
import org.nanosite.simbench.hbsim.FSelOrExpr;
import org.nanosite.simbench.hbsim.FSelector;
import org.nanosite.simbench.hbsim.HbsimFactory;
import org.nanosite.simbench.hbsim.MultExpr;
import org.nanosite.simbench.hbsim.VP_Expr;


public class VPExprInjector extends InjectorBase {
	public void inject (String valueInj, VP_Expr vpExpr) {
		if (vpExpr.getMust()!=null) {
			// TODO: create two selectors, one for the existing value, one for the injected
			log("                     => cpu=" + vpExpr.getMust().toString());
		} else {
			Iterator<FSelector> sels = vpExpr.getSel().iterator();
			Iterator<Expr> exprs = vpExpr.getAlt().iterator();
			while (sels.hasNext()) {
				FSelector sel = sels.next();
				Expr expr = exprs.next();
				if (matchingInjectConfig(sel)) {
					// found correct selector, replacing value
					int idx = vpExpr.getAlt().indexOf(expr);
					Expr exprNew = createExpr(valueInj);
					vpExpr.getAlt().set(idx, exprNew);
					touch(vpExpr);
					return;
				}
			}

			// didn't find correct selector, add a new one
			vpExpr.getSel().add(createSelector());
			vpExpr.getAlt().add(createExpr(valueInj));
			touch(vpExpr);
		}

	}


	private boolean matchingInjectConfig (FSelector sel) {
		FSelOrExpr expr = (FSelOrExpr)sel.getExpr();
		if (expr.getTerm().size()!=1) {
			return false;
		}
		FSelAndExpr expr2 = expr.getTerm().get(0);
		if (expr2.getFactor().size()!=1) {
			return false;
		}
		FSelAtomicExpr expr3 = expr2.getFactor().get(0);
		Feature feature = expr3.getFeature();
		if (feature==null) {
			return false;
		}
		Feature cmpFeature = InjectConfig.eINSTANCE.getFeature();
		if (feature.getName().equals(cmpFeature.getName())) {
			return true;
		}

		return false;
	}


	private FSelector createSelector () {
		HbsimFactory factory = HbsimFactory.eINSTANCE;
		FSelAtomicExpr expr1 = factory.createFSelAtomicExpr();
		expr1.setFeature(InjectConfig.eINSTANCE.getFeature());
		FSelAndExpr expr2 = factory.createFSelAndExpr();
		expr2.getFactor().add(expr1);
		FSelOrExpr expr3 = factory.createFSelOrExpr();
		expr3.getTerm().add(expr2);
		FSelector expr4 = factory.createFSelector();
		expr4.setExpr(expr3);
		return expr4;
	}


	private Expr createExpr (String value) {
		HbsimFactory factory = HbsimFactory.eINSTANCE;
		AtomicExpr expr1 = factory.createAtomicExpr();
		expr1.setValue(value);
		MultExpr expr2 = factory.createMultExpr();
		expr2.getFactor().add(expr1);
		AddExpr expr3 = factory.createAddExpr();
		expr3.getTerm().add(expr2);
		return expr3;
	}
}
