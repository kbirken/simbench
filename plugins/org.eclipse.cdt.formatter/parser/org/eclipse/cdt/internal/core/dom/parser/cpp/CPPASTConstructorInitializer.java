/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTConstructorInitializer extends CPPASTNode implements
        ICPPASTConstructorInitializer, IASTAmbiguityParent {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer#getExpression()
     */
    public IASTExpression getExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer#setExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setExpression(IASTExpression expression) {
        this.exp = expression;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitInitializers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( exp != null ) if( !exp.accept( action ) ) return false;
        if( action.shouldVisitInitializers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
    	if (child == exp) {
    		other.setPropertyInParent(child.getPropertyInParent());
    		other.setParent(child.getParent());
    		exp = (IASTExpression) other;
        }
    }
}
