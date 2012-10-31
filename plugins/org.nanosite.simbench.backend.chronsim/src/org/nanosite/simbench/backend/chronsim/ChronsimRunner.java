package org.nanosite.simbench.backend.chronsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.console.MessageConsoleStream;

public class ChronsimRunner {

	// the preferences key
	public static String ID_CHRONSIM_PATH = "org.nanosite.simbench.ide.chronsim_path";

	String chronSimPath = null;

	private MessageConsoleStream out = null;
	private MessageConsoleStream err = null;

	// hash map for storing all events resulting from simulation
	Map<String,Long> events = new HashMap<String,Long>();

	public ChronsimRunner(String chronSimPath) {
		this.chronSimPath = chronSimPath;
	}

	public ChronsimRunner(MessageConsoleStream out, MessageConsoleStream err) {
		this.out = out;
		this.err = err;

		// get chronSim path from preferences
		IPreferencesService service = Platform.getPreferencesService();
		this.chronSimPath = service.getString("org.nanosite.simbench.ide", ID_CHRONSIM_PATH, "", null);
	}

	public Job run (String iprFile, int durationSecs, String traceDir) {
		String batchExePath = getBatchExePath();
		if (batchExePath==null)
			return null;

		String traceconvPath = getTraceconvPath();
		if (traceconvPath==null)
			return null;

		final MyJob job = new MyJob(batchExePath, iprFile, durationSecs, traceDir, traceconvPath);
		job.setPriority(Job.LONG);
		job.schedule();

		return job;
	}


	public boolean hasEvent (String event) {
		return events.containsKey(event);
	}

	public long getEventTime (String event) {
		return events.get(event);
	}


	class MyJob extends Job {
		boolean ok = false;

		String batchExePath;
		String iprFile;
		int durationSecs;
		String traceDir;
		String traceConvPath;

		public MyJob (String batchExePath, String iprFile, int durationSecs, String traceDir, String traceConvPath) {
			super("ChronSIM batch");
			this.batchExePath = batchExePath;
			this.iprFile = iprFile;
			this.durationSecs = durationSecs;
			this.traceDir = traceDir;
			this.traceConvPath = traceConvPath;
		}

		public IStatus run (IProgressMonitor monitor) {
			// register job in case of plugin shutdown
			Activator.registerJob(this);

			String rawTraceFile = traceDir + "/simtrace.isf";

			// heuristics for progress bar / monitor:
			// Parsing 10%, Analyzing 30%, Linking 50%, Trace conversion 10%
			int parsingPerc = 10;
			int analyzingPerc = 30;
			int linkingPerc = 50;

        	Process p = null;
        	if (monitor!=null)
        		monitor.beginTask("Running chronSIM batch", 100);
        	out("Starting chronSim ...");
        	boolean cancel = false;
			try {
				p = Runtime.getRuntime().exec(batchExePath + " -o " + rawTraceFile + " --until=" + durationSecs + "s " + iprFile);

				// redirect output to console
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

				int nParsing = 0;
				int nAnalyzing = 0;
				String text;
				while (!cancel && (text = in.readLine()) != null) {
					if (monitor!=null)
						cancel = monitor.isCanceled();

					out("chronSIM: " + text);

					if (text.contains("Parsing")) {
						if (monitor!=null && nParsing==0)
							monitor.worked(1);
						nParsing++;
					}
					else if (text.contains("Analyzing")){
						if (monitor!=null) {
							if (nAnalyzing==0)
								monitor.worked(parsingPerc-1);
							else {
								int t1 = (nAnalyzing*analyzingPerc)/nParsing;
								int t2 = ((nAnalyzing+1)*analyzingPerc)/nParsing;
								if (t2>t1)
									monitor.worked(t2-t1);
							}
						}
						nAnalyzing++;
					}
				}
			} catch (IOException e) {
				String msg = "Could not complete chronSIM batch process (IOException)";
				err(msg); e.printStackTrace();
				return new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e);
			}

			if (cancel) {
				out("Simulation with chronSim has been canceled.");
				return Status.CANCEL_STATUS;
			}

			try {
				p.waitFor();
			} catch (InterruptedException e) {
				String msg = "Could not complete chronSIM batch process (InterruptedException)";
				err(msg); e.printStackTrace();
				return new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e);
			}

        	if (monitor!=null)
        		monitor.worked(linkingPerc);

			out("Simulation with chronSim ready.");

        	if (monitor!=null)
        		monitor.setTaskName("Converting chronSIM traces");

			out("Converting traces ...");


        	Process q = null;
			try {
				q = Runtime.getRuntime().exec(traceConvPath + " -c --event " + rawTraceFile);

				// prepare pattern matcher for events in output trace file
				//example: 10000000081,"CPU1","FB1_TG0(3)","FB1_continue/FB1_step2","0",
				Pattern pEvent = Pattern.compile("(\\d+),\"(.*)\",\"(.*)\",\"(.*)/(.*)\",\".*\"");

				// redirect output to file
				String traceFile = traceDir + "/simtrace.txt";
				BufferedReader in = new BufferedReader(new InputStreamReader(q.getInputStream()));
				FileOutputStream os = new FileOutputStream(traceFile);
				BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os));
				String text;
				BigInteger million = new BigInteger("1000000");
				while ((text = in.readLine()) != null) {
					bos.write(text);
					bos.newLine();

					Matcher m = pEvent.matcher(text);
					if (m.find()) {
						BigInteger tNanoSec = new BigInteger(m.group(1));
						long tMilliSec = tNanoSec.divide(million).longValue();
						//String cpu = m.group(2);
						//String taskGroup = m.group(3);
						String behaviour = m.group(4);
						String step = m.group(5);

						if (behaviour.startsWith("execute") && (step.equals("entry") || step.equals("exit"))) {
							// ignore
						} else {
							//System.out.println(tMilliSec + ": " + behaviour + " :: " + step);
							events.put(step, tMilliSec);
						}
					}
				}
				bos.close();
				os.close();

				// optional additional conversion (needed?)
				// %chronsimPath%\traceconv.exe -r traces\simtrace.isf >traces\simtrace_raw.txt

			} catch (IOException e) {
				String msg = "Could not complete traceconv process (IOException)!";
				err(msg); e.printStackTrace();
				return new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e);
			}

			try {
				q.waitFor();
			} catch (InterruptedException e) {
				String msg = "Could not complete traceconv process (InterruptedException)";
				err(msg); e.printStackTrace();
				return new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e);
			}

			out("Converting traces ready.");

        	if (monitor!=null)
        		monitor.done();

			ok = true;

			// unregister job
			Activator.unregisterJob(this);

			return Status.OK_STATUS;
		}
	}


	// ensure batch.exe tool is available in chronSim installation
	private String getBatchExePath() {
		String exePath = chronSimPath + "/batch.exe";

		File file = new File(exePath);
		if (! file.exists()) {
			err("Error: batch.exe is not available (configured path: '" + exePath + "').");
			return null;
		}
		return exePath;
	}

	// ensure batch.exe tool is available in chronSim installation
	private String getTraceconvPath() {
		String exePath = chronSimPath + "/traceconv.exe";

		File file = new File(exePath);
		if (! file.exists()) {
			err("Error: traceconv.exe is not available (configured path: '" + exePath + "').");
			return null;
		}
		return exePath;
	}


	private void out(String txt) {
		if (out!=null) {
			out.println(txt);
		} else {
			System.out.println(txt);
		}
	}

	private void err(String txt) {
		String msg = "Error: " + txt + "!";
		if (err!=null) {
			err.println(msg);
		} else {
			System.err.println(msg);
		}
	}
}
