/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.core.runtime.IPath;

/**
 * Internal singleton map maintained for non-project PDOM objects
 */
class PDOMCache {
	private Map/*<File, PDOM>*/ path2pdom; // gives the PDOM for a particular path

	private static PDOMCache singleton;
	private static Object singletonMutex = new Object();

	private PDOMCache() {
		this.path2pdom = new HashMap();
	}

	/**
	 * Returns the instance of the cache
	 * @return
	 */
	public static PDOMCache getInstance() {
		synchronized(singletonMutex) {
			if(singleton == null) {
				singleton = new PDOMCache();
			}
			return singleton;
		}
	}

   /**
    * Returns the mapped PDOM for the path specified, if such a pdom is not
    * already known about then one is created using the location converter
    * specified.
    * 
    * @param path
    * @param converter
    * @return a PDOM instance or null if the PDOM version was too old
    */
	public PDOM getPDOM(IPath path, IIndexLocationConverter converter) {
		PDOM result= null;
		return result;
	}
}