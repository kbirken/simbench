package org.nanosite.simbench.injection.analyser.simuresult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import org.nanosite.simbench.HbsimHelper;
import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.CPU;
import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.FunctionBlock;
import org.nanosite.simbench.hbsim.Mapping;
import org.nanosite.simbench.hbsim.Partitioning;
import org.nanosite.simbench.hbsim.Plan;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.hbsim.TriggerCall;
import org.nanosite.simbench.hbsim.VP_TriggerCall;
import org.nanosite.simbench.hbsim.helpers.SimModelDescriptor;
import org.nanosite.simbench.hbsim.helpers.SimModelShell;
import org.nanosite.simbench.injection.analyser.toposort.TopoSorter;
import org.nanosite.simbench.injection.analyser.tracereader.ITraceReaderClient;
import org.nanosite.simbench.injection.analyser.tracereader.WarpTraceReader;

public class SimulationResult implements ITraceReaderClient {

	SimModelDescriptor modelDescriptor;
	String traceFilename;

	List<SimulationEvent> events = new ArrayList<SimulationEvent>();

	// some precomputed mappings
	SimModelShell modelShell = null;
	Map<String,CPU> fb2cpu = new HashMap<String,CPU>();
	Map<String,Behaviour> behaviour2behaviour = new HashMap<String,Behaviour>();
	Map<String,Step> step2step = new HashMap<String,Step>();


	public SimulationResult (
			SimModelDescriptor modelDescriptor,
			String traceFilename)
	{
		this.modelDescriptor = modelDescriptor;
		this.traceFilename = traceFilename;
	}

	public FeatureConfig getConfig() {
		return modelDescriptor.getConfig();
	}

	public String getTracefile() {
		return traceFilename;
	}

	public List<SimulationEvent> getEvents() {
		return events;
	}

	public SimModelShell getModelShell() {
		return modelShell;
	}


	public boolean readWarpTrace (IProgressMonitor monitor) {
		events.clear();

		if (! modelDescriptor.isValid()) {
			System.err.println("SimulationResult error: invalid model descriptor.");
			return false;
		}
		prepareMappings();
		WarpTraceReader reader = new WarpTraceReader(this);
		int lines = reader.readTrace(traceFilename, monitor);
		if (lines>0) {
			System.out.println("WarpTraceReader was reading tracefile with " + lines + " lines.");
		} else {
			System.err.println("WarpTraceReader returned " + lines + ", this indicates an error or abort during read.");
			return false;
		}

		computeTopoOrder();
		return true;
	}

	public int compare (SimulationEvent ev1, SimulationEvent ev2) {
		int comp = Integer.valueOf(ev1.getTime()).compareTo(ev2.getTime());
		if (comp!=0)
			return comp;

		// times are equal, sort according TopoIndex
		return Integer.valueOf(ev1.getTopoIndex()).compareTo(ev2.getTopoIndex());
	}

	// *****************************************************************

	private void prepareMappings() {
		modelShell = new SimModelShell(modelDescriptor);

		fb2cpu.clear();
		step2step.clear();
		FeatureConfig config = modelDescriptor.getConfig();
		Partitioning partitioning = modelDescriptor.getPartitioning();
		if (config!=null && partitioning!=null) {
			List<Mapping> mappings = HbsimHelper.getAllMappings(partitioning, config);
			for(Mapping map : mappings) {
				FunctionBlock fb = map.getFb();
				fb2cpu.put(fb.getName(), map.getCpu());

				for(Behaviour bhvr : fb.getBehaviour()) {
					behaviour2behaviour.put(key(fb.getName(), bhvr.getName(), ""), bhvr);
					for(Step step : bhvr.getPlan().getStep()) {
						step2step.put(key(fb.getName(), bhvr.getName(), step.getName()), step);
					}
				}
			}
		}
	}

	@Override
	public void processStartEvent (int line, int time, String fb, String behaviour) {
		addEvent(line, time, fb, behaviour, "");
	}

	@Override
	public void processEventDone (int line, int time, String fb, String behaviour, String step) {
		addEvent(line, time, fb, behaviour, step);
	}

