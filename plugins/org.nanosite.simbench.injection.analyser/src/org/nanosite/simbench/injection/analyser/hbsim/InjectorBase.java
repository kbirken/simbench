package org.nanosite.simbench.injection.analyser.hbsim;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public class InjectorBase {

	static private DecimalFormatSymbols dfs = null;
	static private DecimalFormat df = null;

	protected DecimalFormat getDecimalFormat() {
		if (df == null) {
			// create on demand
			dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');

			df = new DecimalFormat("0.000");
			df.setDecimalFormatSymbols(dfs);
		}

		return df;
	}


	protected void touch (EObject what) {
		what.eResource().setModified(true);
		log("touched object " + what.toString() + " = resource " + what.eResource().toString());
	}


	protected void error (EObject where, String txt) {
		System.err.println("Error: " + txt + " (line " + getLineNumber(where) + ")!");
		System.err.flush();
	}

	protected void warning (EObject where, String txt) {
		System.err.println("Warning: " + txt + " (line " + getLineNumber(where) + ")!");
		System.err.flush();
	}

	protected void log (String txt) {
		System.out.println(txt);
		System.out.flush();
	}


	protected int getLineNumber (EObject obj) {
		return NodeModelUtils.getNode(obj).getStartLine();
	}
}
