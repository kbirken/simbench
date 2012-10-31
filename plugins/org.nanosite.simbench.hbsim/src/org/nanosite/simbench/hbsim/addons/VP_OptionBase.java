package org.nanosite.simbench.hbsim.addons;

import org.nanosite.simbench.hbsim.VP_Optional;

public interface VP_OptionBase {
	  abstract <T extends VP_Optional> T getOpt();
}
