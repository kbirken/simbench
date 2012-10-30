/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * @author jcamelon
 */
public interface ICPPASTTypenameExpression extends IASTExpression, IASTNameOwner {

	/**
	 * Was template token consumed?
	 * 
	 * @param templateTokenConsumed
	 *            boolean
	 */
	public void setIsTemplate(boolean templateTokenConsumed);

	/**
	 * Was template token consumed?
	 * 
	 * @return boolean
	 */
	public boolean isTemplate();

	/**
	 * <code>TYPENAME</code> is the name of the type.
	 */
	public static final ASTNodeProperty TYPENAME = new ASTNodeProperty(
			"ICPPASTTypenameExpression.TYPENAME - The name of the type"); //$NON-NLS-1$

	/**
	 * Set the name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * Get the name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * <code>INITIAL_VALUE</code> is an expression.
	 */
	public static final ASTNodeProperty INITIAL_VALUE = new ASTNodeProperty(
			"ICPPASTTypenameExpression.INITIAL_VALUE - Initial Value is an expression"); //$NON-NLS-1$

	/**
	 * Set initial value.
	 * 
	 * @param expressionList
	 *            <code>IASTExpression</code>
	 */
	public void setInitialValue(IASTExpression expressionList);

	/**
	 * Get initial value.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getInitialValue();

}
