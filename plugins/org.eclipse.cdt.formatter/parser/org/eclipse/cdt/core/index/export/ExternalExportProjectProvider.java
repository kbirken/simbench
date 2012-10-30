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
package org.eclipse.cdt.core.index.export;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

/**
 * An IExportProjectProvider suitable for indexing an external folder. The arguments understood by this provider
 * are
 * <ul>
 * <li>-source what will become the root of the indexed content
 * <li>-include any preinclude files to configure the parser with
 * <li>-id the id to write to the produce fragment
 * </ul>
 */
public class ExternalExportProjectProvider extends AbstractExportProjectProvider implements IExportProjectProvider {
	private static final String PREBUILT_PROJECT_OWNER = "org.eclipse.cdt.core.index.export.prebuiltOwner"; //$NON-NLS-1$
	private static final String ORG_ECLIPSE_CDT_CORE_INDEX_EXPORT_DATESTAMP = "org.eclipse.cdt.core.index.export.datestamp"; //$NON-NLS-1$
	private static final String CONTENT = "content"; //$NON-NLS-1$
	public static final String OPT_SOURCE = "-source"; //$NON-NLS-1$
	public static final String OPT_INCLUDE = "-include"; //$NON-NLS-1$
	public static final String OPT_FRAGMENT_ID = "-id"; //$NON-NLS-1$

	private IFolder content;
	private String fragmentId;

	public ExternalExportProjectProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IProjectForExportManager#createProject(java.util.Map)
	 */
	public ICProject createProject() throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#getLocationConverter(org.eclipse.cdt.core.model.ICProject)
	 */
	public IIndexLocationConverter getLocationConverter(final ICProject cproject) {
		return new ResourceContainerRelativeLocationConverter(content);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#getExportProperties()
	 */
	public Map getExportProperties() {
		Map properties= new HashMap();
		Date now= Calendar.getInstance().getTime();
		properties.put(ORG_ECLIPSE_CDT_CORE_INDEX_EXPORT_DATESTAMP,
				DateFormat.getDateInstance().format(now)
				+" "+DateFormat.getTimeInstance().format(now)); //$NON-NLS-1$
		properties.put(IIndexFragment.PROPERTY_FRAGMENT_ID, fragmentId);
		return properties;
	}
}
