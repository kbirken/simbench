package com.harman.simbench.hbsim.tests;


import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeneratorTest {


	GeneratorRunner runner = null;

	@Before
	public void setUp() throws Exception {
		runner = new GeneratorRunner();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void callHbsimDotGeneratorTest01() throws Exception {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("withScenario", "true");
        properties.put("compact", "true");
        runner.run("generate_dot.mwe", "test01/main.hbsim", properties, "dot");
	}

	@Test
	public void callHbsimWarpGeneratorTest01() throws Exception {
        runner.run("generate_warp.mwe", "test01/main.hbsim", null, "warp");
	}

	@Test
	public void callHbsimReportsGeneratorTest01() throws Exception {
        runner.run("generate_reports.mwe", "test01/main.hbsim", null, "reports");
	}


	@Test
	public void callHbsimDotGeneratorTest02() throws Exception {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("withScenario", "true");
        properties.put("compact", "true");
        runner.run("generate_dot.mwe", "test02/main.hbsim", properties, "dot");
	}

	@Test
	public void callHbsimWarpGeneratorTest02() throws Exception {
        runner.run("generate_warp.mwe", "test02/main.hbsim", null, "warp");
	}

	@Test
	public void callHbsimReportsGeneratorTest02() throws Exception {
        runner.run("generate_reports.mwe", "test02/main.hbsim", null, "reports");
	}


	@Test
	public void callHbsimDotGeneratorTest03() throws Exception {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("withScenario", "true");
        properties.put("compact", "true");
        runner.run("generate_dot.mwe", "test03/main.hbsim", properties, "dot");
	}

	@Test
	public void callHbsimWarpGeneratorTest03() throws Exception {
        runner.run("generate_warp.mwe", "test03/main.hbsim", null, "warp");
	}

	@Test
	public void callHbsimReportsGeneratorTest03() throws Exception {
        runner.run("generate_reports.mwe", "test03/main.hbsim", null, "reports");
	}


	@Test
	public void callHbsimDotGeneratorTest04() throws Exception {
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("withScenario", "true");
        properties.put("compact", "true");
        runner.run("generate_dot.mwe", "test04/main.hbsim", properties, "dot");
	}

	@Test
	public void callHbsimWarpGeneratorTest04() throws Exception {
        runner.run("generate_warp.mwe", "test04/main.hbsim", null, "warp");
	}

	@Test
	public void callHbsimReportsGeneratorTest04() throws Exception {
        runner.run("generate_reports.mwe", "test04/main.hbsim", null, "reports");
	}
}

