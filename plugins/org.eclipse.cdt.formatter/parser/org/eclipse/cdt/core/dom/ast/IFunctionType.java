/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 8, 2004
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IFunctionType extends IType {
    /**
     * get the return type of this function type
     * @return
     * @throws DOMException
     */
    public IType getReturnType() throws DOMException;
    
    /**
     * get the adjusted parameter types
     * ISO C99 6.7.5.3, ISO C++98 8.3.4-3 
     * @return
     * @throws DOMException
     */
    public IType [] getParameterTypes() throws DOMException;
}
