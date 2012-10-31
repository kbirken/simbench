package org.nanosite.simbench.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface ValidationMessageReporter {
	void reportError(String message, EObject object, EStructuralFeature feature);
	void reportWarning(String message, EObject object, EStructuralFeature feature);
}