	private void addEvent (int line, int time, String fb, String behaviour, String step) {
		CPU cpu = fb2cpu.get(fb);
		String key = key(fb, behaviour, step);
		Step stepObj = step2step.get(key);
		if (stepObj!=null) {
			SimulationEvent ev = new SimulationEvent(this, time, line, stepObj, cpu);
			events.add(ev);
		} else {
			Behaviour behaviourObj = behaviour2behaviour.get(key);
			if (behaviourObj==null) {
				System.err.println("SimulationResult: cannot find behaviour object for key " + key + " (line " + line + ", time " + time + ")!");
			} else {
				SimulationEvent ev = new SimulationEvent(this, time, line, behaviourObj, cpu);
				events.add(ev);
			}
		}
	}

	private String key (String fb, String behaviour, String step) {
		if (step.isEmpty()) {
			return fb + "::" + behaviour;
		}
		return fb + "::" + behaviour + "::" + step;
	}

	// mappings needed for TopoSort
	private Map<Behaviour,SimulationEvent> behaviour2event = new HashMap<Behaviour,SimulationEvent>();
	private Map<Step,SimulationEvent> step2event = new HashMap<Step,SimulationEvent>();

	private void computeTopoOrder () {
		// compute some mappings
		for(SimulationEvent ev : events) {
			if (ev.getStep()==null) {
				// start event
				if (ev.getBehaviour()==null) {
					System.err.println("SimulationResult: invalid event (neither step nor behaviour) " +
							ev.toString() + " (line " + ev.getLine() + ")!");
				}
				behaviour2event.put(ev.getBehaviour(), ev);
			} else {
				// normal step
				step2event.put(ev.getStep(), ev);
			}
		}

		// add successor relation
		for(SimulationEvent ev : events) {
			if (ev.getStep()==null) {
				// start event
				if (ev.getBehaviour()==null) {
					System.err.println("SimulationResult: event " +
							ev.getName() + " (line " + ev.getLine() + ") has invalid step or behaviour!");
				} else {
					Behaviour bhvr = ev.getBehaviour();
					if (bhvr.getPlan()==null) {
						System.err.println("SimulationResult: event " +
								ev.getName() + " (line " + ev.getLine() + ") has behaviour without plan!");
					} else {
						Plan plan = bhvr.getPlan();
						if (plan.getStep().size()>0) {
							Step firstStep = plan.getStep().get(0);
							SimulationEvent sev = step2event.get(firstStep);
							if (sev==null) {
								System.err.println("SimulationResult: invalid simulation event for firstStep=" + firstStep.getName() + " in behaviour " + bhvr.getName());
							} else {
								ev.addSuccessor(sev);
							}
						}
					}
				}
			} else {
				// normal step
				Step step = ev.getStep();
				if (HbsimHelper.isLastStep(step)) {
					for(VP_TriggerCall vp : ev.getBehaviour().getSend()) {
						TriggerCall tc = HbsimHelper.bind(vp, getConfig());
						if (tc!=null) {
							SimulationEvent sev = behaviour2event.get(tc.getTrigger());
							if (sev==null) {
								System.err.println("SimulationResult: invalid simulation event for triggerCall=" + tc.toString());
							} else {
								ev.addSuccessor(sev);
							}
						}
					}
				}
				List<Step> predecessors = modelShell.getPredecessors(step);
				if (predecessors!=null) {
					for(Step pred : predecessors) {
						SimulationEvent sev = step2event.get(pred);
						if (sev==null) {
							System.err.println("SimulationResult: missing SimulationEvent for step " + pred.getName());
						} else {
							if (ev==null) {
								System.err.println("SimulationResult: invalid event for simulation event " + sev.getName() + " (line " + sev.getLine() + ")");
							} else {
								sev.addSuccessor(ev);
							}
						}
					}
				}
			}
		}

		TopoSorter sorter = new TopoSorter();
		List<SimulationEvent> sorted = sorter.sort(events);
		int idx = 1;
		for(SimulationEvent ev : sorted) {
			ev.setTopoIndex(idx);
			idx++;
		}
	}
}
