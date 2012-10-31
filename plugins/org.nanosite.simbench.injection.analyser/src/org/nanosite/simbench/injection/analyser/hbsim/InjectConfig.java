package org.nanosite.simbench.injection.analyser.hbsim;

import org.nanosite.feamo.hbfm.Feature;

public interface InjectConfig {
	InjectConfig eINSTANCE = InjectConfigImpl.init();

	void setFeature(Feature f);

	Feature getFeature();
}
