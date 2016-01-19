package org.nanosite.simbench.backend.warp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.console.MessageConsoleStream;

public class WarpRunner {

	// the preferences key
	public static String ID_WARP_PATH = "org.nanosite.simbench.ide.warp_path";
		
	private MessageConsoleStream out = null;
	private MessageConsoleStream err = null;
	
	private String warpPath = null;

	private String simuResultDotFile = "";

	public WarpRunner(MessageConsoleStream out, MessageConsoleStream err) {
		this.out = out;
		this.err = err;

		// get warp path from preferences
		IPreferencesService service = Platform.getPreferencesService();
		this.warpPath = service.getString("org.nanosite.simbench.ide", ID_WARP_PATH, "", null);
	}

	public boolean run (String warpModelFile, String traceDir) {
		if (warpPath==null) {
			err("path to warp executable is not configured in preferences, skipping.");
			return false;
		}
		
		out("loading " + warpModelFile);

		// ensure warp.exe tool is available
		File warp = new File(warpPath);
		if (! warp.exists()) {
			err("warp executable is not available (configured path: '" + warpPath + "').");
			return false;
		}

		// ensure traces directory is available
		File dir = new File(traceDir);
		if (! (dir.exists() || dir.mkdirs())) {
			err("cannot create traces directory " + traceDir + ", aborting simulation.");
			return false;
		}

		// remove previous trace file (if any)
		String traceFile = traceDir + "/warptrace.txt";
		{
			File traceF = new File(traceFile);
			if (traceF.exists()) {
				if (! traceF.delete()) {
					err("couldn't delete previous trace file (configured path: '" + traceFile + "'), aborting.");
					return false;
				}
			}
		}

		// remove previous simu_dot-file (if any)
		simuResultDotFile = traceDir + "/simu_dot.txt";
		{
			File simuDotF = new File(simuResultDotFile);
			if (simuDotF.exists()) {
				if (! simuDotF.delete()) {
					err("couldn't delete previous simu-dot file (configured path: '" + simuResultDotFile + "'), aborting.");
					return false;
				}
			}
		}

		Process p = null;
		try {
			out("executing simulation ...");
			String cmdline = warpPath + " -v3 -g " + simuResultDotFile + " " + warpModelFile;
			p = Runtime.getRuntime().exec(cmdline);

			// redirect output to trace file
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			FileOutputStream os = new FileOutputStream(traceFile);
			BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os));
			String text;
			while ((text = in.readLine()) != null) {
				bos.write(text);
				bos.newLine();
			}
			bos.flush();
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			p.waitFor();

			File traceF = new File(traceFile);
			if (traceF.exists()) {
				out("created trace file '" + traceFile + "'.");
			} else {
				err("trace file hasn't been written ('" + traceFile + "').");
			}

			File simuDotF = new File(simuResultDotFile);
			if (simuDotF.exists()) {
				out("created simu-result dot-file '" + simuResultDotFile + "'.");
			} else {
				err("simu-dot file hasn't been written ('" + simuResultDotFile + "').");
			}

			int exitval = p.exitValue();
			out("simulation ready (exit value " + exitval + ").");
			if (exitval>0) {
				if (exitval==100) {
					err("warp couldn't read input file.");
				} else if (exitval==200) {
					err("parts of the model were never executed during simulation (cf. warptrace.txt).");
				} else {
					err("unknown warp error code.");
				}
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}


	private void out (String txt) {
		out.println("WarpRunner: " + txt);
	}

	private void err (String txt) {
		err.println("WarpRunner error: " + txt);
	}

	public String getSimuDotFile() {
		return simuResultDotFile;
	}
}
