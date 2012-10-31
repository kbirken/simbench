package org.nanosite.simbench;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.nanosite.feamo.hbfm.FeatureModel;
import org.nanosite.simbench.hbsim.*;

/**
 * Provides transparent access to hbsim models which have been distributed onto multiple resources.
 */
public class HbsimModelAccessor {

	public static FeatureModel getFeatureModel (Model main) {
		ArrayList<FeatureModel> items = getAllElements(main, FeatureModel.class);
		if (items.size()!=1) {
			return null;
		}
		return items.get(0);
	}

	public static List<FeatureConfig> getConfigs (Model main) {
		return getAllElements(main, FeatureConfig.class);
	}

	public static List<VP_Element> getVPElements (Model main) {
		return getAllElements(main, VP_Element.class);
	}

	// ****

	private static <T> ArrayList<T> getAllElements (EObject elemInRootResource, Class<T> clazz) {
		ResourceSet resourceSet = elemInRootResource.eResource().getResourceSet();
		Iterator<Object> unfiltered = EcoreUtil.getAllContents(resourceSet, true);
		Iterator<T> filtered = Iterators.filter(unfiltered, clazz);
		return Lists.newArrayList(filtered);
	}
}
