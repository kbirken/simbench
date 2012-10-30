/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core;

import org.eclipse.core.runtime.Plugin;

/**
 * CCorePlugin is the life-cycle owner of the core plug-in, and starting point for access to many core APIs.
 */
public class CCorePlugin extends Plugin
{

	public static final int STATUS_CDTPROJECT_EXISTS = 1;
	public static final int STATUS_CDTPROJECT_MISMATCH = 2;
	public static final int CDT_PROJECT_NATURE_ID_MISMATCH = 3;

	public static final String PLUGIN_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$

	public static final String BUILDER_MODEL_ID = PLUGIN_ID + ".CBuildModel"; //$NON-NLS-1$
	public static final String BINARY_PARSER_SIMPLE_ID = "BinaryParser"; //$NON-NLS-1$
	public final static String BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + BINARY_PARSER_SIMPLE_ID; //$NON-NLS-1$
	public final static String PREF_BINARY_PARSER = "binaryparser"; //$NON-NLS-1$
	public final static String DEFAULT_BINARY_PARSER_SIMPLE_ID = "ELF"; //$NON-NLS-1$
	public final static String DEFAULT_BINARY_PARSER_UNIQ_ID = PLUGIN_ID + "." + DEFAULT_BINARY_PARSER_SIMPLE_ID; //$NON-NLS-1$
	public final static String PREF_USE_STRUCTURAL_PARSE_MODE = "useStructualParseMode"; //$NON-NLS-1$

	public static final String INDEX_SIMPLE_ID = "CIndex"; //$NON-NLS-1$
	public static final String INDEX_UNIQ_ID = PLUGIN_ID + "." + INDEX_SIMPLE_ID; //$NON-NLS-1$

	public static final String INDEXER_SIMPLE_ID = "CIndexer"; //$NON-NLS-1$
	public static final String INDEXER_UNIQ_ID = PLUGIN_ID + "." + INDEXER_SIMPLE_ID; //$NON-NLS-1$
	public static final String PREF_INDEXER = "indexer"; //$NON-NLS-1$

	public final static String ERROR_PARSER_SIMPLE_ID = "ErrorParser"; //$NON-NLS-1$
	public final static String ERROR_PARSER_UNIQ_ID = PLUGIN_ID + "." + ERROR_PARSER_SIMPLE_ID; //$NON-NLS-1$

	// default store for pathentry
	public final static String DEFAULT_PATHENTRY_STORE_ID = PLUGIN_ID + ".cdtPathEntryStore"; //$NON-NLS-1$

	// Build Model Interface Discovery
	public final static String BUILD_SCANNER_INFO_SIMPLE_ID = "ScannerInfoProvider"; //$NON-NLS-1$
	public final static String BUILD_SCANNER_INFO_UNIQ_ID = PLUGIN_ID + "." + BUILD_SCANNER_INFO_SIMPLE_ID; //$NON-NLS-1$

	public static final String DEFAULT_PROVIDER_ID = CCorePlugin.PLUGIN_ID + ".defaultConfigDataProvider"; //$NON-NLS-1$

	/**
	 * Name of the extension point for contributing a source code formatter
	 */
	public static final String FORMATTER_EXTPOINT_ID = "CodeFormatter" ; //$NON-NLS-1$

    /**
     * Possible  configurable option ID.
     * @see #getDefaultOptions
     */
    public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$

	/**
	 * IContentType id for C Source Unit
	 */
	public final static String CONTENT_TYPE_CSOURCE =  "org.eclipse.cdt.core.cSource"; //$NON-NLS-1$
	/**
	 * IContentType id for C Header Unit
	 */
	public final static String CONTENT_TYPE_CHEADER =  "org.eclipse.cdt.core.cHeader"; //$NON-NLS-1$
	/**
	 * IContentType id for C++ Source Unit
	 */
	public final static String CONTENT_TYPE_CXXSOURCE = "org.eclipse.cdt.core.cxxSource"; //$NON-NLS-1$
	/**
	 * IContentType id for C++ Header Unit
	 */
	public final static String CONTENT_TYPE_CXXHEADER = "org.eclipse.cdt.core.cxxHeader"; //$NON-NLS-1$
	/**
	 * IContentType id for ASM Unit
	 */
	public final static String CONTENT_TYPE_ASMSOURCE = "org.eclipse.cdt.core.asmSource"; //$NON-NLS-1$
	/**
	 * IContentType id for Binary Files
	 */
	public final static String CONTENT_TYPE_BINARYFILE = "org.eclipse.cdt.core.binaryFile"; //$NON-NLS-1$

	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String INSERT = "insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String TAB = "tab"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String SPACE = "space"; //$NON-NLS-1$
   public static void log(Exception e)
   {

   }
   public static void log(String msg)
   {

   }
}