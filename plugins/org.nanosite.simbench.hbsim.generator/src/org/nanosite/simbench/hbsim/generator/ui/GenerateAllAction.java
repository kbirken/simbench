package org.nanosite.simbench.hbsim.generator.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.console.MessageConsoleStream;

import org.nanosite.common.util.graphviz.GraphvizWrapper;

public class GenerateAllAction extends GenericGenerateAction {

    public boolean runInternal (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
    	if (! genVisuals(modelFile, projectPath, out, err)) {
    		return false;
    	}

    	if (! genReports(modelFile, projectPath, out, err)) {
    		return false;
    	}

    	out.println("All generators ran properly.");
    	return true;
    }


    private boolean genVisuals (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("modelFile", modelFile);

        String srcGenPathRel = "src-gen/dot";
        properties.put("srcGenPathRel", srcGenPathRel);
        properties.put("srcGenPathAbs", projectPath + "/" + srcGenPathRel);

        // specials for dot generation
        properties.put("withScenario", "true");
        properties.put("compact", "true");

        final String mweFile = "workflow/generate_dot.mwe";
        boolean ok = new GeneratorRunner(shell, out, err).run(mweFile, properties);

        if (ok) {
        	String gif = GraphvizWrapper.INSTANCE().dot(
        			null,
        			GraphvizWrapper.FORMAT_GIF,
        			projectPath + "/" + srcGenPathRel + "/fb_dot.txt",
        			projectPath + "/src-gen/visuals",
        			"dep_graph_fb.gif",
        			err);
        	if (gif!=null) {
				out.println("Created dependency graph " + gif + ".");
        	} else {
        		ok = false;
        	}
        }

        return ok;
    }

	private boolean genReports (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("modelFile", modelFile);

        String srcGenPathRel = "src-gen/reports";
        properties.put("srcGenPathRel", srcGenPathRel);
        properties.put("srcGenPathAbs", projectPath + "/" + srcGenPathRel);

        final String mweFile = "workflow/generate_reports.mwe";
        boolean ok = new GeneratorRunner(shell, out, err).run(mweFile, properties);

        return ok;
    }
}
