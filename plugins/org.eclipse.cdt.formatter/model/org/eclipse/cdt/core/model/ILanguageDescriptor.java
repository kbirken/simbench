/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.ICExtensionDescriptor;
import org.eclipse.core.runtime.content.IContentType;

public interface ILanguageDescriptor extends ICExtensionDescriptor {
	ILanguage getLanguage();
	
	String[] getContentTypeIds();

	IContentType[] getContentTypes();
}
