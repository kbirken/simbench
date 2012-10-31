package org.nanosite.simbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.typesystem.emf.EmfMetaModel;

import org.nanosite.feamo.hbfm.Feature;
import org.nanosite.feamo.hbfm.HbfmPackage;
import org.nanosite.simbench.hbsim.Action;
import org.nanosite.simbench.hbsim.AddExpr;
import org.nanosite.simbench.hbsim.AtomicExpr;
import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.Expr;
import org.nanosite.simbench.hbsim.FSelAndExpr;
import org.nanosite.simbench.hbsim.FSelAtomicExpr;
import org.nanosite.simbench.hbsim.FSelExpr;
import org.nanosite.simbench.hbsim.FSelOrExpr;
import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.HbsimPackage;
import org.nanosite.simbench.hbsim.Mapping;
import org.nanosite.simbench.hbsim.Marker;
import org.nanosite.simbench.hbsim.MarkerSet;
import org.nanosite.simbench.hbsim.MultExpr;
import org.nanosite.simbench.hbsim.Partitioning;
import org.nanosite.simbench.hbsim.Plan;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.hbsim.VP_Expr;
import org.nanosite.simbench.hbsim.VP_Mapping;
import org.nanosite.simbench.hbsim.VP_Option;
import org.nanosite.simbench.hbsim.VP_Optional;
import org.nanosite.simbench.hbsim.VP_Selectable;
import org.nanosite.simbench.hbsim.VP_Selector;


// TODO: refactor this class

public class HbsimHelper {

	private static Map<String,XtendFacade> xtendFacades = new HashMap<String,XtendFacade>();

	public static XtendFacade getXtendFacade(String xtendFile) {
		if (! xtendFacades.containsKey(xtendFile)) {
			// create on demand
			XtendFacade facade = XtendFacade.create(xtendFile);
			if (facade==null) {
				System.err.println("HbsimHelper error: couldn't initialize XtendFacade!");
			}

			xtendFacades.put(xtendFile, facade);

			// register hbfm metamodel
			EmfMetaModel mmHbfm = new EmfMetaModel();
			mmHbfm.setMetaModelPackage(HbfmPackage.class.getName());
			facade.registerMetaModel(mmHbfm);

			// register hbsim metamodel
			EmfMetaModel mmHbsim = new EmfMetaModel();
			mmHbsim.setMetaModelPackage(HbsimPackage.class.getName());
			facade.registerMetaModel(mmHbsim);
		}

		return xtendFacades.get(xtendFile);
	}


	public static List<Mapping> getAllMappings (Partitioning partitioning, FeatureConfig config) {
		List<Mapping> results = new ArrayList<Mapping>();

		// add own mappings
		Set<Feature> features = getFeatures(config);
		for(VP_Mapping vp : getAllVPMappings(partitioning)) {
			if (vp.getSel()==null || matches(vp.getSel().getExpr(), features)) {
				results.add(vp.getOpt());
			}
		}
		return results;
	}

	private static List<VP_Mapping> getAllVPMappings (Partitioning partitioning) {
		List<VP_Mapping> results = new ArrayList<VP_Mapping>();
		results.addAll(partitioning.getMapping());
		if (partitioning.getBase()!=null) {
			results.addAll(getAllVPMappings(partitioning.getBase()));
		}
		return results;
	}

	public static <T extends VP_Selectable, VP extends VP_Selector> T bind (VP vp, FeatureConfig config) {
		if (vp.getSel()==null)
			return vp.getMust();

		Set<Feature> cfg = getFeatures(config);
		for(int i=0; i<vp.getSel().size(); i++) {
			if (matches(vp.getSel().get(i).getExpr(), cfg)) {
				EList<T> t = vp.getAlt();
				return t.get(i);
			}
		}
		return vp.getMust();
	}

	public static <T extends VP_Optional, VP extends VP_Option> T bind (VP vp, FeatureConfig config) {
		Set<Feature> features = getFeatures(config);
		if (vp.getSel()==null || matches(vp.getSel().getExpr(), features))
			return vp.getOpt();
		return null;
	}


	public static Behaviour getBehaviour (Step step) {
		Plan plan = (Plan)step.eContainer();
		return (Behaviour)plan.eContainer();
	}

	public static boolean isLastStep (Step step) {
		Plan plan = (Plan)step.eContainer();
		return plan.getStep().indexOf(step) == plan.getStep().size()-1;
	}

	public static Step getLastStep (Behaviour behaviour) {
		List<Step> steps = behaviour.getPlan().getStep();
		if (steps.size()>0) {
			return steps.get(steps.size()-1);
		}
		return null;
	}

