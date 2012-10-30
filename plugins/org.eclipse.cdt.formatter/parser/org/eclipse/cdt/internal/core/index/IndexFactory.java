/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Markus Schorn - initial API and implementation Andrew Ferguson
 * (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
/**
 * Class that creates indexes based on PDOMs
 * @since 4.0
 */
public class IndexFactory {
	private static final int ADD_DEPENDENCIES = IIndexManager.ADD_DEPENDENCIES;
	private static final int ADD_DEPENDENT = IIndexManager.ADD_DEPENDENT;
	private static final int SKIP_PROVIDED = IIndexManager.SKIP_PROVIDED;

	private Object fPDOMManager;

	public IndexFactory(Object manager) {
		fPDOMManager= manager;
	}

	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		projects = (ICProject[]) ArrayUtil.removeNulls(ICProject.class, projects);

		boolean addDependencies= (options & ADD_DEPENDENCIES) != 0;
		boolean addDependent=    (options & ADD_DEPENDENT) != 0;
		boolean skipProvided= (options & SKIP_PROVIDED) != 0;

		HashMap map= new HashMap();
		Collection selectedProjects= getProjects(projects, addDependencies, addDependent, map, new Integer(1));

		HashMap fragments= new LinkedHashMap();
		for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
			ICProject cproject = (ICProject) iter.next();
			IIndexFragment pdom= null;//fPDOMManager.getPDOM(cproject);
			if (pdom != null) {
				safeAddFragment(fragments, pdom);

				if(!skipProvided) {
					safeAddProvidedFragments(cproject, fragments);
				}
			}
		}
		if (fragments.isEmpty()) {
			return EmptyCIndex.INSTANCE;
		}

		int primaryFragmentCount= fragments.size();

		if (!addDependencies) {
			projects= (ICProject[]) selectedProjects.toArray(new ICProject[selectedProjects.size()]);
			selectedProjects.clear();
			// don't clear the map, so projects are not selected again
			selectedProjects= getProjects(projects, true, false, map, new Integer(2));
			for (Iterator iter = selectedProjects.iterator(); iter.hasNext(); ) {
				ICProject cproject = (ICProject) iter.next();
				IIndexFragment pdom= null;//fPDOMManager.getPDOM(cproject);
				safeAddFragment(fragments, pdom);

				if(!skipProvided) {
					safeAddProvidedFragments(cproject, fragments);
				}
			}
		}

		Collection pdoms= fragments.values();
		return new CIndex(
            (IIndexFragment[])pdoms.toArray(new IIndexFragment[pdoms.size()]),
            primaryFragmentCount);
	}

	public IWritableIndex getWritableIndex(ICProject project) throws CoreException
   {
		return null;
	}

	private Collection getProjects(ICProject[] projects, boolean addDependencies, boolean addDependent, HashMap map, Integer markWith) {
		return new ArrayList();
	}

	private void checkAddProject(IProject project, HashMap map, List projectsToSearch, Integer markWith) {
		if (map.get(project) == null) {
			if (project.isOpen()) {
				map.put(project, markWith);
				projectsToSearch.add(project);
			}
			else {
				map.put(project, new Integer(0));
			}
		}
	}

	/**
	 * Add an entry for the specified fragment. This copes with problems occurring when reading
	 * the fragment ID.
	 * @param id2fragment the map to add the entry to
	 * @param fragment the fragment or null (which will result in no action)
	 */
	private void safeAddFragment(Map id2fragment, IIndexFragment fragment) {
		if(fragment!=null) {
			try {
				fragment.acquireReadLock();
				try {
					String id= fragment.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
					id2fragment.put(id, fragment);
				} finally {
					fragment.releaseReadLock();
				}

			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			} catch(InterruptedException ie) {
				CCorePlugin.log(ie);
			}
		}
	}

	/**
	 * Adds ID -> IIndexFragment entries to the specified Map, for fragments provided under the
	 * CIndex extension point for the specified ICProject
	 * @param cproject
	 * @param fragments
	 */
	private void safeAddProvidedFragments(ICProject cproject, Map fragments) {
	}
}
