package org.nanosite.feamo.validation;

import org.nanosite.feamo.hbfm.*;

import java.util.Comparator;

public class FeatureDefNameComparator implements Comparator<FeatureDef> {

	public int compare(FeatureDef fd1, FeatureDef fd2) {
		return fd1.getFeature().getName().compareTo(fd2.getFeature().getName());
	}
}
