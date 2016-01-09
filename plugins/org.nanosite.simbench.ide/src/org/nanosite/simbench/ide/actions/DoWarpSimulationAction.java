package org.nanosite.simbench.ide.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.console.MessageConsoleStream;
import org.nanosite.common.util.graphviz.GraphvizWrapper;
import org.nanosite.simbench.backend.warp.WarpRunner;
import org.nanosite.simbench.simo.generator.ui.GeneratorRunner;
import org.nanosite.simbench.simo.generator.ui.GenericGenerateAction;

public class DoWarpSimulationAction extends GenericGenerateAction {

	public boolean runInternal (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("modelFile", "platform:/resource" + modelFile);

        String srcGenPathRel = "src-gen/warp";
        properties.put("srcGenPathRel", srcGenPathRel);
        properties.put("srcGenPathAbs", projectPath + "/" + srcGenPathRel);

        String mweFile = "workflow/generate_warp.mwe2";
        boolean ok = new GeneratorRunner(shell, out, err).run(mweFile, properties);

        if (ok) {
            WarpRunner warpRunner = new WarpRunner(out, err);
            String warpFile = projectPath + "/" + srcGenPathRel + "/example_warp.txt";
            String tracesDir = projectPath + "/sim/traces";
            ok = warpRunner.run(warpFile, tracesDir);

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
        }

        return ok;
    }

}
