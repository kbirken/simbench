/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.simbench.hbsim.exported;

import java.io.InputStream;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.xmi.impl.XMLHandler;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.util.Strings;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class XMLEncodingProvider implements IEncodingProvider {

	private static final int BUFFER_SIZE = 512;

	private static final Logger LOG = Logger
			.getLogger(XMLEncodingProvider.class.getName());

	public String getEncoding(URI uri) {
		try {
			InputStream inputStream = URIConverter.INSTANCE
					.createInputStream(uri);
			byte[] buffer = new byte[BUFFER_SIZE];
			inputStream.read(buffer);
			return XMLHandler.getXMLEncoding(buffer);
		} catch (Exception e) {
			LOG.severe("Error detecting encoding for " + Strings.notNull(uri)
					+ " (" + e.toString() + ")");
			return null;
		}
	}
}
