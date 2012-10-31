package org.nanosite.simbench.injection.analyser.simuresult;

import org.nanosite.simbench.HbsimHelper;
import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.CPU;
import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.FunctionBlock;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.injection.analyser.toposort.Vertex;

public class SimulationEvent extends Vertex {
	// back-link to owner
	private SimulationResult simuResult = null;

	private int time;
	private int line;
	private Behaviour behaviour;
	private Step step;
	private CPU cpu;

	private int topoIndex = 0;

	public SimulationEvent(SimulationResult simuResult, int time, int line, Step step, CPU cpu) {
		this.simuResult = simuResult;
		this.time = time;
		this.line = line;
		this.behaviour = (Behaviour)step.eContainer().eContainer();
		this.step = step;
		this.cpu = cpu;
	}

	public SimulationEvent(SimulationResult simuResult, int time, int line, Behaviour behaviour, CPU cpu) {
		this.simuResult = simuResult;
		this.time = time;
		this.line = line;
		this.behaviour = behaviour;
		this.step = null;
		this.cpu = cpu;
	}

	public void setTopoIndex (int idx) {
		this.topoIndex = idx;
	}

	public boolean isStartEvent() {
		return step==null;
	}

	public boolean isLastEvent() {
		if (step==null)
			return false;

		return HbsimHelper.isLastStep(step);
	}

	public boolean isMilestone() {
		return step!=null && step.getName().startsWith("_");
	}

	public int getTime() {
		return time;
	}

	public int getLine() {
		return line;
	}

	public Behaviour getBehaviour() {
		return behaviour;
	}

	public Step getStep() {
		return step;
	}

	public CPU getCpu() {
		return cpu;
	}

	public FeatureConfig getConfig() {
		return simuResult.getConfig();
	}

	public String getName() {
		FunctionBlock fb = (FunctionBlock)behaviour.eContainer();
		if (step==null) {
			return fb.getName() + "::" + behaviour.getName();
		}
		return fb.getName() + "::" + behaviour.getName() + "::" + step.getName();
	}

	public double getUsedCPU (boolean cumulated) {
		return HbsimHelper.getUsedCPU(step, getConfig(), cumulated);
	}

	public boolean isSuccessorOf (SimulationEvent other) {
		return other.hasSuccessor(this);
	}

	public boolean isPredecessorOf (SimulationEvent other) {
		return hasSuccessor(other);

//		List<Step> otherSteps = other.behaviour.getPlan().getStep();
//		if (step==null) {
//			// check if this is a start event and other is first actual step
//			if (other.step!=null && behaviour==other.behaviour) {
//				// this is a start event (i.e., behaviour and optional marker)
//				if (other.step==otherSteps.get(0))
//					return true;
//			}
//		} else {
//			// this is a real step (no start event)
//			if (other.step==null) {
//				// other is a start event
//				if (! otherSteps.isEmpty()) {
//					// inherit predecessors from first real step
//					List<Step> predecessors = simuResult.getModelShell().getPredecessors(otherSteps.get(0));
//					if (predecessors!=null && predecessors.contains(step))
//						return true;
//				}
//				// check if this step is last step and its behaviour triggers other
//				if (HbsimHelper.isLastStep(step)) {
//					List<Behaviour> triggerers = simuResult.getModelShell().getTriggerers(other.behaviour);
//					if (triggerers!=null && triggerers.contains(behaviour))
//						return true;
//				}
//			} else {
//				// other is a real step (no start event)
//				List<Step> predecessors = simuResult.getModelShell().getPredecessors(other.step);
//				if (predecessors!=null && predecessors.contains(step))
//					return true;
//
//				// incoming triggers will be handled by start event
//			}
//		}
//
//		return false;
	}

	public int compare (SimulationEvent other) {
		return simuResult.compare(this, other);
	}

	public int getTopoIndex() {
		return topoIndex;
	}
}

