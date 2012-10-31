/*
 * Project: com.harman.simbench.hbsim.tests
 * (c) copyright 2010 by Harman/Becker Automotive Systems GmbH
 * Secrecy Level STRICTLY CONFIDENTIAL
 */
package com.harman.simbench.hbsim.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.harman.simbench.HbsimRuntimeModule;
import com.harman.simbench.HbsimStandaloneSetup;
//import com.harmanbecker.sys.dsi.model.util.internal.ISearchPathProvider;
//import com.harmanbecker.utils.MoCCASearchPath;

/**
 *
 * @author KBirken
 */
public class HbsimTestSetup extends HbsimStandaloneSetup
{
//   private final String        moccaPath;
//   private final String        projectPath;
//   private final String        generatedRoot;
//   private ISearchPathProvider searchPathProvider = new ISearchPathProvider()
//                                                  {
//                                                     public MoCCASearchPath getSearchPath()
//                                                     {
//                                                        return MoCCASearchPath.create(
//                                                              moccaPath, projectPath,
//                                                              generatedRoot);
//                                                     }
//                                                  };

//   public HbsimTestSetup(String projectRoot, String deliveriesInclude,
//         String additionalIncludes)
//   {
//      projectPath = projectRoot;
//      moccaPath = deliveriesInclude;
//      generatedRoot = additionalIncludes;
//   }

   @Override
   public Injector createInjector()
   {
      return Guice.createInjector(new HbsimRuntimeModule()
      {
//         @Override
//         public ISearchPathProvider bindSearchPathProvider()
//         {
//            return searchPathProvider;
//         }
      });
   }
}
