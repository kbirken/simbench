package org.nanosite.feamo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nanosite.feamo.hbfm.Feature;
import org.nanosite.feamo.hbfm.FeatureDef;
import org.nanosite.feamo.hbfm.FeatureDetails;
import org.nanosite.feamo.hbfm.FeatureGroup;
import org.nanosite.feamo.hbfm.FeatureModel;
import org.nanosite.feamo.hbfm.SimpleFeature;

public class HbfmHelper {

	static public List<Feature> getAllFeatures (FeatureModel fm)
	{
		List<Feature> all = getAllFeatures(fm.getDetails());
		for (FeatureDef fdef : fm.getDefs()) {
			all.addAll(getAllFeatures(fdef.getDetails()));
		}
		return all;
	}


	// *****************************************************************************

	static private List<Feature> getAllFeatures (FeatureDetails fd)
	{
		List<Feature> all = new ArrayList<Feature>(50);
		for (FeatureGroup fg : fd.getGroups()) {
			if (fg.getSimple()!=null) {
				all.add(feature(fg.getSimple()));
			} else {
				Iterator<SimpleFeature> i = fg.getAlt().iterator();
				if (! fg.getOr().isEmpty()) {
					i = fg.getOr().iterator();
				}
				for (; i.hasNext();) {
					all.add(feature(i.next()));
				}
			}
		}

		return all;
	}

	static private Feature feature (SimpleFeature sf)
	{
		if (sf.getMandatory()!=null) {
			return sf.getMandatory();
		}
		return sf.getOptional();
	}

}
