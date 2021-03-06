/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.IToken;

/**
 * @author Doug Schaefer
 */
public class MacroExpansionToken implements IToken {

	public MacroExpansionToken() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getType()
	 */
	public int getType() {
		return IToken.tMACROEXP;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getImage()
	 */
	public String getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getOffset()
	 */
	public int getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getLength()
	 */
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getEndOffset()
	 */
	public int getEndOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getLineNumber()
	 */
	public int getLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getNext()
	 */
	public IToken getNext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#setImage(java.lang.String)
	 */
	public void setImage(String i) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#setNext(org.eclipse.cdt.core.parser.IToken)
	 */
	public void setNext(IToken t) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#setType(int)
	 */
	public void setType(int i) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#looksLikeExpression()
	 */
	public boolean looksLikeExpression() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#isPointer()
	 */
	public boolean isPointer() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#isOperator()
	 */
	public boolean isOperator() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#canBeAPrefix()
	 */
	public boolean canBeAPrefix() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getCharImage()
	 */
	public char[] getCharImage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#setImage(char[])
	 */
	public void setImage(char[] i) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getFilename()
	 */
	public char[] getFilename() {
		return null;
	}

}
