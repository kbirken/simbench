/*
 * Project: org.nanosite.simbench.injection.analyser
 * (c) copyright 2010 by Harman/Becker Automotive Systems GmbH
 * Secrecy Level STRICTLY CONFIDENTIAL
 */
package org.nanosite.simbench.injection.analyser;

import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.osgi.framework.Bundle;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import org.nanosite.simbench.injection.HbinjRuntimeModule;
import org.nanosite.simbench.injection.ui.HbinjUiModule;

/**
 *
 * @author DaWeber
 */
public class HbinjExecutableExtensionFactory extends
	org.nanosite.simbench.injection.ui.HbinjExecutableExtensionFactory
{
   private Injector injector;

   @Override
   protected Bundle getBundle()
   {
      return Activator.getDefault().getBundle();
   }

   @Override
   protected Injector getInjector()
   {
      if(null == injector)
      {
         injector = Guice.createInjector(Modules.override(
               Modules.override(new HbinjRuntimeModule()).with(
                     new HbinjUiModule(Activator.getDefault()))).with(
               new SharedStateModule()));
      }
      return injector;
   }
}
