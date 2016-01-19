package org.nanosite.simbench.ide.actions;

import org.eclipse.ui.console.MessageConsoleStream;
import org.nanosite.common.util.graphviz.GraphvizWrapper;
import org.nanosite.simbench.backend.warp.WarpRunner;
import org.nanosite.simbench.simo.generator.ui.GenericGenerateAction;
import org.nanosite.simbench.simo.generator.warp.WarpGenerator;

public class DoWarpSimulationAction extends GenericGenerateAction {

	public boolean runInternal (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
        String relativeGenPath = WarpGenerator.getRelativeGenPath(modelFile.substring(1));

        WarpRunner warpRunner = new WarpRunner(out, err);
        
        String warpFile = projectPath + "/src-gen/" + relativeGenPath;
        String tracesDir = projectPath + "/sim/traces";
        boolean ok = warpRunner.run(warpFile, tracesDir);
        if (ok) {
        	// call graphviz to draw simulation result graph
        	String pdf = GraphvizWrapper.INSTANCE().dot(
        			shell,
        			GraphvizWrapper.FORMAT_PDF,
        			warpRunner.getSimuDotFile(),
        			tracesDir,
        			"simu_result.pdf",
        			err);
        	if (pdf!=null) {
				out.println("Created simulation result graph " + pdf + ".");
        	} else {
        		ok = false;
        	}
        }

        return ok;
    }

}
