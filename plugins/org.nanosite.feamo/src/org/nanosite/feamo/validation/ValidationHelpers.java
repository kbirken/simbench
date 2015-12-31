/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.feamo.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

public class ValidationHelpers {

	public static int checkDuplicates(ValidationMessageAcceptor reporter,
			Map<EObject, String> items, EStructuralFeature feature)
	{
		int nErrors = 0;

		// for each name store the element of its first occurrence
		Map<String, EObject> firstOccurrenceOfName = new HashMap<String, EObject>();
		Set<String> duplicateNames = new HashSet<String>();

		// iterate (once!) over all types in the model
		for (EObject i : items.keySet()) {
			String name = items.get(i);
			// if the name already occurred we have a duplicate name and hence an error
			if (firstOccurrenceOfName.get(name) != null) {
				duplicateNames.add(name);
				reporter.acceptError("Duplicate name " + name, i, feature,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
				nErrors++;
			} else {
				firstOccurrenceOfName.put(name, i);
			}
		}

		// now create the error for the first occurrence of a duplicate name
		for (String s : duplicateNames) {
			reporter.acceptError("Duplicate name " + s, firstOccurrenceOfName.get(s), feature,
					ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
			nErrors++;
		}

		return nErrors;
	}


}
