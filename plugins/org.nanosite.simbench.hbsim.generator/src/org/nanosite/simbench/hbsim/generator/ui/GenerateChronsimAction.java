package org.nanosite.simbench.hbsim.generator.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsoleStream;

import org.nanosite.common.util.file.FileUtils;
import org.nanosite.simbench.hbsim.generator.ui.GeneratorRunner;
import org.nanosite.simbench.hbsim.generator.ui.GenericGenerateAction;

public class GenerateChronsimAction extends GenericGenerateAction {

	public boolean runInternal (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("modelFile", modelFile);

        String srcGenPathRel = "sim/src-gen/ntg5";
        String srcGenPathAbs = projectPath + "/" + srcGenPathRel;
        properties.put("srcGenPathRel", srcGenPathRel);
        properties.put("srcGenPathAbs", srcGenPathAbs);
        properties.put("basePrefix", "base/chronSim/src");
        properties.put("genPrefix", "src-gen/ntg5");

        String mweFile = "workflow/generate_chronsim.mwe";
        boolean ok = new GeneratorRunner(shell, out, err).run(mweFile, properties);
        if (!ok)
        	return false;

    	// copy chronSIM project file (*.ipr)
        String iprFile = "model_gen.ipr";
        String iprTarget = projectPath + "/sim/" + iprFile;
        if (! FileUtils.copy(srcGenPathAbs + "/" + iprFile, iprTarget)) {
        	err.println("Error: cannot copy chronSim project file ('" + iprFile + ")");
        	return false;
        }

        // hook for derived classes
        return runSimulation(shell, projectPath, iprTarget, out, err);
    }

	// to be overriden by derived class
	protected boolean runSimulation(
			Shell shell,
    		String projectPath,
    		String iprTarget,
			MessageConsoleStream out,
    		MessageConsoleStream err) {
		return true;
	}
}
