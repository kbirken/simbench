package org.nanosite.simbench.injection.analyser.hbsim;

import java.text.DecimalFormat;

import org.nanosite.simbench.hbsim.Action;
import org.nanosite.simbench.hbsim.HbsimFactory;
import org.nanosite.simbench.hbsim.VP_Expr;
import org.nanosite.simbench.injection.hbinj.InjAction;
import org.nanosite.simbench.injection.hbinj.InjDetailsIO;
import org.nanosite.simbench.injection.hbinj.InjIOActivity;


public class ActionInjector extends InjectorBase {

	public boolean containsData (InjAction actionInj) {
		if (actionInj==null) {
			return false;
		}
		if (! actionInj.getConsumedCPU().isEmpty()) {
			return true;
		}
		if (! actionInj.getIo().isEmpty()) {
			return true;
		}
		return false;
	}

	public void inject (InjAction actionInj, Action action) {
		log("       inject action: cpu=" + actionInj.getConsumedCPU());

		DecimalFormat ft = getDecimalFormat();
		ft.setDecimalFormatSymbols(null);
		if (action==null) {
			// TODO: this should be more generic :-)
			double resIgnored = 0.0;
			double resIFS = 0.0;
			double resNAND0 = 0.0;
			double resNAND1 = 0.0;
			double resHDD = 0.0;
			for(InjIOActivity ioInj : actionInj.getIo()) {
				Float amount = Float.valueOf(ioInj.getAmount());
				InjDetailsIO ioDetailsInj = ioInj.getDetails();
				if (ioDetailsInj==null) {
					warning(ioInj, "no details available, data will be ignored");
					resIgnored += amount;
				} else {
					String file = ioDetailsInj.getFile();
					if (file==null) {
						warning(ioInj, "no file name given, data will be ignored");
						resIgnored += amount;
					} else {
						if (file.startsWith("/boot/lib") || file.startsWith("/boot/bin")) {
							resIFS += amount;
						} else if (file.startsWith("/fs/sda0")) {
							resNAND0 += amount;
						} else if (file.startsWith("/fs/sda1")) {
							resNAND1 += amount;
						} else if (file.startsWith("/mnt/data")) {
							resHDD += amount;
						} else {
							warning(ioInj, "resource could not be determined for " + file);
							resIgnored += amount;
						}
					}
				}
			}

			if (resIgnored>0) {
				log("\t\t\t//read ignored: " + ft.format(resIgnored) + ";");
			}
			if (resIFS>0) {
				log("\t\t\t//read ifs_image: " + ft.format(resIFS) + " via IFS;");
			}
			if (resNAND0>0) {
				log("\t\t\tread sda0: " + ft.format(resNAND0) + " via NAND_Flash.standard;");
			}
			if (resNAND1>0) {
				log("\t\t\tread sda1: " + ft.format(resNAND1) + " via NAND_Flash.standard;");
			}
			if (resHDD>0) {
				log("\t\t\tread data: " + ft.format(resHDD) + " via HDD.standard;");
			}
		} else {
			VP_Expr vpExpr = action.getConsumedCPU();
			if (vpExpr==null) {
				// new VP_Expr object must be created
				HbsimFactory factory = HbsimFactory.eINSTANCE;
				vpExpr = factory.createVP_Expr();
				action.setConsumedCPU(vpExpr);
				touch(action);
			}
			VPExprInjector injector = new VPExprInjector();
			injector.inject(actionInj.getConsumedCPU(), vpExpr);
		}
	}
}
