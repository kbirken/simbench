package org.nanosite.simbench.simo.generator.warp

import com.google.common.collect.Sets
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.nanosite.simbench.simo.simModel.Behaviour
import org.nanosite.simbench.simo.simModel.FeatureConfig
import org.nanosite.simbench.simo.simModel.FunctionBlock
import org.nanosite.simbench.simo.simModel.Model
import org.nanosite.simbench.simo.simModel.Step
import org.nanosite.simbench.simo.simModel.VP_Expr

import static extension org.nanosite.simbench.simo.ExpressionEvaluator.*
import static extension org.nanosite.simbench.simo.FeatureBinder.*
import static extension org.nanosite.simbench.simo.ModelExtensions.*
import static extension org.nanosite.simbench.simo.ListExtensions.*
import org.nanosite.simbench.simo.simModel.IOActivity
import org.nanosite.simbench.simo.simModel.ResourceInterface
import org.nanosite.simbench.simo.simModel.Action
import org.nanosite.simbench.simo.simModel.CPU
import org.nanosite.simbench.simo.simModel.Pool
import org.nanosite.simbench.simo.simModel.BehaviourRepeat

/**
 * This is a generator for warp input files in "condensed" ASCII format.
 * 
 * It is the re-implementation of the original warp generator, which has
 * been implemented with OAW technology (xpt/ext templates).
 * 
 * The units in the generated WarpSimulator files are:
 * 		- times in microsecs
 * 		- amounts in kByte
 * 		- percentage in percent*10 (e.g., 12.5% is resolved to 125)
 */
class WarpGenerator extends AbstractGenerator {
	
	val static int FILE_FORMAT_VERSION = 9
	
	override void doGenerate(
		Resource resource,
		IFileSystemAccess2 fsa,
		IGeneratorContext context
	) {
		val firstModel = resource.allContents.filter(Model).head
		if (firstModel?.main != null)
			fsa.generateFile("example_warp.txt", generateAll(firstModel))
	}
	
	def private generateAll(Model it) {
		val steps = allSteps
		
		'''
			«FILE_FORMAT_VERSION»
			«getAllCPUs.size+getAllResources.size»
			«genCPUs»
			«genResources»
			«genPools»
			«genFunctionBlocks»
			«genSteps(steps)»
			«genExternalTriggers»
			«genAllReachableBehaviors»
			«genStepPreconditions(steps)»
		'''
	}

	def private genCPUs(Model model) '''
		«FOR cpu : model.allCPUs»
			«cpu.name» 0
			«IF cpu.needsPartitioning»
				1 «cpu.partition.size»
				«FOR p : cpu.partition»
					«p.name» «p.percentage.percent(model.main.config)»
				«ENDFOR»
			«ELSE»
				0
			«ENDIF»			
		«ENDFOR»
	'''
	
	def private genResources(Model model) '''
		«FOR r : model.allResources»
			«r.shortName» «r.interface.size»
			«FOR ri : r.interface»
				«ri.contextSwitchingTime.percent(model.main.config)»
			«ENDFOR»
			0
		«ENDFOR»
	'''

	def private genPools(Model model) '''
		«model.allPools.size»
		«FOR p : model.allPools»
			«p.name» «p.maximum.amount(model.main.config)»
		«ENDFOR»
	'''

	def private genFunctionBlocks(Model model) '''
		«model.allFunctionBlocks.size»
		«FOR fb : model.allFunctionBlocks»
			«fb.genFB(model)»
		«ENDFOR»
	'''

	def private genFB(FunctionBlock fb, Model model) {
		val part = fb.getPartition(model)
		val cpu = fb.getCPU(model)

		if (cpu.needsPartitioning && part!=null &&
			part.percentage.eval(model.main.config)==0)
		{
			error("fb '" + fb.name + "' " +
				"is mapped to zero-sized partition '" + part.name + "'")
			return ""		
		}
		
		val behaviors = fb.getReachableBehaviours(model)
		
		return '''
			«fb.id» «model.allCPUs.toList.indexOf(cpu) + 1» «IF part==null»0«ELSE»«cpu.partition.indexOf(part) + 1»«ENDIF»
			«" " + behaviors.size»
			«FOR b : behaviors»
				«b.genBehaviorDefinition(model)»
			«ENDFOR»
		'''
	}
	
