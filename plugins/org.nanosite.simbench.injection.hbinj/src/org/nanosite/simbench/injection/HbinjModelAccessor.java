package org.nanosite.simbench.injection;

//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.emf.ecore.util.EcoreUtil;
//import org.eclipse.xtext.resource.XtextResource;
//import org.eclipse.xtext.resource.XtextResourceSet;
//
//import com.google.common.collect.Iterators;
//import com.google.common.collect.Lists;
//import org.nanosite.feamo.hbfm.FeatureModel;
//import org.nanosite.simbench.injection.hbinj.InjFunctionBlock;
//import org.nanosite.simbench.injection.hbinj.InjModel;

/**
 * Provides transparent access to hbinj models which have been distributed onto multiple resources.
 */
public class HbinjModelAccessor {

//	public static List<InjFunctionBlock> getFunctionBlocks (InjModel main) {
//		if (main==null) {
//			System.err.println("HbinjModelAccessor error: main==null");
//			return new ArrayList<InjFunctionBlock>();
//		}
//		List<InjFunctionBlock> fbs = getAllElements(main, InjFunctionBlock.class);
//		for(InjFunctionBlock fb : fbs) {
//			System.out.println("FB " + fb.getFb().getName());
//		}
//		return fbs;
//	}

//	public static List<InjFunctionBlock> getFunctionBlocks (InjModel main) {
//		List<InjFunctionBlock> fbs = new ArrayList<InjFunctionBlock>();
//		if (main==null) {
//			System.err.println("HbinjModelAccessor error: main==null");
//			return fbs;
//		}
//
//		// add main's direct fbs
//		fbs.addAll(main.getFbs());
//
//		// add fbs from hbinj-imports
//		// TODO: this probably doesn't work recursively yet
//		ResourceSet resourceSet = main.eResource().getResourceSet();
//		List<Resource> parts = new ArrayList<Resource>();
//		for(Resource r : resourceSet.getResources()) {
//			System.out.println("Resource " + r.getURI().toString() + (r.isLoaded() ? " is loaded" : " is not loaded"));
//			if (r.getContents().get(0)!=null && (r.getContents().get(0) instanceof InjModel)) {
//				parts.add(r);
//			}
//		}
//
//		for(Resource res : parts) {
//			InjModel part = (InjModel)res.getContents().get(0);
//			System.out.println("  #fb = " + part.getFbs().size());
//			for(InjFunctionBlock fb : part.getFbs()) {
//				if (! fbs.contains(fb)) {
//					fbs.add(fb);
//				}
//			}
//		}
//
//		return fbs;
//	}
//
//	// ****
//
//	private static <T> ArrayList<T> getAllElements (EObject elemInRootResource, Class<T> clazz) {
//		ResourceSet resourceSet = elemInRootResource.eResource().getResourceSet();
//		for(Resource r : resourceSet.getResources()) {
//			System.out.println("Resource " + r.getURI().toString() + (r.isLoaded() ? " is loaded" : " is not loaded"));
//			if (r.getContents().get(0)!=null && (r.getContents().get(0) instanceof InjModel)) {
//				InjModel m = (InjModel)r.getContents().get(0);
//				System.out.println("    #fb = " + m.getFbs().size());
//			}
//		}
//		System.out.println("\n\n");
//		Iterator<Object> unfiltered = EcoreUtil.getAllContents(resourceSet, true);
//		while (unfiltered.hasNext()) {
//			Object obj = unfiltered.next();
//			if (obj instanceof Resource) {
//				System.out.println("Obj " + obj.toString());
//			}
//		}
//		System.out.println("\n\n");
//		Iterator<T> filtered = Iterators.filter(unfiltered, clazz);
//		return Lists.newArrayList(filtered);
//	}
}
