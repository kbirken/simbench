/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface repesents a mechanism for a name to discover more information about it's parent.
 * All interfaces that claim ownership/residence of a name should extend this interface.
 * 
 * @author jcamelon
 */
public interface IASTNameOwner {
	
	/**
	 * Role of name in this context is a declaration.
	 */
	public static final int r_declaration = 0;
	/**
	 * Role of name in this construct is a reference.
	 */
	public static final int r_reference = 1;
    
    /**
     * Role of name in this construct is a definition. 
     */
    public static final int r_definition = 2;
	/**
	 * Role is unclear.
	 */
	public static final int r_unclear = 3; 
	
	/**
	 * Get the role for the name. 
	 * 
	 * @param name <code>IASTName</code>
	 * @return r_declaration, r_reference or r_unclear.
	 */
	public int getRoleForName( IASTName n );

}
