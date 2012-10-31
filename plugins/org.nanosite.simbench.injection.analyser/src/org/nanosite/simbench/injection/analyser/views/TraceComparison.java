package org.nanosite.simbench.injection.analyser.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.nanosite.simbench.HbsimHelper;
import org.nanosite.simbench.hbsim.Behaviour;
import org.nanosite.simbench.hbsim.CPU;
import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.FunctionBlock;
import org.nanosite.simbench.hbsim.Mapping;
import org.nanosite.simbench.hbsim.MarkerSet;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.hbsim.helpers.SimModelDescriptor;
import org.nanosite.simbench.injection.analyser.simuresult.SimulationEvent;
import org.nanosite.simbench.injection.analyser.simuresult.SimulationResult;
import org.nanosite.simbench.injection.hbinj.InjBehaviour;
import org.nanosite.simbench.injection.hbinj.InjContext;
import org.nanosite.simbench.injection.hbinj.InjFunctionBlock;
import org.nanosite.simbench.injection.hbinj.InjMarker;
import org.nanosite.simbench.injection.hbinj.InjModel;
import org.nanosite.simbench.injection.hbinj.InjOffset;
import org.nanosite.simbench.injection.hbinj.InjStep;

public class TraceComparison {

    public class TraceEvent {
    	public static final int NO_TIME_REAL = 0;
    	public static final int NO_TIME_SIMU = -1;

        private int time;
        private String name;

        // if a step has no markers, we will have step!=null and marker==null
        private InjStep step = null;
        private InjMarker marker = null;

        SimulationEvent simEvent = null;

        // constructors for events which exist in reality (and optionally in simulation)
        public TraceEvent (int time, String name, InjMarker marker, InjStep step) {
            this.time = time;
            this.name = name;
            this.step = step;
            this.marker = marker;
            this.simEvent = null;
        }
        public TraceEvent (int time, String name, InjStep step) {
            this.time = time;
            this.name = name;
            this.step = step;
            this.marker = null;
            this.simEvent = null;
        }

        // constructor for events which exist only in simulation
        public TraceEvent (SimulationEvent simEvent) {
            this.time = 0;
            this.name = "";
            this.step = null;
            this.marker = null;
            this.simEvent = simEvent;
        }

        public void setSimEvent (SimulationEvent simEvent) {
            this.simEvent = simEvent;
        }

        public int getTimeReal() {
            return time;
        }

        public int getTimeSimu() {
            return simEvent!=null ? simEvent.getTime() : NO_TIME_SIMU;
        }

        public String getName() {
            return simEvent!=null ? simEvent.getName() : name;
        }

        public InjMarker getMarker() {
            return marker;
        }

        public InjStep getInjStep() {
            return step;
        }

        public String getMarkerString() {
            if (marker==null)
                    return "";

            // compute marker name
            String mn = marker.getType().getName() + "(";
            boolean isFirst = true;
            for(String p : marker.getParam()) {
                if (!isFirst) mn += " ";
                mn += '"' + p + '"';
                isFirst=false;
            }
            mn += ")";
            return mn;
        }

        public String getCPU() {
            return simEvent!=null ? simEvent.getCpu().getName() : "";
        }

        public FunctionBlock getFunctionBlock() {
            Behaviour behaviour = getBehaviour();
            if (behaviour!=null) {
                return (FunctionBlock)behaviour.eContainer();
            }
            return null;
        }

        public Behaviour getBehaviour() {
            if (simEvent!=null) {
                return simEvent.getBehaviour();
            }
            if (step!=null) {
                InjBehaviour bhvr = (InjBehaviour)step.eContainer();
                return bhvr.getBehaviour();
            }
            return null;
        }

        public Step getStep() {
            if (simEvent!=null) {
                return simEvent.getStep();
            }
            if (step!=null) {
                return step.getStep();
            }
            return null;
        }

        public FeatureConfig getConfig() {
            return simEvent!=null ? simEvent.getConfig() : null;
        }

        public double getUsedCPU (boolean cumulated) {
            if (step!=null) {
                double used = 0.0;
                if (cumulated) {
                    InjBehaviour bhvr = (InjBehaviour)step.eContainer();
                    Iterator<InjStep> si = bhvr.getStep().iterator();
                    InjStep s;
                    do {
                        s = si.next();
                        if (s.getAction()!=null && s.getAction().getConsumedCPU()!=null) {
                            used += Double.parseDouble(s.getAction().getConsumedCPU());
                        }
                    } while(si.hasNext() && s!=step);
                } else {
                    if (step.getAction()!=null && step.getAction().getConsumedCPU()!=null) {
                        used = Double.parseDouble(step.getAction().getConsumedCPU());
                    }
                }
                return used;
            }
            return -1; // indicates N/A entry;
        }

