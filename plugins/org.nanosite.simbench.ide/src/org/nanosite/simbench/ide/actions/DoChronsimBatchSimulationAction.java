package org.nanosite.simbench.ide.actions;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsoleStream;

import org.nanosite.simbench.hbsim.generator.ui.GenerateChronsimAction;
import org.nanosite.simbench.backend.chronsim.ChronsimRunner;

public class DoChronsimBatchSimulationAction extends GenerateChronsimAction {

	public boolean runSimulation (
			Shell shell,
    		String projectPath,
    		String iprTarget,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
    	ChronsimRunner chronsimRunner = new ChronsimRunner(out, err);
        String tracesDir = projectPath + "/sim/traces";
        Job job = chronsimRunner.run(iprTarget, 30, tracesDir);

        // do some asynchronous post-processing here based on code like:
//        job.addJobChangeListener(new JobChangeAdapter() {
//            public void done(IJobChangeEvent event) {
//            if (event.getResult().isOK())
//               postMessage("Job completed successfully");
//               else
//                  postError("Job did not complete successfully");
//            }
//         });


//        if (job!=null) {
//        	// call graphviz to draw simulation result graph
//        	String pdf = GraphvizWrapper.INSTANCE().dot(
//        			shell,
//        			GraphvizWrapper.FORMAT_PDF,
//        			warpRunner.getSimuDotFile(),
//        			tracesDir,
//        			"simu_result.pdf",
//        			err);
//        	if (pdf!=null) {
//				out.println("Created simulation result graph " + pdf + ".");
//        	} else {
//        		ok = false;
//        	}
//        }
//    }

        return job!=null;
    }
}
