package org.nanosite.common.util.graphviz;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsoleStream;


public class GraphvizWrapper {

	// the preferences key
	public static String ID_GRAPHVIZ_PATH = "org.nanosite.simbench.ide.graphviz_path";

	// output formats
	public final static int FORMAT_GIF = 1;
	public final static int FORMAT_PDF = 2;

	// this is a singleton
	static private GraphvizWrapper instance = null;
	static public GraphvizWrapper INSTANCE() {
		if (instance==null) {
			instance = new GraphvizWrapper();
		}
		return instance;
	}


	public String dot (
			Shell shell,
			int outputFormat,
			String dotFile,
			String targetDir,
			String targetFile,
    		MessageConsoleStream err)
	{
		String dotPath = getDotPath(err);
		if (dotPath==null)
			return null;

		String outfile = targetDir + "/" + targetFile;
		File dir = new File(targetDir);
		if (dir.exists() || dir.mkdirs()) {
			if (! run(shell, dotPath, outputFormat, outfile, dotFile)) {
				err.println("Error: Couldn't start graphviz process!");
				return null;
			}
		} else {
			err.println("Error: Couldn't create graphics output directory " + targetDir + "!");
			return null;
		}

		return outfile;
	}


	// ensure dot.exe tool is available
	private static String getDotPath (MessageConsoleStream err) {
		IPreferencesService service = Platform.getPreferencesService();
		String graphvizDir = service.getString("org.nanosite.simbench.ide", ID_GRAPHVIZ_PATH, "", null);
		String dotPath = graphvizDir + "/dot.exe";

		File dotFile = new File(dotPath);
		if (! dotFile.exists()) {
			err.println("Error: dot.exe is not available (configured path: '" + dotPath + "').");
			return null;
		}
		return dotPath;
	}


	class MyRunnable implements IRunnableWithProgress {
		boolean ok = false;

		String dotPath;
		int outputFormat;
		String outfile;
		String dotFile;

		public MyRunnable (String dotPath, int outputFormat, String outfile, String dotFile) {
			this.dotPath = dotPath;
			this.outputFormat = outputFormat;
			this.outfile = outfile;
			this.dotFile = dotFile;
		}

		public void run (IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException
		{
        	Process p = null;
        	if (monitor!=null)
        		monitor.beginTask("Starting dot.exe", 100);
			try {
				String fmt = "gif";
				if (outputFormat==FORMAT_PDF)
					fmt = "pdf";

				p = Runtime.getRuntime().exec(dotPath + " -T" + fmt + " -o " + outfile + " " + dotFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

        	if (monitor!=null)
        		monitor.setTaskName("Running dot.exe");

			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

        	if (monitor!=null)
        		monitor.done();

			ok = true;
		}
	}


	private boolean run (Shell shell, String dotPath, int outputFormat, String outfile, String dotFile) {
		MyRunnable runnable = new MyRunnable(dotPath, outputFormat, outfile, dotFile);
		try {
			if (shell==null) {
				runnable.run(null);
			} else {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
				dialog.run(true, true, runnable);
			}
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return runnable.ok;
	}

}