        public double getUsedCPUSimu (boolean cumulated) {
            if (simEvent==null) {
                return -1; // indicates N/A entry
            }
            return simEvent.getUsedCPU(cumulated);
        }

        public boolean isStartEvent() {
            if (simEvent!=null)
                return simEvent.isStartEvent();
            return false; // cannot tell
        }

        public boolean isLastEvent() {
            if (simEvent!=null)
                return simEvent.isLastEvent();
            return false; // cannot tell
        }

        public boolean isMilestone() {
            return simEvent!=null ? simEvent.isMilestone() : false;
        }

        public boolean isSuccessorOf (TraceEvent other) {
            if (other==null)
                return false;

            if (simEvent==null || other.simEvent==null)
                return false;

            return simEvent.isSuccessorOf(other.simEvent);
        }

        public boolean isPredecessorOf (TraceEvent other) {
            if (other==null)
                return false;

            if (simEvent==null || other.simEvent==null)
                return false;

            return simEvent.isPredecessorOf(other.simEvent);
        }

        public int compare (TraceEvent other) {
            if (simEvent==null || other.simEvent==null)
                return 0;

            return simEvent.compare(other.simEvent);
        }
    };

    InjModel model = null;

    // as reading simu traces takes a long time, we cache the results here
    Map<SimModelDescriptor,SimulationResult> simResults = new HashMap<SimModelDescriptor,SimulationResult>();

    List<TraceEvent> events = new ArrayList<TraceEvent>();
    Map<String,TraceEvent> name2event = new HashMap<String,TraceEvent>();

    public TraceComparison () {
    }