	def private genBehaviorDefinition(Behaviour it, Model model) '''
		«"  " + id» «IF isToken»1«ELSE»0«ENDIF» «repeat.genRepeat(model)»
	'''
	
	def private genRepeat(BehaviourRepeat repeat, Model model) {
		if (repeat==null)
			'''0'''
		else {
			val loop = repeat.loop.round2(model.main.config)
			if (loop > 0)
				'''1 «loop»'''
			else if (repeat.until!=null)
				'''2 0'''
			else if (repeat.unless!=null)
				'''3 «repeat.unless.step.index(model)»'''
			else
				'''0'''
		}
	}
	
	def private genSteps(Model model, List<Step> steps) '''
		«steps.size»
		«FOR s : steps»
			«s.genStepDefinition(steps.indexOf(s), model)»
		«ENDFOR»
	'''
	
	def private genStepDefinition(Step it, int i, Model model) '''
		«i» «getFB.index(model)» «getBehaviour.index(model)» «id» «milestoneType()» «genResourceConsumption(model)»
	'''
	
	def private genResourceConsumption(Step it, Model model) '''
		«IF action.size==1»«action.head.genAction(it, model)»«ELSE»«genNoAction(model)»«ENDIF»
	'''

	def private genNoAction(Model model) {
		val n = model.getAllCPUs.size + model.getAllResources.map[interface].flatten.size + model.getAllPools.size
		val StringBuffer sb = new StringBuffer
		sb.append("0") // for additionalWaitTime
		for(i : 1..n)
			sb.append(" 0")
		sb.toString
	}

	def private genAction(Action a, Step it, Model model) {
		val StringBuffer sb = new StringBuffer
		
		if (a.additionalWaitTime!=null)
			sb.append(" " + a.additionalWaitTime.time(model.main.config))
		else
			sb.append(" " + 0)

		for(cpu : model.getAllCPUs) {
			if (getFB.getCPU(model) == cpu)
				sb.append(" " + a.allCPU(cpu, model.main.config))
			else
				sb.append(" " + 0)
		}

		for(ri : model.getAllResources().map[interface].flatten) {
			sb.append(" " + resourceTime(ri, model.main.config))
		}
		
		for(p : model.getAllPools) {
			sb.append(" " + poolAmount(p, model.main.config))
		}
		sb.toString		
	}
	
	
	def private genExternalTriggers(Model model) '''
		«model.getAllExternalTriggers.size»
		«FOR tr : model.getAllExternalTriggers»
			«tr.fb.index(model)» «tr.trigger.index(model)»
		«ENDFOR»
	'''
	
	def private genAllReachableBehaviors(Model model) '''
		«model.getAllReachableBehaviours.size»
		«FOR b : model.getAllReachableBehaviours»
			«b.getFB().index(model)» «b.index(model)» «b.getTriggerCalls(model.main.config).size»
			«FOR tr : b.getTriggerCalls(model.main.config)»
			 «tr.fb.index(model)» «tr.trigger.index(model)»
			«ENDFOR»
		«ENDFOR»
	'''
	
	def private genStepPreconditions(Model model, List<Step> steps) '''
		«FOR s : steps»
			«IF s.preconditions(model).size > 0»
				«steps.indexOf(s)» «s.preconditions(model).size»
				«FOR p : s.preconditions(model)»
					«val idx = p.index(model)»
					«IF idx<0»«error("Step '" + s.fullName + "' has non-reachable precondition '" + p.fullName + "'")»«ELSE»
					 «p.index(model)»
					«ENDIF»
				«ENDFOR»
			«ENDIF»
		«ENDFOR»
		-1
	'''
	

	// round time-Expr to actual timebase (which is microsec)

	def static long time(double r) {
		Math.round(r * 1000.0)
	}

	def static long time(VP_Expr e, FeatureConfig cfg) {
		e.eval(cfg).time
	}

	def static long time(VP_Expr e, FeatureConfig cfg, double factor) {
		val r = e.eval(cfg) * factor
		r.time
	}


	/**
	 * Round percent-expression to percent*10 (e.g., 12.5% resolves to 125).
	 */
	def static private long percent(double r) {
		Math.round(r * 10.0)
	}
	
