/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * @author jcamelon
 */
public abstract class ASTNode implements IASTNode {

    private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];

    private IASTNode parent;
    private ASTNodeProperty property;

    private int length;
    private int offset;

    public IASTNode getParent() {
    	return parent;
    }
    
    public void setParent(IASTNode node) {
    	this.parent = node;
    }
    
    public ASTNodeProperty getPropertyInParent() {
    	return property;
    }
    
    public void setPropertyInParent(ASTNodeProperty property) {
    	this.property = property;
    }
    
    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.locations = null;
    }

    public void setLength(int length) {
        this.length = length;
        this.locations = null;
    }

    public void setOffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
        this.locations = null;
    }

    public void setOffsetAndLength(ASTNode node) {
        setOffsetAndLength(node.getOffset(), node.getLength());
    }

    private IASTNodeLocation[] locations = null;
    private IASTFileLocation fileLocation = null;

    public IASTNodeLocation[] getNodeLocations() {
        if (locations != null)
            return locations;
        if (length == 0)
            return EMPTY_LOCATION_ARRAY;
        locations = getTranslationUnit().getLocationInfo(offset, length);
        return locations;
    }

    public String getRawSignature() {
        return getTranslationUnit().getUnpreprocessedSignature(
                getNodeLocations());
    }

    public String getContainingFilename() {
        return getTranslationUnit().getContainingFilename(offset);
    }

    public IASTFileLocation getFileLocation() {
        if( fileLocation != null )
            return fileLocation;
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null)
        	fileLocation = ast.flattenLocationsToFile( getNodeLocations() );
        return fileLocation;
    }
    
    public IASTTranslationUnit getTranslationUnit() {
       	return parent != null ? parent.getTranslationUnit() : null;
    }

    public boolean accept(ASTVisitor visitor) {
    	return true;
    }
    
    public boolean contains(IASTNode node) {
    	if (node instanceof ASTNode) {
    		ASTNode astNode= (ASTNode) node;
    		return offset <= astNode.offset && 
    			astNode.offset+astNode.length <= offset+length;
    	}
    	return false;
    }
}