    public void readModel (InjModel model) {
        this.model = model;

        readInjModel(model);

        InjContext ctxt = model.getContext();
        final String tracefile = ctxt.getTracefile();
        if (tracefile!=null) {
            SimModelDescriptor simu = new SimModelDescriptor(ctxt.getPartitioning(), ctxt.getScenario(), ctxt.getConfig());
            if (simu.isValid()) {
                SimulationResult simRes = null;
                if (simResults.containsKey(simu)) {
                    simRes = simResults.get(simu);
                    if (! simRes.getTracefile().equals(tracefile)) {
                        // TODO just checking tracefile is not enough ... we should also check timestamp of file on FS
                        simResults.remove(simu);
                        simRes = null;
                    }
                }

                if (simRes==null) {
        	        ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        	        try {
        	        	TraceReadRunner runner = new TraceReadRunner(simu, tracefile);
        	        	dialog.run(true, false, runner);
        	        	simRes = runner.simRes;
                        simResults.put(simu, simRes);
        	        } catch (InvocationTargetException e) {
        				e.printStackTrace();
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
                }

                integrateSimResult(simRes);
                computeStatistics();
            }
        }
    }


    // force reload of traces on next update
    public void forceReload() {
    	simResults.clear();
    }


    private class TraceReadRunner implements IRunnableWithProgress {
    	SimulationResult simRes = null;
    	SimModelDescriptor simu;
    	String tracefile;

    	public TraceReadRunner (SimModelDescriptor simu, String tracefile) {
    		this.simu = simu;
    		this.tracefile = tracefile;
    	}

		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException
		{
            System.out.println("TraceComparison: reading warp tracefile");
            simRes = new SimulationResult(simu, tracefile);
            simRes.readWarpTrace(monitor);
		}
    }


    public InjModel getModel () {
        return model;
    }

    public List<TraceEvent> getEvents() {
        return events;
    }

    private void readInjModel (InjModel model) {
        events.clear();
        name2event.clear();

        List<InjMarker> results = new ArrayList<InjMarker>();

        // we need a mapping from FB to offset on this CPU
        Map<FunctionBlock,Integer> offsets = new HashMap<FunctionBlock,Integer>();
        InjContext ctxt = model.getContext();
        if (ctxt.getPartitioning()!=null && ctxt.getOffsets()!=null && ctxt.getConfig()!=null) {
            Map<CPU,Integer> offsetDefs = new HashMap<CPU,Integer>();
            for(InjOffset offset : model.getContext().getOffsets()) {
                offsetDefs.put(offset.getCpu(), offset.getOffset().intValue());
            }
            if (offsetDefs.size()>0) {
                for(Mapping m : HbsimHelper.getAllMappings(ctxt.getPartitioning(), ctxt.getConfig())) {
                    offsets.put(m.getFb(), offsetDefs.get(m.getCpu()));
                }
            }
        }

        for(InjFunctionBlock fb : model.getFbs()) {
            int offset = offsets.containsKey(fb.getFb()) ? offsets.get(fb.getFb()) : 0;
            for(InjBehaviour bhvr : fb.getBehaviour()) {
                // this is a list of start markers (before first step)
                for(InjMarker m : bhvr.getMarkers()) {
                    TraceEvent ev = new TraceEvent(
                            Integer.valueOf(m.getTime()) + offset,
                            fb.getFb().getName() + "::" + bhvr.getBehaviour().getName(),
                            m, null);
                    addEvent(ev);
                }
                results.addAll(bhvr.getMarkers());
                for(InjStep step : bhvr.getStep()) {
                    if (step.getMarkers().isEmpty()) {
                        // we insert an marker-less step here
                        TraceEvent ev = new TraceEvent(
                                0,
                                fb.getFb().getName() + "::" + bhvr.getBehaviour().getName() + "::" + step.getStep().getName(),
                                step);
                        addEvent(ev);
                    } else {
                        // insert markers for this step (may be many)
                        for(InjMarker m : step.getMarkers()) {
                            TraceEvent ev = new TraceEvent(
                                    Integer.valueOf(m.getTime()) + offset,
                                    fb.getFb().getName() + "::" + bhvr.getBehaviour().getName() + "::" + step.getStep().getName(),
                                    m, step);
                            addEvent(ev);
                        }
                    }
                }
            }
        }
    }


    public void computeStatistics() {
    	int nNoTimeReal = 0;
    	int nNoTimeSimu = 0;

    	int absDeltaTimes = 0;
    	int nAbsDeltaTimes = 0;
    	int posDeltaTimes = 0;
    	int nPosDeltaTimes = 0;
    	int negDeltaTimes = 0;
    	int nNegDeltaTimes = 0;

    	int nNoMarkers = 0;
    	int nMissedMarkers = 0;
    	for(TraceEvent ev : events) {
    		if (ev.getTimeReal()==TraceEvent.NO_TIME_REAL) {
    			nNoTimeReal++;
    		} else {
    			if (ev.getTimeSimu()==TraceEvent.NO_TIME_SIMU) {
    				nNoTimeSimu++;
    			} else {
    				// both times available, compute delta
    				int delta = ev.getTimeSimu()-ev.getTimeReal();
    				absDeltaTimes += Math.abs(delta);
    				nAbsDeltaTimes++;

    				if (delta>0) {
    					posDeltaTimes += delta;
    					nPosDeltaTimes++;
    				} else if (delta<0) {
    					negDeltaTimes -= delta;
    					nNegDeltaTimes++;
    				}
    			}
    		}
			if (ev.getMarker()==null && ev.getStep()!=null) {
				MarkerSet ms = ev.getStep().getMarker_set();
				if (ms!=null && ms.getMarker().size()>0)
				{
					// no marker in hbinj file found, but at least one marker has been modeled
					nMissedMarkers++;
				} else {
					nNoMarkers++;
				}
			}
    	}

    	System.out.println("absDeltaTimes    = " + msec(absDeltaTimes) + " (" + nAbsDeltaTimes + " events)");
    	if (nAbsDeltaTimes>0)
    		System.out.println("averageDeltaTime = " + msec(absDeltaTimes/nAbsDeltaTimes));
    	System.out.println("posDeltaTimes    = " + msec(posDeltaTimes) + " (" + nPosDeltaTimes + " events)");
    	if (nPosDeltaTimes>0)
    		System.out.println("averagePosDeltaT = " + msec(posDeltaTimes/nPosDeltaTimes));
    	System.out.println("negDeltaTimes    = " + msec(negDeltaTimes) + " (" + nNegDeltaTimes + " events)");
    	if (nAbsDeltaTimes>0)
    		System.out.println("averageNegDeltaT = " + msec(negDeltaTimes/nNegDeltaTimes));

    	System.out.println("nNoMarkers       = " + nNoMarkers);
    	System.out.println("nMissedMarkers   = " + nMissedMarkers);
    	System.out.println("nNoTimeReal      = " + nNoTimeReal);
    	System.out.println("nNoTimeSimu      = " + nNoTimeSimu);
    	System.out.println("---");
    }

    private String msec (int t) {
    	return String.format("%7d", t) + " millisec";
    }

    private void integrateSimResult (SimulationResult simResult) {
        for(SimulationEvent ev : simResult.getEvents()) {
            String key = ev.getName();
            if (name2event.containsKey(key)) {
                // already exists, integrate
                name2event.get(key).setSimEvent(ev);
            } else {
                addEvent(new TraceEvent(ev));
            }
        }
    }


    private void addEvent (TraceEvent ev) {
        events.add(ev);

        // we block all subsequent Events with same name
        // (in case of multiple markers for one step)
        if (name2event.containsKey(ev.getName()))
            return;

        name2event.put(ev.getName(), ev);
    }

}
