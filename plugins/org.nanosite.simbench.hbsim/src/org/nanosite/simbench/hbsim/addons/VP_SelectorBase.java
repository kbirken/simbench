package org.nanosite.simbench.hbsim.addons;

import org.eclipse.emf.common.util.EList;

import org.nanosite.simbench.hbsim.VP_Selectable;

public interface VP_SelectorBase {
	  abstract <T extends VP_Selectable> T getMust();
	  abstract <T extends VP_Selectable> EList<T> getAlt();
}
