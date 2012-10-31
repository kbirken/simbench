package org.nanosite.simbench.hbsim.generator.ui;

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

public class GenericGenerateAction implements IObjectActionDelegate {

    protected Shell shell;
    private IStructuredSelection selection = null;

    /**
     * Constructor for GenericGenerateAction.
     */
    public GenericGenerateAction() {
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
    	SimbenchConsole myConsole = new SimbenchConsole(shell);
        final MessageConsoleStream out = myConsole.getOut();
        final MessageConsoleStream err = myConsole.getErr();

        if (selection!=null) {
            if (selection.size()!=1) {
            	err.println("Please select exactly one hbsim file!");
                return;
            }

            IFile file = (IFile)selection.getFirstElement();
            String modelFile = file.getFullPath().toString();
            IProject project = file.getProject();
            String projectPath = project.getLocation().toPortableString();

            /*boolean ok =*/ runInternal(modelFile, projectPath, out, err);

            try {
    			project.refreshLocal(IResource.DEPTH_INFINITE, null);;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    public boolean runInternal (
    		String modelFile,
    		String projectPath,
    		MessageConsoleStream out,
    		MessageConsoleStream err)
    {
    	return false;
    }


    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = (IStructuredSelection)selection;
    }


//  System.out.println("Log 1 = " + System.getProperty("org.apache.commons.logging.Log"));
//  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
//  System.out.println("Log 2 = " + System.getProperty("org.apache.commons.logging.Log"));
//
//  System.out.println("LogFactory.DIAGNOSTICS_DEST_PROPERTY  = " + LogFactory.DIAGNOSTICS_DEST_PROPERTY );
//  System.setProperty(LogFactory.DIAGNOSTICS_DEST_PROPERTY , "STDOUT");
//  System.out.println("  value = " + System.getProperty(LogFactory.DIAGNOSTICS_DEST_PROPERTY ));
//
//  System.out.println("LogFactory.FACTORY_DEFAULT            = " + LogFactory.FACTORY_DEFAULT);
//  System.out.println("  value = " + System.getProperty(LogFactory.FACTORY_DEFAULT));
//  System.out.println("LogFactory.FACTORY_PROPERTIES         = " + LogFactory.FACTORY_PROPERTIES);
//  System.out.println("LogFactory.FACTORY_PROPERTY           = " + LogFactory.FACTORY_PROPERTY);
//  System.out.println("  value = " + System.getProperty(LogFactory.FACTORY_PROPERTY ));


//  LogFactory fac = LogFactory.getFactory();
//  LogFactoryImpl ifac = (LogFactoryImpl)fac;
//  String[] attribNames = ifac.getAttributeNames();
//  System.out.println("Attributes " + attribNames.length);
//  for(String an : attribNames) {
//  	System.out.println("Attrib " + an + " = " + fac.getAttribute(an));
//  }
//  System.out.println("LogFactoryImpl.LOG_PROPERTY              = " + LogFactoryImpl.LOG_PROPERTY );
//  System.out.println("  value = " + System.getProperty(LogFactoryImpl.LOG_PROPERTY));

}
