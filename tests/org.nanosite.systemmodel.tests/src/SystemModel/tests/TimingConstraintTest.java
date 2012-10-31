/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package SystemModel.tests;

import SystemModel.SystemModelFactory;
import SystemModel.TimingConstraint;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Timing Constraint</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class TimingConstraintTest extends NamedElementTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(TimingConstraintTest.class);
	}

	/**
	 * Constructs a new Timing Constraint test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TimingConstraintTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Timing Constraint test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected TimingConstraint getFixture() {
		return (TimingConstraint)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(SystemModelFactory.eINSTANCE.createTimingConstraint());
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

} //TimingConstraintTest
