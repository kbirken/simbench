package com.harman.simbench.hbsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.mwe.core.WorkflowEngine;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;

public class GeneratorRunner {

	final static String TEST_PATH = "platform:/resource/com.harman.simbench.hbsim.examples/tests";
	final static String GENERATOR_PATH = "../com.harman.simbench.hbsim.generator/src/workflow";

	public GeneratorRunner() {
		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("..");
	}

	public void run (String mweFile, String modelFile, Map<String,String> properties, String targetDir) throws Exception {
        final Map<String,String> props = new HashMap<String,String>();
		if (properties!=null) {
			props.putAll(properties);
		}
		props.put("modelFile", TEST_PATH + "/" + modelFile);
		if (! props.containsKey("srcGenPathRel")) {
			props.put("srcGenPathRel", "src-gen/" + targetDir);
		}
		if (! props.containsKey("srcGenPathAbs")) {
			props.put("srcGenPathAbs", "src-gen/" + targetDir);
		}

        final Map<String,String> slotContents = new HashMap<String,String>();
        WorkflowEngine runner = new WorkflowEngine();
		assertTrue("Workflow could not be prepared",
				runner.prepare(GENERATOR_PATH + "/" + mweFile, null, props));

		final Issues issues = new IssuesImpl();
		runner.executeWorkflow(slotContents, issues);
		assertEquals("Workflow could not be executed without errors", 0, issues.getErrors().length);
	}

	public static String getTestsPath() {
		URI uri = URI.createURI(TEST_PATH);
	    String path = uri.toPlatformString(true);
	    URI resolved = EcorePlugin.resolvePlatformResourcePath(path);
	    return resolved.path();
	}
}
