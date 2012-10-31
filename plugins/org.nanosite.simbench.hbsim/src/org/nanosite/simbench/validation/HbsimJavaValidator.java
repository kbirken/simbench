package org.nanosite.simbench.validation;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import org.nanosite.simbench.HbsimHelper;
import org.nanosite.simbench.hbsim.Action;
import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.HbsimPackage;
import org.nanosite.simbench.hbsim.Model;
import org.nanosite.simbench.hbsim.Precondition;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.hbsim.StepRef;


public class HbsimJavaValidator extends AbstractHbsimJavaValidator implements ValidationMessageReporter
{

//	@Check(CheckType.FAST)
//	public void checkModel (Model model) {
//		Iterable<FunctionBlock> fbs = HbsimModelAccessor.getFunctionBlocks(model);
//		ValidationHelpers.checkDuplicates(this, fbs,
//				HbsimPackage.Literals.ELEMENT__NAME, "function block");
//	}

//	@Check(CheckType.FAST)
//	public void checkFB (FunctionBlock fb) {
//		ValidationHelpers.checkDuplicates(this, fb.getBehaviour(),
//				HbsimPackage.Literals.BEHAVIOUR__NAME, "behaviour");
//	}

	@Check(CheckType.FAST)
	public void checkFB (Behaviour b) {
		if (b.getPlan()==null || b.getPlan().getStep().size()==0) {
			error("Behaviour " + b.getName() + " doesn't have steps", b,
					HbsimPackage.Literals.BEHAVIOUR__NAME, -1);
		} else {
//			ValidationHelpers.checkDuplicates(this, b.getPlan().getStep(),
//					HbsimPackage.Literals.PLAN__STEP, "step");
		}
	}

	@Check(CheckType.FAST)
	public void checkAction (Action a) {
		if (a.getConsumedCPU()!=null && a.getCpu()==null) {
			warning("Specifying cpu consumption without reference to CPU is deprecated.",
					a, HbsimPackage.Literals.ACTION__CONSUMED_CPU, -1);
		}
	}


	@Check(CheckType.NORMAL) // TODO: maybe EXPENSIVE
	public void checkModel (Model model) {
		if (model.getMain()==null)
			return;

		XtendFacade xtend = HbsimHelper.getXtendFacade("templates::helpers");
		if (xtend==null) {
			System.err.println("HbsimJavaValidator error: couldn't initialize XtendFacade!");
			return;
		}

		FeatureConfig config = model.getMain().getConfig();
		@SuppressWarnings("unchecked")
		Set<Behaviour> behaviours = (Set<Behaviour>)xtend.call("getAllReachableBehaviours", new Object[]{model});
		for(Behaviour bhvr : behaviours) {
			if (bhvr.getPlan()!=null) {
				for(Step s : bhvr.getPlan().getStep()) {
					for(Precondition prec : s.getPrecondition()) {
						StepRef sr = HbsimHelper.bind(prec.getRef(), config);
						if (sr!=null) {
							Step precondStep = sr.getStep();
							if (! behaviours.contains(HbsimHelper.getBehaviour(precondStep))) {
								String sFullName = (String)xtend.call("fullName", new Object[]{s});
								String pFullName = (String)xtend.call("fullName", new Object[]{precondStep});
								error("Precondition " + pFullName + " is unreachable from step " + sFullName, s,
										HbsimPackage.Literals.STEP__NAME, -1);
								error("Precondition " + pFullName + " is unreachable from step " + sFullName,
										precondStep, HbsimPackage.Literals.STEP__NAME, -1);
							}
						}
					}
				}
			}
//			System.out.println("bhvr: " + bhvr.bhvr.getName()
		}

//		Scenario scenario = model.getMain().getScenario();
//		for(UseCase uc : scenario.getUsecase()) {
//			for(VP_TriggerCall vpTC : uc.getPart()) {
//				TriggerCall tc = HbsimHelper.bind(vpTC, config);
//			}
//		}
	}



	// *****************************************************************************

	// ValidationMessageReporter interface
	public void reportError(String message, EObject object, EStructuralFeature feature)
	{
		error(message, object, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}

	public void reportWarning(String message, EObject object, EStructuralFeature feature)
	{
		warning(message, object, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}
}
