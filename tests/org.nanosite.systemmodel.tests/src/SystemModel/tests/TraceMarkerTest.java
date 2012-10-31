/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package SystemModel.tests;

import SystemModel.SystemModelFactory;
import SystemModel.TraceMarker;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Trace Marker</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class TraceMarkerTest extends NamedElementTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(TraceMarkerTest.class);
	}

	/**
	 * Constructs a new Trace Marker test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TraceMarkerTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Trace Marker test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected TraceMarker getFixture() {
		return (TraceMarker)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(SystemModelFactory.eINSTANCE.createTraceMarker());
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

} //TraceMarkerTest
