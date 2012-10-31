/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package SystemModel.tests;

import SystemModel.NamedElement;
import SystemModel.SystemModelFactory;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Named Element</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class NamedElementTest extends DatabaseEntityTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(NamedElementTest.class);
	}

	/**
	 * Constructs a new Named Element test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NamedElementTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Named Element test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected NamedElement getFixture() {
		return (NamedElement)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(SystemModelFactory.eINSTANCE.createNamedElement());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

} //NamedElementTest
