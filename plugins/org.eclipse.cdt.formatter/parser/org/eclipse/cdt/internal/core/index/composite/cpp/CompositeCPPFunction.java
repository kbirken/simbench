/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPFunction extends CompositeCPPBinding implements ICPPFunction, ICPPDelegateCreator {

	public CompositeCPPFunction(ICompositesFactory cf, ICPPFunction rbinding) {
		super(cf, rbinding);
	}

	public boolean isInline() throws DOMException {
		return ((ICPPFunction)rbinding).isInline();
	}

	public boolean isMutable() throws DOMException {
		return ((ICPPFunction)rbinding).isMutable();
	}

	public IScope getFunctionScope() throws DOMException {
		fail(); return null;
	}

	public IParameter[] getParameters() throws DOMException {
		IParameter[] result = ((ICPPFunction)rbinding).getParameters();
		for(int i=0; i<result.length; i++) {
			result[i] = (IParameter) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public IFunctionType getType() throws DOMException {
		IType rtype = ((ICPPFunction)rbinding).getType();
		return (IFunctionType) cf.getCompositeType((IIndexType)rtype);
	}

	public boolean isAuto() throws DOMException {
		return ((ICPPFunction)rbinding).isAuto();
	}

	public boolean isExtern() throws DOMException {
		return ((ICPPFunction)rbinding).isExtern();
	}

	public boolean isRegister() throws DOMException {
		return ((ICPPFunction)rbinding).isRegister();
	}

	public boolean isStatic() throws DOMException {
		return ((ICPPFunction)rbinding).isStatic();
	}

	public boolean takesVarArgs() throws DOMException {
		return ((ICPPFunction)rbinding).takesVarArgs();
	}

	public Object clone() {
		fail(); return null;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append(getName()+" "+ASTTypeUtil.getParameterTypeString(getType())); //$NON-NLS-1$
		} catch(DOMException de) {
			result.append(de);
		}
		return result.toString();
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPFunction.CPPFunctionDelegate(name, this);
	}
}
