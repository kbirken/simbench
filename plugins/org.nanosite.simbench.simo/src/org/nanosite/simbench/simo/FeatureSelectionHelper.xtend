package org.nanosite.simbench.simo

import com.google.common.collect.Sets
import com.google.common.collect.Sets.SetView
import java.util.List
import org.nanosite.feamo.feaMo.Feature
import org.nanosite.simbench.simo.simModel.FSelAndExpr
import org.nanosite.simbench.simo.simModel.FSelAtomicExpr
import org.nanosite.simbench.simo.simModel.FSelExpr
import org.nanosite.simbench.simo.simModel.FSelOrExpr
import org.nanosite.simbench.simo.simModel.FSelector
import org.nanosite.simbench.simo.simModel.FeatureConfig

import static extension org.nanosite.simbench.simo.ListExtensions.*

/**
 * Helper methods for easy implementation of variation points in derived models
 */
class FeatureSelectionHelper {
	
	// get name of a SimpleFeature
	//cached String name (SimpleFeature f) :
	//	f.mandatory==null ? f.optional.name : f.mandatory.name;

	/**
	 * Get index of first matching FSelector in fsels-list according to FeatureConfig. 
	 */
	def static Integer getBindingIndex(List<FSelector> fsels, FeatureConfig cfg) {
		val matched = fsels.matchSelector(cfg)
		if (matched.empty) {
			// no matching selector found
			-1
		} else {
			// if >1 match, take first
			fsels.indexOf(matched.head)
		}
	}

	/**
	 * Check which selector out of a list matches FeatureConfig. 
	 */
	def static Iterable<FSelector> matchSelector(List<FSelector> fsels, FeatureConfig cfg) {
		fsels.filter[matches(cfg)]
	}

	/**
	 * Check if FSelector has been configured in FeatureConfig.
	 */
	def static Boolean matches(FSelector fsel, FeatureConfig cfg) {
		fsel.expr.matches(cfg)	
	}

	def static Boolean matches(FSelExpr expr, FeatureConfig cfg) {
		if (expr==null)
			false
		else
			(expr as FSelOrExpr).matches(cfg)
	}

	def static Boolean matches(FSelOrExpr expr, FeatureConfig cfg) {
		expr.term.matches_or(cfg)
	}

	def private static Boolean matches_or(List<FSelAndExpr> list, FeatureConfig cfg) {
		if (list.empty)
			false
		else {
			if (list.head.matches(cfg))
				true
			else
				list.withoutFirst.matches_or(cfg)
		}
	}
	
	def static Boolean matches(FSelAndExpr expr, FeatureConfig cfg) {
		expr.factor.matches_and(cfg);
	}

	def private static Boolean matches_and(List<FSelAtomicExpr> list, FeatureConfig cfg) {
		if (list.empty)
			true
		else {
			if (list.head.matches(cfg))
				list.withoutFirst.matches_and(cfg)
			else
				false
		}
	}

	def static Boolean matches(FSelAtomicExpr expr, FeatureConfig cfg) {
		val sel = cfg.selected.contains(expr.feature)
		if (expr.neg)
			!sel
		else
			sel
	}
	
	def static SetView<Feature> getSelected(FeatureConfig cfg) {
		if (cfg.base==null)
			Sets.difference(cfg.plus.toSet, cfg.minus.toSet)
		else {
			val withBase = Sets.union(cfg.base.selected, cfg.plus.toSet)
			Sets.difference(withBase, cfg.minus.toSet)
		}
	}
		
}
