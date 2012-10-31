package com.harman.simbench.hbsim.tests;


import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import com.harman.simbench.HbsimStandaloneSetup;
import com.harman.simbench.hbsim.Model;

public class LoadModelsTest {

	final static String TEST_PATH = "platform:/resource/com.harman.simbench.hbsim.examples/tests";

	private Injector injector;

	@Before
	public void setUp() throws Exception {
//		injector = new HbsimTestSetup("../com.harman.diagnostics.mid.examples/simple",
//				"../com.harman.diagnostics.mid.examples/scpf",
//		"../com.harman.diagnostics.mid.examples/simple").createInjectorAndDoEMFRegistration();

		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("..");
		injector = new HbsimStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void loadModelTest01() throws Exception
	{
		Model model = loadModel(TEST_PATH + "/test01/main.hbsim");
		assertEquals(5, model.getVp_elements().size());
	}

	@Test
	public void loadModelTest02() throws Exception
	{
		Model model = loadModel(TEST_PATH + "/test02/main.hbsim");
		assertEquals(6, model.getVp_elements().size());
	}

	@Test
	public void loadModelTest03() throws Exception
	{
		Model model = loadModel(TEST_PATH + "/test03/main.hbsim");
		assertEquals(6, model.getVp_elements().size());
	}

	@Test
	public void loadModelTest04() throws Exception
	{
		Model model = loadModel(TEST_PATH + "/test04/main.hbsim");
		assertEquals(7, model.getVp_elements().size());
	}



	private Model loadModel (String path) throws Exception
	{
		ResourceSet rset = injector.getInstance(XtextResourceSet.class);
		Resource testResource = rset.createResource(URI.createURI(path));
		testResource.load(null);
		assertEquals(1, testResource.getContents().size());
		assertTrue(testResource.getContents().get(0) instanceof Model);

		Model model = (Model)testResource.getContents().get(0);

		resolveAllProxies(testResource);
		Diagnostic diagnostic = injector.getInstance(Diagnostician.class).validate(model);
		assertEquals(resourceErrorList(testResource.getErrors()), 0, testResource.getErrors().size());
		assertEquals(errorList(diagnostic), 0, Iterables.size(errors(diagnostic.getChildren())));

		return model;
	}

	private void resolveAllProxies(Resource resource)
	{
		ResourceSet resourceSet = resource.getResourceSet();
		int numberResources;
		do
		{
			numberResources = resourceSet.getResources().size();
			EcoreUtil.resolveAll(resource);
		}
		while(numberResources != resourceSet.getResources().size());
	}

	private String errorList(Diagnostic diagnostic)
	{
		StringBuilder ret = new StringBuilder('\n');
		ret.append(diagnostic.getMessage()).append('\n');
		for(Diagnostic d : errors(diagnostic.getChildren()))
		{
			ret.append("   ");
			ret.append(d.getMessage()).append('\n');
		}
		return ret.toString();
	}

	private Iterable<Diagnostic> errors(List<Diagnostic> issues)
	{
		return Iterables.filter(issues, new Predicate<Diagnostic>()
				{
			public boolean apply(Diagnostic input)
			{
				return input.getSeverity() == Diagnostic.ERROR;
			}
				});
	}

	private String resourceErrorList(List<Resource.Diagnostic> errors)
	{
		StringBuilder ret = new StringBuilder('\n');
		for(Resource.Diagnostic d : errors)
		{
			ret.append(d.getMessage()).append('\n');
		}
		return ret.toString();
	}
}
