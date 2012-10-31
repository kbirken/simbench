package com.harman.simbench.hbsim.tests;


import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggingEnvironmentTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void createLoggerAndLog() throws Exception {
        Log logger = LogFactory.getLog("my test logger");
        logger.info("Hello World!");
        assertTrue("We expect Log4JLogger as a logger", logger instanceof Log4JLogger);
	}

}
