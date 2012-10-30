package org.nanosite.feamo.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import org.nanosite.feamo.hbfm.FeatureConstraint;
import org.nanosite.feamo.hbfm.FeatureModel;


public class HbfmJavaValidator extends AbstractHbfmJavaValidator implements ValidationMessageReporter {
	FeatureModelValidator validator = null;

	public HbfmJavaValidator () {
		validator = new FeatureModelValidator(this);
	}

	@Check
	public void checkSelfRestriction(FeatureConstraint c) {
		validator.checkSelfRestriction(c);
	}

	@Check
	public void checkFeatureModel(FeatureModel fm) {
		validator.checkFeatureModel(fm);
	}

	// *****************************************************************************

	// ValidationMessageReporter interface
	public void reportError (String message, EObject object, EStructuralFeature feature) {
		error(message, object, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}
	public void reportWarning (String message, EObject object, EStructuralFeature feature) {
		warning(message, object, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}
}
