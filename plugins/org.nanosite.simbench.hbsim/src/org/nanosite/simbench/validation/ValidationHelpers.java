// TODO: this class is not depending on a particular DSL, should be factored to a common helper package
package org.nanosite.simbench.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class ValidationHelpers {

	// simple helper for finding duplicates in ELists
	public static <T extends EObject> int checkDuplicates(
			ValidationMessageReporter reporter,
			Iterable<T> items,
			EStructuralFeature feature,
			String description )
	{
		int nErrors = 0;
		String msg = "Duplicate " + description + ' ';

		// for each name store the element of its first occurrence
		Map<String, EObject> firstOccurrenceOfName = new HashMap<String, EObject>();
		Set<String> duplicateNames = new HashSet<String>();

		// iterate (once!) over all types in the model
		for (EObject i : items) {
			String name = i.eGet(feature).toString();

			// if the name already occurred we have a duplicate name and hence an error
			if (firstOccurrenceOfName.get(name) != null) {
				duplicateNames.add(name);
				reporter.reportError(msg + name, i, feature);
				nErrors++;
			} else {
				firstOccurrenceOfName.put(name, i);
			}
		}

		// now create the error for the first occurrence of a duplicate name
		for (String s : duplicateNames) {
			reporter.reportError(msg + s, firstOccurrenceOfName.get(s), feature);
			nErrors++;
		}

		return nErrors;
	}

	// more complex helper for finding duplicates in arbitrary sets of EObjects
	// (the EObject names must be determined by the caller)
	public static int checkDuplicates(ValidationMessageReporter reporter,
			Map<EObject, String> items,
			EStructuralFeature feature,
			String description)
	{
		int nErrors = 0;
		String msg = "Duplicate " + description + ' ';

		// for each name store the element of its first occurrence
		Map<String, EObject> firstOccurrenceOfName = new HashMap<String, EObject>();
		Set<String> duplicateNames = new HashSet<String>();

		// iterate (once!) over all types in the model
		for (EObject i : items.keySet()) {
			String name = items.get(i);
			// if the name already occurred we have a duplicate name and hence an error
			if (firstOccurrenceOfName.get(name) != null) {
				duplicateNames.add(name);
				reporter.reportError(msg + name, i, feature);
				nErrors++;
			} else {
				firstOccurrenceOfName.put(name, i);
			}
		}

		// now create the error for the first occurrence of a duplicate name
		for (String s : duplicateNames) {
			reporter.reportError(msg + s, firstOccurrenceOfName.get(s), feature);
			nErrors++;
		}

		return nErrors;
	}
}
