package org.nanosite.feamo.hbfm.generator.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.MessageConsoleStream;

public class GenerateFeatureDiagram implements IObjectActionDelegate {

	protected Shell shell;
    private IStructuredSelection selection = null;

    /**
     * Constructor for GenerateFeatureDiagram.
     */
    public GenerateFeatureDiagram() {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
    	FeamoConsole myConsole = new FeamoConsole(shell);
        final MessageConsoleStream out = myConsole.getOut();
        final MessageConsoleStream err = myConsole.getErr();

        if (selection!=null) {
            if (selection.size()!=1) {
            	err.println("Please select exactly one hbfm file!");
                return;
            }

            IFile file = (IFile)selection.getFirstElement();
            String modelFile = file.getFullPath().toString();
            IProject project = file.getProject();
            String projectPath = project.getLocation().toPortableString();

            try {
    			project.refreshLocal(IResource.DEPTH_INFINITE, null);;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			genVisuals(modelFile, projectPath, out, err);
        }
    }


    private boolean genVisuals (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("modelFile", modelFile);

        String srcGenPathRel = "src-gen/feamo";
        properties.put("srcGenPathRel", srcGenPathRel);
        properties.put("srcGenPathAbs", projectPath + "/" + srcGenPathRel);

        // specials for feature graph generation
        properties.put("rootConcept", "MyRootConcept");

        final String mweFile = "workflow/generate_feature_diagram.mwe";
        boolean ok = new GeneratorRunner(shell, out, err).run(mweFile, properties);

//        if (ok) {
//        	String gif = GraphvizWrapper.INSTANCE().dot(
//        			null,
//        			GraphvizWrapper.FORMAT_GIF,
//        			projectPath + "/" + srcGenPathRel + "/feature_diagram.txt",
//        			projectPath + "/src-gen/visuals",
//        			"feature_diagram.gif",
//        			err);
//        	if (gif!=null) {
//				out.println("Created dependency graph " + gif + ".");
//        	} else {
//        		ok = false;
//        	}
//        }

        return ok;
    }



    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = (IStructuredSelection)selection;
    }


}
