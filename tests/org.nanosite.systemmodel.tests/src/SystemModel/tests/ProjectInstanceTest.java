/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package SystemModel.tests;

import SystemModel.ProjectInstance;
import SystemModel.SystemModelFactory;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Project Instance</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ProjectInstanceTest extends NamedElementTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ProjectInstanceTest.class);
	}

	/**
	 * Constructs a new Project Instance test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProjectInstanceTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Project Instance test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected ProjectInstance getFixture() {
		return (ProjectInstance)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(SystemModelFactory.eINSTANCE.createProjectInstance());
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

} //ProjectInstanceTest
