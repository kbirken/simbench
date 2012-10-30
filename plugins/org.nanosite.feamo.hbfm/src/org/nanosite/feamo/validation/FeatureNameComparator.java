package org.nanosite.feamo.validation;

import org.nanosite.feamo.hbfm.*;

import java.util.Comparator;

public class FeatureNameComparator implements Comparator<Feature> {

	public int compare(Feature f1, Feature f2) {
		return f1.getName().compareTo(f2.getName());
	}
}
