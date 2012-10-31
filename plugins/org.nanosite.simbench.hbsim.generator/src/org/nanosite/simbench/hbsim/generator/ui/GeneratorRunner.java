package org.nanosite.simbench.hbsim.generator.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.mwe.core.WorkflowEngine;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitorAdapter;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsoleStream;

public class GeneratorRunner {

	private Shell shell = null;
	private MessageConsoleStream out = null;
	private MessageConsoleStream err = null;

	public GeneratorRunner (Shell shell, MessageConsoleStream out, MessageConsoleStream err) {
		this.shell = shell;
		this.out = out;
		this.err = err;
	}


	class MyRunnable implements IRunnableWithProgress {
		String mweFile;
		boolean ok = false;
		Map<String,String> properties;

		public MyRunnable (String mweFile, Map<String,String> properties) {
			this.mweFile = mweFile;
			this.properties = properties;
		}

		public void run (IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {

//			Mwe2Runner runner = new Mwe2Runner();
//			runner.run("workflow.generate_warp", properties);
			
			WorkflowEngine runner = new WorkflowEngine();
			boolean configOK = runner.prepare(mweFile, monitor==null ? null : new ProgressMonitorAdapter(monitor), properties);
			if (!configOK) {
				err.println("Error: Couldn't prepare workflow!");
				ok = false;
				return;
			}

			out.println("Running workflow " + mweFile + " ...");
			out.println("Output directory: " + properties.get("srcGenPathAbs"));
			final Issues issues = new IssuesImpl();
	        final Map<String,String> slotContents = new HashMap<String,String>();
			ok = runner.executeWorkflow(slotContents, issues);
			for(int i=0; i<issues.getInfos().length; i++) {
				out.println("Info: " + issues.getInfos()[i].toString());
			}
			for(int i=0; i<issues.getIssues().length; i++) {
				out.println("Issue: " + issues.getIssues()[i].toString());
			}
			out.println("Workflow ready - " +
					issues.getErrors().length + " errors, " +
					issues.getWarnings().length + " warnings.");
		}
	}


	public boolean run (final String mweFile, final Map<String,String> properties) {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
    	MyRunnable runnable = new MyRunnable(mweFile, properties);
        try {
        	dialog.run(true, true, runnable);
        } catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return runnable.ok;
	}
}
