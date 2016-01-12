package org.nanosite.simbench.simo.generator.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.mwe2.language.Mwe2StandaloneSetup;
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher;
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Runner;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsoleStream;

import com.google.inject.Injector;

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
		Map<String,String> params;

		public MyRunnable (String mweFile, Map<String,String> properties) {
			this.mweFile = mweFile;
			this.params = properties;
		}

		public void run (IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {

//			Mwe2Runner runner = new Mwe2Runner();
//			runner.run("workflow.generate_warp", properties);
			
			Injector injector = new Mwe2StandaloneSetup().createInjectorAndDoEMFRegistration();
			Mwe2Runner runner = injector.getInstance(Mwe2Runner.class);
			String classpath = System.getProperty("java.class.path") ;
			System.out.println("classpath: " + classpath);

			out.println("Running workflow " + mweFile + " ...");
			out.println("Output directory: " + params.get("srcGenPathAbs"));
//			final Issues issues = new IssuesImpl();
//	        final Map<String,String> slotContents = new HashMap<String,String>();
			try {
				if (mweFile.contains("/")) {
					runner.run(URI.createURI(mweFile), params);
				} else {
					runner.run(mweFile, params);
				}
				ok = true;
			} catch (NoClassDefFoundError e) {
				if ("org/eclipse/core/runtime/OperationCanceledException".equals(e.getMessage())){
					System.err.println("Could not load class: org.eclipse.core.runtime.OperationCanceledException");
					System.err.println("Add org.eclipse.equinox.common to the class path.");
				} else {
					throw e;
				}
			}
//			for(int i=0; i<issues.getInfos().length; i++) {
//				out.println("Info: " + issues.getInfos()[i].toString());
//			}
//			for(int i=0; i<issues.getIssues().length; i++) {
//				out.println("Issue: " + issues.getIssues()[i].toString());
//			}
//			out.println("Workflow ready - " +
//					issues.getErrors().length + " errors, " +
//					issues.getWarnings().length + " warnings.");
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
