package org.nanosite.feamo.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class ValidationHelpers {

	public static int checkDuplicates(ValidationMessageReporter reporter,
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
				reporter.reportError("Duplicate name " + name, i, feature);
				nErrors++;
			} else {
				firstOccurrenceOfName.put(name, i);
			}
		}

		// now create the error for the first occurrence of a duplicate name
		for (String s : duplicateNames) {
			reporter.reportError("Duplicate name " + s, firstOccurrenceOfName.get(s), feature);
			nErrors++;
		}

		return nErrors;
	}


}
