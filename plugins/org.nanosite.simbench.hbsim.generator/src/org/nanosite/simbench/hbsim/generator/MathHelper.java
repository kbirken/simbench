package org.nanosite.simbench.hbsim.generator;

public class MathHelper {
	public static long round(Number value) {
		return Math.round(value.doubleValue());
	}
	public static double asDouble (String value) {
		if (value == null) {
			return 0.0;
		}
		return Double.valueOf(value);
	}
}

