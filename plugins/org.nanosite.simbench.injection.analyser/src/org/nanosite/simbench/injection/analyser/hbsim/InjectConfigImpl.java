package org.nanosite.simbench.injection.analyser.hbsim;

import org.nanosite.feamo.hbfm.Feature;

public class InjectConfigImpl implements InjectConfig {

	private Feature feature = null;

	public static InjectConfigImpl init() {
		return new InjectConfigImpl();

	}

	public void setFeature(Feature f) {
		feature = f;
	}

	public Feature getFeature() {
		return feature;
	}
}
