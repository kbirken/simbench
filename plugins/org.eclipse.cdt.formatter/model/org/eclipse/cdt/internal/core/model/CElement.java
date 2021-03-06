/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;

public abstract class CElement extends PlatformObject implements ICElement {

	protected static final CElement[] NO_ELEMENTS = new CElement[0];
	protected int fType;

	protected ICElement fParent;

	protected String fName;

	protected CElement(ICElement parent, String name, int type) {
		fParent= parent;
		fName= name;
		fType= type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		// handle all kinds of resources
		if (IResource.class.isAssignableFrom(adapter)) {
			IResource r= getResource();
			if (r != null && adapter.isAssignableFrom(r.getClass())) {
				return r;
			}
		}
		return super.getAdapter(adapter);
	}


	// setters

	public void setElementType (int type) {
		fType= type;
	}

	public void setElementName(String name) {
		fName = name;
	}

	public void setParent (ICElement parent) {
		fParent = parent;
	}

	// getters

	public int getElementType() {
		return fType;
	}

	public String getElementName() {
		return fName;
	}

	public ICElement getParent() {
		return fParent;
	}

	public IPath getPath() {
		IResource res = getUnderlyingResource();
		if (res != null)
			return res.getFullPath();
		return new Path(getElementName());
	}


	/**
	 * Returns the elements that are located at the given source offset
	 * in this element.  This is a helper method for <code>ITranslationUnit#getElementAtOffset</code>,
	 * and only works on compilation units and types. The offset given is
	 * known to be within this element's source range already, and if no finer
	 * grained element is found at the offset, this element is returned.
	 */

	public boolean isReadOnly () {
		IResource r = getUnderlyingResource();
		if (r != null) {
			ResourceAttributes attributes = r.getResourceAttributes();
			if (attributes != null) {
				return attributes.isReadOnly();
			}
		}
		return false;
	}

