package org.nanosite.simbench.injection.analyser.hbsim;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;
import org.nanosite.feamo.HbfmStandaloneSetup;
import org.nanosite.feamo.hbfm.Feature;
import org.nanosite.feamo.hbfm.FeatureDef;
import org.nanosite.feamo.hbfm.FeatureDetails;
import org.nanosite.feamo.hbfm.FeatureGroup;
import org.nanosite.feamo.hbfm.FeatureModel;
import org.nanosite.feamo.hbfm.HbfmFactory;
import org.nanosite.feamo.hbfm.SimpleFeature;
import org.nanosite.simbench.HbsimModelAccessor;
import org.nanosite.simbench.HbsimStandaloneSetup;
import org.nanosite.simbench.hbsim.Model;
import org.nanosite.simbench.injection.HbinjStandaloneSetup;
import org.nanosite.simbench.injection.hbinj.InjModel;


public class SomeModelReader {

	private static String FEATURE_MEASURE = "Measure";

	private XtextResourceSet resourceSet = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SomeModelReader myself = new SomeModelReader();
		myself.inject();
	}

	private void inject() {
		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("D:/P4_simbench/hbsim/imp/plugins");

		//Model model = readHbsimModel("platform:/resource/org.nanositebecker.simbench.hbsim_tools.generator/src/example_model_ntg5/ntg5_main.hbsim");
		//readInjectModel("platform:/resource/org.nanositebecker.simbench.hbsim_inject.ui/src/examples/trace_arm_100210.kev6.hbinj", model);

		//Model model = readHbsimModel("platform:/resource/org.nanositebecker.simbench.hbsim_tools.generator/src/example_model_ntg5/simple.hbsim");
		//readInjectModel("platform:/resource/org.nanositebecker.simbench.hbsim_inject.ui/src/examples/simple.hbinj", model);

		readInjectModel("platform:/resource/org.nanositebecker.simbench.hbsim_inject.ui/src/examples/tracebuffer_intel_1.merged.kev3.hbinj", null);

		if (resourceSet != null) {
			saveModifiedResources();
		}
	}


	private Model readHbsimModel (String uri) {
		/*Injector injectorFM = */ new HbfmStandaloneSetup().createInjectorAndDoEMFRegistration();
		Injector injector = new HbsimStandaloneSetup().createInjectorAndDoEMFRegistration();
		resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		Resource resource = resourceSet.getResource(URI.createURI(uri), true);

		Model model = (Model) resource.getContents().get(0);

		// find "Measure" feature
		FeatureModel fm = HbsimModelAccessor.getFeatureModel(model);
		Feature fMeasure = null;
		if (fm==null) {
			System.out.println("Warning: Cannot find feature model!");

			// workaround: we create our own FeatureModel
			HbfmFactory factory = HbfmFactory.eINSTANCE;
			fMeasure = factory.createFeature();
			fMeasure.setName(FEATURE_MEASURE);
		} else {
			fMeasure = getFeature(fm, FEATURE_MEASURE);
		}
		if (fMeasure==null) {
			// should never happen
			System.out.println("Warning: Could not create feature '" + FEATURE_MEASURE + "'!");
		} else {
			// set "Measure" feature for later use
			InjectConfig.eINSTANCE.setFeature(fMeasure);
		}

		return model;

		/*
		// activate modification tracking for all resources
		for(Resource res : resourceSet.getResources()) {
			res.setTrackingModification(true);
		}
		*/
	}


	private void readInjectModel (String uri, Model model) {
		Injector injector = new HbinjStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		Resource resource = resourceSet.getResource(URI.createURI(uri), true);

		InjModel modelInj = (InjModel) resource.getContents().get(0);
		ModelInjector modelInjector = new ModelInjector();
		modelInjector.inject(modelInj, model);
	}


	private void saveModifiedResources () {
		//IHiddenTokenMerger

		System.out.println("saving modified resources ...");
		for(Resource res : resourceSet.getResources()) {
			if (res.isModified()) {
				System.out.println("  file " + res.getURI().toFileString());
				System.out.println("  plat " + res.getURI().toPlatformString(false));

				System.out.println("  saving resource " + res.getURI().lastSegment() + " ...");
			    HashMap<String,Boolean> saveOptions = new HashMap<String,Boolean>();
			    //saveOptions.put(XtextResource.OPTION_FORMAT, Boolean.TRUE);
			    try {
			    	//OutputStream os = resourceSet.getURIConverter().createOutputStream(res.getURI());
					res.save(saveOptions);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("ready.");
	}


	private Feature getFeature (FeatureModel fm, String featureName) {
		Feature f1 = getFeature(fm.getDetails(), featureName);
		if (f1!=null) {
			return f1;
		}
		for (FeatureDef fdef : fm.getDefs()) {
			Feature f = getFeature(fdef.getDetails(), featureName);
			if (f!=null) {
				return f;
			}
		}
		return null;
	}


	private Feature getFeature (FeatureDetails fd, String featureName) {
		for (FeatureGroup fg : fd.getGroups()) {
			if (fg.getSimple()!=null) {
				Feature f = feature(fg.getSimple());
				if (f.getName().equals(featureName)) {
					return f;
				}
			} else {
				Iterator<SimpleFeature> i = fg.getAlt().iterator();
				if (! fg.getOr().isEmpty()) {
					i = fg.getOr().iterator();
				}
				for (; i.hasNext();) {
					Feature f = feature(i.next());
					if (f.getName().equals(featureName)) {
						return f;
					}
				}
			}
		}

		return null;
	}

	private static Feature feature (SimpleFeature sf) {
		if (sf.getMandatory()!=null) {
			return sf.getMandatory();
		}
		return sf.getOptional();
	}
}
