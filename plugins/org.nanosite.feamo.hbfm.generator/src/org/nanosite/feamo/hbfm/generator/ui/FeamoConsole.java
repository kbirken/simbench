package org.nanosite.feamo.hbfm.generator.ui;

import org.eclipse.swt.widgets.Shell;

import org.nanosite.common.util.ui.SpecificConsole;

public class FeamoConsole extends SpecificConsole {

	final static String CONSOLE_NAME = "Feature Modeling";

	public FeamoConsole() {
		super(CONSOLE_NAME);
	}

	public FeamoConsole (Shell shell) {
		super(CONSOLE_NAME, shell);
	}

}
