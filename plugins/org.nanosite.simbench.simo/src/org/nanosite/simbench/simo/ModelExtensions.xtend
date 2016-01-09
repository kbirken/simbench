package org.nanosite.simbench.simo

import com.google.common.collect.Sets
import java.util.List
import java.util.Set
import org.nanosite.simbench.simo.simModel.Behaviour
import org.nanosite.simbench.simo.simModel.FeatureConfig
import org.nanosite.simbench.simo.simModel.FunctionBlock
import org.nanosite.simbench.simo.simModel.IOActivity
import org.nanosite.simbench.simo.simModel.Model
import org.nanosite.simbench.simo.simModel.Resource
import org.nanosite.simbench.simo.simModel.ResourceInterface
import org.nanosite.simbench.simo.simModel.Step
import org.nanosite.simbench.simo.simModel.TriggerCall
import org.nanosite.simbench.simo.simModel.UseCase
import org.nanosite.simbench.simo.simModel.VP_TriggerCall

import static extension org.nanosite.simbench.simo.FeatureBinder.*

class ModelExtensions {

	// model navigation extensions

	def static FunctionBlock getFB(Step step) {
		step.eContainer.eContainer.eContainer as FunctionBlock
	}
	
	def static Behaviour getBehaviour(Step step) {
		step.eContainer.eContainer as Behaviour
	}
	
	def static FunctionBlock getFB(Behaviour b) {
		b.eContainer as FunctionBlock
	}
	
	def static Resource getResource(ResourceInterface i) {
		i.eContainer as Resource
	}
	
	def static ResourceInterface getInterface(IOActivity io) {
		io.ioInterface.interface
	}
	
	
	// human readable full names for entities

	def static String fullName(FunctionBlock fb) {
		fb.name
	}

	def static String fullName(Behaviour b) {
		b.getFB.fullName + "::" + b.name
	}

	def static String fullName(Step s) {
		s.behaviour.fullName + "::" + s.name
	} 


	// model navigation collection extensions
	
	def static Set<TriggerCall> getTriggerCalls(Behaviour b, FeatureConfig cfg) {
		b.send.bindTriggerCalls(cfg)
	}

	def static Set<TriggerCall> getTriggerCalls(UseCase uc, FeatureConfig cfg) {
		uc.part.bindTriggerCalls(cfg)
	}

	def private static Set<TriggerCall> bindTriggerCalls(
		List<VP_TriggerCall> items,
		FeatureConfig cfg
	) {
		items.map[bind(cfg)].filterNull.toSet
	}

	// top-level model navigation
	// (used currently because top-level lists don't contain elements from imported files)

	def static Set<UseCase> getAllUseCases(Model model) {
		model.main.scenario.usecase.toSet
	}
	
	def static Set<TriggerCall> getAllExternalTriggers(Model model) {
		model.allUseCases.map[getTriggerCalls(model.main.config)].flatten.toSet
	}
	
	def static Set<Behaviour> getAllReachableBehaviours(Model model) {
		model.allExternalTriggers.reachable(model.main.config)
	}


	// reachability analysis
	
	def static Set<Behaviour> reachable (Set<TriggerCall> start, FeatureConfig cfg) {
		val bs = start.map[trigger].toSet()
		bs.successorsRec(bs, cfg)
	}

	def static Set<Behaviour> successorsRec(
		Set<Behaviour> visit,
		Set<Behaviour> visited,
		FeatureConfig cfg
	) {
		val nextvis = visit.directSuccessors(cfg)
		val newnext = Sets.difference(nextvis, visited)
		if (newnext.empty)
			visited
		else
			newnext.successorsRec(Sets.union(visited, newnext), cfg);
	}

	def static Set<Behaviour> directSuccessors(Set<Behaviour> bs, FeatureConfig cfg) {
		bs.map[directSuccs(cfg)].flatten.toSet
	}

	def private static Set<Behaviour> directSuccs(Behaviour b, FeatureConfig cfg) {
		b.getTriggerCalls(cfg).map[trigger].toSet
	}

}
