package org.nanosite.feamo.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import org.nanosite.feamo.HbfmHelper;
import org.nanosite.feamo.hbfm.*;


public class FeatureModelValidator {
	ValidationMessageReporter reporter = null;

	public FeatureModelValidator (ValidationMessageReporter reporter) {
		this.reporter = reporter;
	}

	// *****************************************************************************

	public void checkFeatureModel (FeatureModel fm) {
		checkFeatureNamesAreUnique(fm);
		checkFeatureDefsAreUnique(fm);
	}

	public void checkSelfRestriction (FeatureConstraint c) {
		if (c.getFeature1().getName().equals(c.getFeature2().getName())) {
			reporter.reportError("Features must be distinct in constraint", c,
					HbfmPackage.Literals.FEATURE_CONSTRAINT__FEATURE1);
		}
	}

	// *****************************************************************************

	public void checkFeatureNamesAreUnique(FeatureModel fm) {
		List<Feature> all = HbfmHelper.getAllFeatures(fm);
		Map<EObject, String> names = new HashMap<EObject, String>();
		for (Feature i : all) {
			names.put(i, i.getName());
		}
		ValidationHelpers.checkDuplicates(reporter, names,
				HbfmPackage.Literals.FEATURE__NAME);
	}


	public void checkFeatureDefsAreUnique (FeatureModel fm) {
		Map<EObject, String> names = new HashMap<EObject, String>();
		for (FeatureDef i : fm.getDefs()) {
			names.put(i, i.getFeature().getName());
		}
		ValidationHelpers.checkDuplicates(reporter, names,
				HbfmPackage.Literals.FEATURE_DEF__FEATURE);
	}

	/* not used
	// visit all FeatureDefs for fd recursively and remove visited from fdefs along the way
	private void visitFeatureDefs (FeatureDetails fd, List<FeatureDef> fdefs) {
		List<Feature> reachable = getAllFeatures(fd);
		for(Feature f : reachable) {
			List<FeatureDef> del = new ArrayList<FeatureDef>(10);
			for(FeatureDef fdef : fdefs) {
				if (fdef.getName().getName().equals(f.getName())) {
					del.add(fdef);
				}
			}
			fdefs.removeAll(del);
			for(FeatureDef fdef : del) {
				visitFeatureDefs(fdef.getDetails(), fdefs);
			}
		}
	}
	*/
}
