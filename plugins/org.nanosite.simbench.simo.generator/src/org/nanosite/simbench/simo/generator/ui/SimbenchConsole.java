package org.nanosite.simbench.simo.generator.ui;

import org.eclipse.swt.widgets.Shell;

import org.nanosite.common.util.ui.SpecificConsole;

public class SimbenchConsole extends SpecificConsole {

	final static String CONSOLE_NAME = "SIMBENCH";

	public SimbenchConsole() {
		super(CONSOLE_NAME);
	}

	public SimbenchConsole (Shell shell) {
		super(CONSOLE_NAME, shell);
	}
}
