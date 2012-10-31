package com.harman.simbench.hbsim.tests;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.IStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.harman.common.util.file.FileUtils;
import com.harman.simbench.chronsim.ChronsimRunner;
import com.harman.simbench.hbsim.generator.ui.SimbenchConsole;

public class ChronsimTest {

	// configure chronSIM installation here
	public static boolean isChronSimInstalled = true;
	public static String CHRONSIM_PATH = "C:/Program Files/INCHRON Tool-Suite/bin";

	public static String TESTS_DIR = "sim";

	GeneratorRunner runner = null;
	SimbenchConsole console = null;

	@Before
	public void setUp() throws Exception {
		runner = new GeneratorRunner();
    	//console = new SimbenchConsole(); // TODO: doesn't work yet
		assertTrue("Could not create tests directory", FileUtils.ensureDir(TESTS_DIR));
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void callChronsimTest01() throws Exception {
		callChronsimTest("test01");
	}

	@Test
	public void callChronsimTest02() throws Exception {
		callChronsimTest("test02");
	}

	@Test
	public void callChronsimTest03() throws Exception {
		callChronsimTest("test03");
	}

	@Test
	public void callChronsimTest04() throws Exception {
		callChronsimTest("test04");
	}


	private void callChronsimTest(String testname) throws Exception {

		// TODO: this premature check could be removed, however, there
		//       is a NoClassDefFoundError when this is run by Hudson
		//       (for class org/eclipse/jface/text/IDocument)
		if (! isChronSimInstalled) {
        	// chronSIM is not available, we skip this test
        	return;
        }
		String testDir = TESTS_DIR + "/" + testname;
		assertTrue("Cannot create single test directory", FileUtils.ensureDir(testDir));

        final Map<String,String> properties = new HashMap<String,String>();
        String srcGenPathRel = testDir + "/src-gen";
        String srcGenPathAbs = srcGenPathRel;
        String baseDirRel = "base/src";
        properties.put("srcGenPathRel", srcGenPathRel);
        properties.put("srcGenPathAbs", srcGenPathAbs);
        properties.put("basePrefix", baseDirRel);
        properties.put("genPrefix", "src-gen");

        runner.run("generate_chronsim.mwe", testname + "/main.hbsim", properties, "dot");

    	// copy chronSIM project file (*.ipr)
        String iprFile = "model_gen.ipr";
        String iprTarget = testDir + "/" + iprFile;
        assertTrue("Chronsim ipr file cannot be copied",
        		FileUtils.copy(srcGenPathAbs + "/" + iprFile, iprTarget));

        if (! isChronSimInstalled) {
        	// chronSIM is not available, we skip this test
        	return;
        }

        // provide additional code for chronSIM execution
        String baseDir = testDir + "/" + baseDirRel;
        assertTrue("Directory for chronSIM base files could not be provided", FileUtils.ensureDir(baseDir));
//        assertTrue("Chronsim base files could not be copied",
//        		ChronsimCodeProvider.copyChronsimCode(baseDir));
        assertTrue("Chronsim base file cpu.h could not be copied",
        		FileUtils.copy("sim/base/chronSim/src/cpu.h", baseDir + "/cpu.h"));
        assertTrue("Chronsim base file cpu_common.c could not be copied",
        		FileUtils.copy("sim/base/chronSim/src/cpu_common.c", baseDir + "/cpu_common.c"));

        // start chronSIM as batch
        String tracesDir = testDir + "/traces";
        assertTrue("Could not create traces directory", FileUtils.ensureDir(tracesDir));
    	//ChronsimRunner chronsimRunner = new ChronsimRunner(console.getOut(), console.getErr());
        ChronsimRunner chronsimRunner = new ChronsimRunner(CHRONSIM_PATH);
        Job job = chronsimRunner.run(iprTarget, 30, tracesDir);
        assertTrue("Chronsim job could not be started", job!=null);

        // wait until job is finished and check result
        job.join();
        IStatus status = job.getResult();
        assertTrue("Chronsim job status is not ok (see console for error messages)", status.isOK());

        // read file with expected results
        Map<String,Long> results = new HashMap<String,Long>();
        assertTrue("File with expected results could not be loaded",
        		readResultsFile(GeneratorRunner.getTestsPath() + "/" + testname + "/results.txt", results));

        // check if all expected results are there at right times
        for(Map.Entry<String,Long> e : results.entrySet()) {
        	String ev = e.getKey();
        	assertTrue("Result event '" + ev + "' not found in simulation",
        			chronsimRunner.hasEvent(ev));
        	long tSimu = chronsimRunner.getEventTime(ev);
        	long tExp = e.getValue().longValue();
        	long delta = Math.abs(tSimu - tExp);
        	if (delta > 5) {
        		// more than 5 millisecs off is regarded as fault
        		fail("Wrong time for event " + ev + " (simulated: " + tSimu + ", expected " + tExp + ")");
        	}
        }
        System.out.println("All expected event times are ok. Simulation test ok.");
	}


	private boolean readResultsFile (String filename, Map<String,Long> results) {
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			int line = 0;
			String input;
			Pattern pat = Pattern.compile("(.*)::(.*?)\\s*@\\s*(\\d+)");
			while ((input = br.readLine()) != null) {
				line++;

				// skip empty lines comment lines
				if (! (input.isEmpty() || input.startsWith("#"))) {
					Matcher m = pat.matcher(input);
					if (m.find()) {
						String fb = m.group(1);
						String step = m.group(2);
						Long t = new Long(m.group(3));
						results.put(fb + "_" + step, t.longValue());
					} else {
						fail("Syntax error in results file '" + filename + "', line " + line);
						return false;
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			fail("File " + filename + " not found");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
