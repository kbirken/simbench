/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.simbench.simo

import org.nanosite.simbench.simo.simModel.BinaryExpr
import org.nanosite.simbench.simo.simModel.Expr
import org.nanosite.simbench.simo.simModel.FeatureConfig
import org.nanosite.simbench.simo.simModel.FloatConstant
import org.nanosite.simbench.simo.simModel.VP_Expr
import org.nanosite.simbench.simo.simModel.VariableRef

import static extension org.nanosite.simbench.simo.FeatureBinder.*

class ExpressionEvaluator {


	def static double eval(VP_Expr vp, FeatureConfig cfg) {
		if (vp==null)
			0.0
		else {
			val expr = vp.bind(cfg)
			if (expr==null)
 				// TODO null-case is a non-valid selection and should return an error
 				0.0
			else
				expr.eval(cfg)
		}		
	}

//	//Real eval2 (Expr expr, FeatureConfig cfg) : expr==null ? 0 : ((AddExpr)expr).eval(cfg);
//	Integer round (VP_Expr vp, FeatureConfig cfg) :
//		vp==null ? 0 :
//			(let expr = vp.bind(cfg):
//				expr==null ? 0 : expr.round2(cfg) // TODO null-case is a non-valid selection and should return an error
//			);

	def static long round2(Expr expr, FeatureConfig cfg) {
		if (expr==null)
			0
		else
			Math.round(expr.eval(cfg))
	}
	

	def static private dispatch double eval(FloatConstant expr, FeatureConfig cfg) {
		expr.value.asDouble
	}

	def static private dispatch double eval(VariableRef expr, FeatureConfig cfg) {
		expr.^var.rhs.eval(cfg)
	}

	def static private dispatch double eval(BinaryExpr expr, FeatureConfig cfg) {
		val a = expr.left.eval(cfg)
		val b = expr.right.eval(cfg)
		switch (expr.op) {
			case '+': a+b
			case '-': a-b
			case '*': a*b
			case '/': a/b
			default: 0.0
		}
	}

	def static private dispatch double eval(Expr expr, FeatureConfig cfg) {
		throw new RuntimeException("ExpressionEvaluator: Unknown expression type " + expr.class.simpleName)				
	}


	def static double asDouble(String s) {
		if (s==null)
			0.0
		else
			Double.valueOf(s)
	}
	
}
