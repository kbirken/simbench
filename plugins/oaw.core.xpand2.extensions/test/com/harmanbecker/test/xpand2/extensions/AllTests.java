/*
 * Project: oaw.core.xpand2.extensions
 * (c) copyright 2008 by Harman/Becker Automotive Systems GmbH
 */
package com.harmanbecker.test.xpand2.extensions;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author DaWeber
 */
public class AllTests
{
   /**
    * @return A list of test cases
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite("Test for com.harmanbecker.test.xpand2.extensions");
      //$JUnit-BEGIN$
      suite.addTestSuite(CppBeautifierTest.class);
      //$JUnit-END$
      return suite;
   }

}
