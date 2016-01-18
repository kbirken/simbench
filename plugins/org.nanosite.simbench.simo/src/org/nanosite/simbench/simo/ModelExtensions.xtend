/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.nanosite.simbench.simo

import com.google.common.collect.Sets
import java.util.List
import java.util.Set
import org.nanosite.simbench.simo.simModel.Action
import org.nanosite.simbench.simo.simModel.Behaviour
import org.nanosite.simbench.simo.simModel.CPU
import org.nanosite.simbench.simo.simModel.FeatureConfig
import org.nanosite.simbench.simo.simModel.FunctionBlock
import org.nanosite.simbench.simo.simModel.IOActivity
import org.nanosite.simbench.simo.simModel.Mapping
import org.nanosite.simbench.simo.simModel.Model
import org.nanosite.simbench.simo.simModel.Partition
import org.nanosite.simbench.simo.simModel.Partitioning
import org.nanosite.simbench.simo.simModel.Plan
import org.nanosite.simbench.simo.simModel.Pool
import org.nanosite.simbench.simo.simModel.Precondition
import org.nanosite.simbench.simo.simModel.Resource
import org.nanosite.simbench.simo.simModel.ResourceInterface
import org.nanosite.simbench.simo.simModel.Step
import org.nanosite.simbench.simo.simModel.StopTriggerDef
import org.nanosite.simbench.simo.simModel.TriggerCall
import org.nanosite.simbench.simo.simModel.UseCase
import org.nanosite.simbench.simo.simModel.VP_TriggerCall

import static extension org.nanosite.simbench.simo.FeatureBinder.*
import static extension org.nanosite.simbench.simo.ExpressionEvaluator.*

/**
 * TODO: Split this class.
 */
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
	
	
	// making C identifiers out of strings

	def static String makeId(String s) {
		s.replaceAll(' ', '_')
	}
	
	def static String id(FunctionBlock fb)  { fb.name.makeId }
	def static String id(Behaviour b)       { b.name.makeId }
	def static String id(StopTriggerDef tr) { tr.name.makeId }
	def static String id(Step s)            { s.name.makeId }
	def static String id(Resource r)        { r.name.makeId }
	def static String id(UseCase uc)        { uc.name.makeId }

	
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
	
	def static Set<Precondition> getPreconditions(Step step, FeatureConfig cfg) {
		step.precondition.filter[ref.bind(cfg)!=null].toSet
	}

	def static Set<IOActivity> getAnyIOs(Action action, FeatureConfig cfg) {
		action.io.map[bind(cfg)].filterNull.toSet
	}

	def static Set<IOActivity> getPoolIOs(Action action, FeatureConfig cfg) {
		action.getAnyIOs(cfg).filter[op=='alloc' || op=='free'].toSet
	}

	def static Set<IOActivity> getResourceIOs(Action action, FeatureConfig cfg) {
		action.getAnyIOs(cfg).filter[op=='read' || op=='write'].toSet
	}

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

	def static Set<Resource> getResources(Plan plan, FeatureConfig cfg) {
		plan.step.map[getAccessedResources(cfg)].flatten.toSet
	}

	def static Set<Resource> getAccessedResources(Step step, FeatureConfig cfg) {
		val ios = step.action.map[getResourceIOs(cfg)].flatten
		ios.map[ioInterface.interface.eContainer as Resource].toSet
	}

	def static Set<Pool> getPools(Plan plan, FeatureConfig cfg) {
		plan.step.map[getAccessedPools(cfg)].flatten.toSet
	}
	
	def static Set<Pool> getAccessedPools(Step step, FeatureConfig cfg) {
		step.action.map[getPoolIOs(cfg)].flatten.map[pool].toSet
	}

	def static Set<Step> getPreconditions(Resource res, Model model) {
		val behaviors = model.allFunctionBlocks.map[getReachableBehaviours(model)].flatten
		val steps = behaviors.map[plan.step].flatten
		steps.filter[s|s.provides == res].toSet
	}

	def static Set<Mapping> getMappings(Partitioning part, FeatureConfig cfg) {
		if (part.base==null)
			part.getMappingsLocal(cfg)
		else
			Sets.union(part.getMappingsLocal(cfg), part.base.getMappings(cfg)).toSet
	}

	def static Set<Mapping> getMappingsLocal(Partitioning part, FeatureConfig cfg) {
		part.mapping.map[bind(cfg)].filterNull.toSet
	}

	// top-level model navigation
	// (used currently because top-level lists don't contain elements from imported files)

	def static Set<CPU> getAllCPUs(Model model) {
		val mappings = model.main.partitioning.getMappings(model.main.config)
		mappings.map[cpu].toSet
	}
	
	def static Set<UseCase> getAllUseCases(Model model) {
		model.main.scenario.usecase.toSet
	}
	
	def static Set<TriggerCall> getAllExternalTriggers(Model model) {
		model.allUseCases.map[getTriggerCalls(model.main.config)].flatten.toSet
	}
	
	def static Set<Behaviour> getAllReachableBehaviours(Model model) {
		model.allExternalTriggers.reachable(model.main.config)
	}
	
	def static Set<FunctionBlock> getAllFunctionBlocks(Model model) {
		model.allReachableBehaviours.map[getFB].toSet
	}

	def static Set<Resource> getAllResources(Model model) {
		val behaviors = model.allFunctionBlocks.map[behaviour].flatten
		behaviors.map[plan.getResources(model.main.config)].flatten.toSet
	}

	def static Set<Pool> getAllPools(Model model) {
		val behaviors = model.getAllFunctionBlocks().map[behaviour].flatten
		behaviors.map[plan.getPools(model.main.config)].flatten.toSet
	}

	def static Set<Behaviour> getReachableBehaviours(FunctionBlock fb, Model model) {
		model.getAllReachableBehaviours.filter[b|fb.behaviour.contains(b)].toSet
	}


	// reachability analysis
	
	def static Set<Behaviour> reachable(Set<TriggerCall> start, FeatureConfig cfg) {
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


	// filtered access according to CPU mapping

	def static Mapping getMapping(FunctionBlock fb, Model model) {
		// this only works if there is at least one mapping for fb! (first()..)
		val mappings = model.main.partitioning.getMappings(model.main.config)
		mappings.filter[m | m.fb==fb].toList.head
	}
	
	def static CPU getCPU(FunctionBlock fb, Model model) {
		fb.getMapping(model)?.cpu
	}

	def static Partition getPartition(FunctionBlock fb, Model model) {
		fb.getMapping(model)?.partition
	}

	def static Set<FunctionBlock> getAllFunctionBlocksOn(Model model, CPU cpu) {
		model.allFunctionBlocks.filter(fb | fb.getCPU(model) == cpu).toSet
	}
	
	
	// helpers partitioning of CPUs
	def static boolean needsPartitioning(CPU cpu) {
		cpu.scheduling!=null && cpu.scheduling=='aps' &&
		cpu.partition!=null && cpu.partition.size>0
	}
	
	
	// computations for Actions
	def static double cpuFactor(Action a, CPU cpu, FeatureConfig cfg) {
		val factor1 = if (a.cpu==null || a.cpu.factor==null) 1.0 else a.cpu.factor.eval(cfg)
		val factor2 = if (cpu==null || cpu.factor==null) 1.0 else cpu.factor.eval(cfg)
		factor1 / factor2
	}

}