	def static private long percent(VP_Expr expr, FeatureConfig cfg) {
		expr.eval(cfg).amount
	}
	
	/**
	 * Round amount-Expr to actual n*1000 (needed for pool alloc/free amounts).
	 */
	def static private long amount(double r) {
		Math.round(r * 1000.0)
	}
	
	def static private long amount(VP_Expr expr, FeatureConfig cfg) {
		expr.eval(cfg).amount	
	}
	
	
	def private error(String msg) {
		System.err.println("ERROR in WarpGenerator: " + msg)
	}


	// helpers for computing ids of various elements
	
	def static List<Step> allSteps(Model model) {
		val behaviors = model.getAllFunctionBlocks.map[getReachableBehaviours(model)].flatten
		behaviors.map[plan.step].flatten.toList
	}
	
	def static Integer index(Step step, Model model) {
		model.allSteps.indexOf(step)
	}

	def static Integer index(Behaviour b, Model model) {
		b.getFB.getReachableBehaviours(model).toList().indexOf(b)
	}
	
	def static Integer index(FunctionBlock fb, Model model) { 
		model.getAllFunctionBlocks.toList.indexOf(fb)
	}
	
	def static List<Step> preconditionsExplicit(Step step, FeatureConfig cfg) {
		step.getPreconditions(cfg).map[ref.bind(cfg).step].toList
	}

	def static List<Step> preconditionsImplicit(Step step, Model model) {
		step.getAccessedResources(model.main.config).map[getPreconditions(model)].flatten.toList
	}
	
	def static List<Step> preconditions(Step step, Model model) {
		val implicit = step.preconditionsImplicit(model).toSet
		val explicit = step.preconditionsExplicit(model.main.config).toSet
		Sets.union(implicit, explicit).toList
	}

	def static Integer milestoneType (Step s) {
		if (s.milestone==null)
			0
		else {
			if (s.milestone=='internal_milestone') 1 else 2
		}
	}


	// computations for IOActivities

	def static double getBandwidth(IOActivity io, FeatureConfig cfg) {
		io.getInterface.bandwidth.eval(cfg)
	}

	// return time for IOActivity (read access) in microsecs
	def static long getTime(IOActivity io, FeatureConfig cfg) {
		(io.amount.eval(cfg) * 1000 / io.getBandwidth(cfg)).time
	}

	// return induced CPU time for IOActivity in microsecs
	def static long getInducedCPU(IOActivity io, FeatureConfig cfg) {
		Math.round(io.getTime(cfg) * io.getInterface.inducedCPU.eval(cfg) / 100)
	}

	// return amount*1000 of this alloc/free activity (<0 is free, >0 is alloc)
	def static long getAmount(IOActivity io, FeatureConfig cfg) {
		(if (io.op=='alloc') 1 else -1) * io.amount.amount(cfg)
	}

	def static List<IOActivity> ioActivities(Step step, ResourceInterface ri, FeatureConfig cfg) {
		step.action.map[getResourceIOs(cfg)].flatten.filter[io | io.ioInterface.interface==ri].toList
	}
	
	def static long resourceTime(Step step, ResourceInterface ri, FeatureConfig cfg) {
		step.ioActivities(ri,cfg).map[io|io.getTime(cfg)].sumlist
	}
	
	def static List<IOActivity> poolActivities(Step step, Pool pool, FeatureConfig cfg) {
		step.action.map[getPoolIOs(cfg)].flatten.filter[io | io.pool==pool].toList
	}

	def static long poolAmount(Step step, Pool pool, FeatureConfig cfg) {
		step.poolActivities(pool,cfg).map[getAmount(cfg)].sumlist
	}


	// computations for Actions
	
	def static private long allCPU(Action a, CPU cpu, FeatureConfig cfg) {
		(if (a.consumedCPU==null) 0 else a.consumedCPU.time(cfg, a.cpuFactor(cpu, cfg))) + a.inducedCPU(cfg)
	}

	def static private long inducedCPU(Action a, FeatureConfig cfg) {
		val values = a.getResourceIOs(cfg).map[getInducedCPU(cfg)]
		values.sumlist
	}


}
