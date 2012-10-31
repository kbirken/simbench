package org.nanosite.simbench.hbsim.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nanosite.simbench.HbsimHelper;
import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.FunctionBlock;
import org.nanosite.simbench.hbsim.IOActivity;
import org.nanosite.simbench.hbsim.Mapping;
import org.nanosite.simbench.hbsim.Partitioning;
import org.nanosite.simbench.hbsim.Precondition;
import org.nanosite.simbench.hbsim.Resource;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.hbsim.StepRef;
import org.nanosite.simbench.hbsim.TriggerCall;
import org.nanosite.simbench.hbsim.VP_IOActivity;
import org.nanosite.simbench.hbsim.VP_TriggerCall;


public class SimModelShell {

	private SimModelDescriptor modelDescriptor = null;

	// data derived from model (for fast access)
	private Map<Resource,Step> resourceProviders = new HashMap<Resource,Step>();
	private Map<Behaviour,List<Behaviour>> behaviourTriggerers = new HashMap<Behaviour,List<Behaviour>>();
	private Map<Step,List<Step>> stepPredecessors = new HashMap<Step,List<Step>>();

	public SimModelShell(SimModelDescriptor modelDescriptor) {
		this.modelDescriptor = modelDescriptor;
		buildAdditions();
	}

	public Step getProvider (Resource res) {
		return resourceProviders.get(res);
	}

	public List<Behaviour> getTriggerers (Behaviour target) {
		return behaviourTriggerers.get(target);
	}

	public List<Step> getPredecessors (Step step) {
		return stepPredecessors.get(step);
	}


	// **********************************************************************

	private void buildAdditions() {
		resourceProviders.clear();

		// iterate through all mapped function blocks
		FeatureConfig config = modelDescriptor.getConfig();
		Partitioning partitioning = modelDescriptor.getPartitioning();
		if (config!=null && partitioning!=null) {
			List<Mapping> mappings = HbsimHelper.getAllMappings(partitioning, config);

			// pass 1
			for(Mapping map : mappings) {
				FunctionBlock fb = map.getFb();
				for(Behaviour bhvr : fb.getBehaviour()) {
					for(VP_TriggerCall vpCall : bhvr.getSend()) {
						TriggerCall call = HbsimHelper.bind(vpCall, config);
						if (call!=null) {
							Behaviour triggerTarget = call.getTrigger();
							List<Behaviour> triggerers = behaviourTriggerers.get(triggerTarget);
							if (triggerers==null) {
								triggerers = new ArrayList<Behaviour>();
								behaviourTriggerers.put(triggerTarget, triggerers);
							}
							triggerers.add(bhvr);

//							Step lastStep = HbsimHelper.getLastStep(bhvr);
//							if (! triggerTarget.getPlan().getStep().isEmpty()) {
//								Step targetStep = triggerTarget.getPlan().getStep().get(0);
//								if (lastStep!=null && targetStep!=null) {
//									addPredecessor(targetStep, lastStep);
//								}
//							}
						}
					}
					Step last = null;
					for(Step step : bhvr.getPlan().getStep()) {
						if (step.getProvides()!=null) {
							resourceProviders.put(step.getProvides(), step);
						}
						if (last!=null)
							addPredecessor(step, last);
						last = step;

						for(Precondition prec : step.getPrecondition()) {
							StepRef sr = HbsimHelper.bind(prec.getRef(), config);
							if (sr!=null) {
								addPredecessor(step, sr.getStep());
							}
						}
					}
				}
			}

			// pass 2
			for(Mapping map : mappings) {
				FunctionBlock fb = map.getFb();
				for(Behaviour bhvr : fb.getBehaviour()) {
					for(Step step : bhvr.getPlan().getStep()) {
						if (step.getAction()!=null && step.getAction().size()==1) {
							for(VP_IOActivity vpIO : step.getAction().get(0).getIo()) {
								IOActivity io = HbsimHelper.bind(vpIO, config);
								if (io!=null && io.getIoInterface()!=null) {
									addPredecessor(step, getProvider(io.getIoInterface().getResource()));
								}
							}
						}
					}
				}
			}
		}
	}

	private void addPredecessor (Step step, Step predecessor) {
		if (predecessor==null || step==null)
			return;

		List<Step> predecessors = stepPredecessors.get(step);
		if (predecessors==null) {
			predecessors = new ArrayList<Step>();
			stepPredecessors.put(step, predecessors);
		}
		predecessors.add(predecessor);
	}
}