	public static double getUsedCPU (Step step, FeatureConfig config, boolean cumulated) {
		double used = 0.0;
		if (step!=null && config!=null) {
			if (cumulated) {
				Plan plan = (Plan)step.eContainer();
				Iterator<Step> si = plan.getStep().iterator();
				Step s;
				do {
					s = si.next();
					used += getUsedCPU(s, config);
				} while(si.hasNext() && s!=step);
			} else {
				used = getUsedCPU(step, config);
			}
		}
		return used;
	}

	private static double getUsedCPU (Step step, FeatureConfig config) {
		double used = 0.0;
		if (step!=null && config!=null) {
			if (step.getAction()!=null && step.getAction().size()==1) {
				Action action = step.getAction().get(0);
				if (action.getConsumedCPU()!=null) {
					used = bindAndEval(action.getConsumedCPU(), config);
				}
			}
		}
		return used;
	}


	public static String getMarkerString (MarkerSet markerSet) {
		String mstr = "";
		String msep = "";
		for(Marker m : markerSet.getMarker()) {
			// compute marker name
			String mn = m.getType().getName() + "(";
			boolean isFirst = true;
			for(String p : m.getParam()) {
				if (!isFirst) mn += " ";
				mn += '"' + p + '"';
				isFirst=false;
			}
			mn += ")";
			mstr += msep + mn;
			msep = " and ";
		}
		return mstr;
	}


	public static double bindAndEval (VP_Expr vpExpr, FeatureConfig config) {
		Set<Feature> features = getFeatures(config);
		return eval(vpExpr, features);
	}

	private static double eval (VP_Expr vpExpr, Set<Feature> cfg) {
		if (vpExpr.getSel()==null)
			return eval(vpExpr.getMust(), cfg);

		for(int i=0; i<vpExpr.getSel().size(); i++) {
			if (matches(vpExpr.getSel().get(i).getExpr(), cfg)) {
				return eval(vpExpr.getAlt().get(i), cfg);
			}
		}
		return eval(vpExpr.getMust(), cfg);
	}

	public static double eval (Expr expr, Set<Feature> cfg) {
		if (expr instanceof AddExpr) {
			AddExpr ae = (AddExpr)expr;
			double sum = 0.0;
			for(int i=0; i<ae.getTerm().size(); i++) {
				MultExpr me = ae.getTerm().get(i);
				double product = eval(me.getFactor().get(0), cfg);
				for(int j=1; j<me.getFactor().size(); j++) {
					double f = eval(me.getFactor().get(j), cfg);
					if (me.getOp().get(j-1).equals("*")) {
						product = product * f;
					} else {
						product = product / f;
					}
				}
				if (i==0) {
					sum = product;
				} else {
					if (ae.getOp().get(i-1).equals("+")) {
						sum = sum + product;
					} else {
						sum = sum - product;
					}
				}
			}
			return sum;
		}
		return 7777.0; // TODO
	}

	private static double eval (AtomicExpr expr, Set<Feature> cfg) {
		if (expr.getValue()!=null) {
			return Double.parseDouble(expr.getValue());
		}
		if (expr.getVar()!=null) {
			return eval(expr.getVar().getRhs(), cfg);
		}
		return eval((Expr)expr, cfg);
	}



	// get features along the FeatureConfig hierarchy
	private static Set<Feature> getFeatures (FeatureConfig config) {
		Set<Feature> results = null;
		if (config.getBase()!=null) {
			results = getFeatures(config.getBase());
		} else {
			results = new HashSet<Feature>();
		}
		results.removeAll(config.getMinus());
		results.addAll(config.getPlus());
		return results;
	}


	// TODO move this to HbfmHelper
	private static boolean matches (FSelExpr expr, Set<Feature> cfg) {
		if (expr==null || ! (expr instanceof FSelOrExpr))
			return false;

		FSelOrExpr orExpr = (FSelOrExpr)expr;
		for(FSelAndExpr andExpr : orExpr.getTerm()) {
			if (matches(andExpr, cfg))
				return true;
		}
		return false;
	}

	private static boolean matches (FSelAndExpr andExpr, Set<Feature> cfg) {
		for(FSelAtomicExpr expr : andExpr.getFactor()) {
			if (! matches(expr, cfg))
				return false;
		}
		return true;
	}

	private static boolean matches (FSelAtomicExpr expr, Set<Feature> cfg) {
		if (expr.getFeature()==null)
			return matches((FSelExpr)expr, cfg);
		boolean sel = cfg.contains(expr.getFeature());
		return expr.isNeg() ? !sel : sel;
	}
}
