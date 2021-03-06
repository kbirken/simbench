/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Iterator;
import java.util.List;


/**
 * @author jcamelon
 */
public interface ITokenDuple {
	/**
	 * @return
	 */
	public abstract IToken getFirstToken();
	/**
	 * @return
	 */
	public abstract IToken getLastToken();
	
	public List [] getTemplateIdArgLists();
	
	public ITokenDuple getLastSegment();
	public ITokenDuple getLeadingSegments();
	public int getSegmentCount();
	
	public abstract Iterator iterator();
	public abstract String toString();
	public char [] toCharArray();
	
	public char [] getFilename();
		
	public abstract boolean isIdentifier();
	public abstract int length(); 
	
	public abstract ITokenDuple getSubrange( int startIndex, int endIndex );
	public IToken getToken(int index);
	public ITokenDuple[] getSegments();
	
	public int findLastTokenType( int type );
	
	public int getStartOffset();
	public int getEndOffset();
	public int getLineNumber();
	/**
	 * @return
	 */
	public abstract boolean syntaxOfName();
	
	public char[] extractNameFromTemplateId();
	/**
	 * @param duple
	 * @return
	 */
	public boolean contains(ITokenDuple duple);
	/**
	 * @return
	 */
	public abstract String [] toQualifiedName();
	
	public void freeReferences( );
	public void acceptElement( ISourceElementRequestor requestor );
}