	public ICModel getCModel () {
		ICElement current = this;
		do {
			if (current instanceof ICModel) return (ICModel) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	public ICProject getCProject() {
		ICElement current = this;
		do {
			if (current instanceof ICProject) return (ICProject) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	protected void addChild(ICElement e) throws CModelException {
	}

	public IResource getUnderlyingResource() {
		IResource res = getResource();
		if (res == null) {
			ICElement p = getParent();
			if (p != null) {
				res = p.getUnderlyingResource();
			}
		}
		return res;
	}

	public abstract IResource getResource() ;

	protected abstract CElementInfo createElementInfo();

	/**
	 * Tests if an element has the same name, type and an equal parent.
	 */
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o instanceof ICElement) {
			return equals(this, (ICElement) o);
		}
		return false;
	}

	public static boolean equals(ICElement lhs, ICElement rhs) {
		if (lhs.getElementType() != rhs.getElementType()) {
			return false;
		}
		String lhsName= lhs.getElementName();
		String rhsName= rhs.getElementName();
		if( lhsName == null || rhsName == null || lhsName.length() == 0 ||
				!lhsName.equals(rhsName)) {
			return false;
		}

		ICElement lhsParent= lhs.getParent();
		ICElement rhsParent= rhs.getParent();
		if (lhsParent == rhsParent) {
			return true;
		}

		return lhsParent != null && lhsParent.equals(rhsParent);
	}

	public String toString() {
		return getElementName();
	}

	public String toDebugString() {
			return getElementName() + " " + getTypeString(); //$NON-NLS-1$
	}

	// util
	public String getTypeString() {
		switch (getElementType()) {
			case C_MODEL:
				return "CMODEL";  //$NON-NLS-1$
			case C_PROJECT:
				return "CPROJECT";  //$NON-NLS-1$
			case C_CCONTAINER:
				if (this instanceof ISourceRoot) {
					return "SOURCE_ROOT"; //$NON-NLS-1$
				}
				return "CCONTAINER"; //$NON-NLS-1$
			case C_UNIT:
				if (this instanceof IWorkingCopy) {
					return "WORKING_UNIT";  //$NON-NLS-1$
				}
				return "TRANSLATION_UNIT";  //$NON-NLS-1$
			case C_FUNCTION:
				return "C_FUNCTION";  //$NON-NLS-1$
			case C_FUNCTION_DECLARATION:
				return "C_FUNCTION_DECLARATION";  //$NON-NLS-1$
			case C_VARIABLE:
				return "C_VARIABLE";  //$NON-NLS-1$
			case C_VARIABLE_DECLARATION:
				return "C_VARIABLE_DECLARATION";  //$NON-NLS-1$
			case C_INCLUDE:
				return "C_INCLUDE";  //$NON-NLS-1$
			case C_MACRO:
				return "C_MACRO"; 			 //$NON-NLS-1$
			case C_STRUCT:
				return "C_STRUCT"; //$NON-NLS-1$
			case C_CLASS:
				return "C_CLASS"; //$NON-NLS-1$
			case C_UNION:
				return "C_UNION"; //$NON-NLS-1$
			case C_FIELD:
				return "C_FIELD";  //$NON-NLS-1$
			case C_METHOD:
				return "C_METHOD"; 						 //$NON-NLS-1$
			case C_NAMESPACE:
				return "C_NAMESPACE"; 						 //$NON-NLS-1$
			case C_USING:
				return "C_USING"; 						 //$NON-NLS-1$
			case C_VCONTAINER:
				return "C_CONTAINER"; //$NON-NLS-1$
			case C_BINARY:
				return "C_BINARY"; //$NON-NLS-1$
			case C_ARCHIVE:
				return "C_ARCHIVE"; //$NON-NLS-1$
			default:
				return "UNKNOWN"; //$NON-NLS-1$
		}
	}

	/**
	 * Close the C Element
	 * @throws CModelException
	 */
	public void close() throws CModelException {
	}

	/**
	 * This element is being closed.  Do any necessary cleanup.
	 */
	protected void closing(Object info) throws CModelException {
	}

	/**
	 * This element has just been opened.  Do any necessary setup.
	 */
	protected void opening(Object info) {
	}

	/**
	 * Return the first instance of IOpenable in the parent
	 * hierarchy of this element.
	 *
	 * <p>Subclasses that are not IOpenable's must override this method.
	 */
	public IOpenable getOpenableParent() {
		if(fParent instanceof IOpenable)
      {
			return (IOpenable)fParent;
		}
		return null;
	}

	/**
	 * Builds this element's structure and properties in the given
	 * info object, based on this element's current contents (i.e. buffer
	 * contents if this element has an open buffer, or resource contents
	 * if this element does not have an open buffer). Children
	 * are placed in the given newElements table (note, this element
	 * has already been placed in the newElements table). Returns true
	 * if successful, or false if an error is encountered while determining
	 * the structure of this element.
	 */
	protected abstract void generateInfos(Object info, Map newElements, IProgressMonitor monitor) throws CModelException;


	/**
	 * @see ICElement
	 */
	public ICElement getAncestor(int ancestorType) {
		ICElement element = this;
		while (element != null) {
			if (element.getElementType() == ancestorType) {
				 return element;
			}
			element= element.getParent();
		}
		return null;
	}

	/**
	 * Returns true if this element is an ancestor of the given element,
	 * otherwise false.
	 */
	public boolean isAncestorOf(ICElement e) {
		ICElement parent= e.getParent();
		while (parent != null && !parent.equals(this)) {
			parent= parent.getParent();
		}
		return parent != null;
	}

	/**
	 * Creates and returns and not present exception for this element.
	 */
	protected CModelException newNotPresentException() {
		return new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * Returns the hash code for this Java element. By default,
	 * the hash code for an element is a combination of its name
	 * and parent's hash code. Elements with other requirements must
	 * override this method.
	 */
	// CHECKPOINT: making not equal objects seem equal
	// What elements should override this?
	public int hashCode() {
		return hashCode(this);
	}

	public static int hashCode(ICElement elem) {
		ICElement parent= elem.getParent();
		if (parent == null) {
			return System.identityHashCode(elem);
		}
		return Util.combineHashCodes(elem.getElementName().hashCode(), parent.hashCode());
	}

	/*
	 * Test to see if two objects are identical
	 * Subclasses should override accordingly
	 */
	public boolean isIdentical(CElement otherElement){
		return this.equals(otherElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#accept(org.eclipse.cdt.core.model.ICElementVisitor)
	 */
	public void accept(ICElementVisitor visitor) throws CoreException {
		// Visit me, return right away if the visitor doesn't want to visit my children
		if (!visitor.visit(this))
			return;

		// If I am a Parent, visit my children
		if (this instanceof IParent) {
			ICElement [] children = ((IParent)this).getChildren();
			for (int i = 0; i < children.length; ++i)
				children[i].accept(visitor);
		}
	}

}
