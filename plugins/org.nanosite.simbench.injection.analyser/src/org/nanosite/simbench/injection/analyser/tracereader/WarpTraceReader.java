package org.nanosite.simbench.injection.analyser.tracereader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

public class WarpTraceReader {

	static private Pattern eventPattern = Pattern.compile("(\\d+) (DONE|READY)\\s+(.*)::(.*)::(.*)");
	static BigInteger THOUSAND = new BigInteger("1000");

	ITraceReaderClient client = null;

	public WarpTraceReader (ITraceReaderClient client) {
		this.client = client;
	}

	// hash for behaviours we have already found in trace
	private Set<String> found = null;

	public int readTrace (String filename, IProgressMonitor monitor) {
		int ret = -1;
		int line = 0;
		try {
			found = new HashSet<String>();

			File file = new File(filename);
			Long s = file.length() / 1024L;
			monitor.beginTask("Reading warp tracefile ...", s.intValue());
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String input;
			Long sread = 0L;
			int mon = 0;
			while ((! monitor.isCanceled()) && ((input = br.readLine()) != null)) {
				parseLine(line, input);
				line++;

				// monitor handling
				sread += input.length();
				Long sr2 = sread/1024;
				if (sr2.intValue() > mon) {
					monitor.worked(sr2.intValue()-mon);
					mon = sr2.intValue();
				}
			}
			in.close();
		} catch (Exception e) {
			System.err.println("WarpTraceReader error: " + e.getMessage());
		} finally {
			monitor.done();
			ret = line;
		}

		if (monitor.isCanceled()) {
			System.err.println("Reading of warp trace had been canceled by user!");
		}

		return ret;
	}

	private void parseLine (int line, String input) {
		Matcher m = eventPattern.matcher(input);
		if (m.find()) {
			// convert timestamp from microsec to millisec
			BigInteger t = (new BigInteger(m.group(1))).divide(THOUSAND);
			String event = m.group(2);
			String fb = m.group(3);
			String bhvr = m.group(4);
			String step = m.group(5);
			if (event.equals("DONE")) {
				client.processEventDone(line, t.intValue(), fb, bhvr, step);
			} else {
				// READY: send only if beginning of behaviour
				String key = fb + "::" + bhvr;
				if (! found.contains(key)) {
					found.add(key);
					client.processStartEvent(line, t.intValue(), fb, bhvr);
				}
			}
		}
	}
}
