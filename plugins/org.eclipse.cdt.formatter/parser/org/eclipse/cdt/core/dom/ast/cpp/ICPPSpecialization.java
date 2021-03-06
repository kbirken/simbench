/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * /
 *******************************************************************************/
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * For an instantiation of a class template, the members of that instantiation will be
 * specializations of the members of the original class template.
 * For an instantiation of a function template, the parameters will be specializations 
 * of the parameters of the original function template.
 * 
 * Specializations can also be explicitly defined
 * 
 * @author aniefer
 *
 */
public interface ICPPSpecialization extends ICPPBinding {
	/**
	 * Return the binding that this specialization specializes.
	 * @return the original binding that this is a specialization of
	 */ 
	public IBinding getSpecializedBinding();
	
	/**
	 * Returns the argument map for this specialization. For partial specializations, only
	 * those arguments which have been specialized will appear.
	 * @return a map which maps from template parameter to the corresponding
	 * template argument
	 */
	public ObjectMap getArgumentMap();
}
