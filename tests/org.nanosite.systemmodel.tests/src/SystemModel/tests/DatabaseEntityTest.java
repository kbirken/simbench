/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package SystemModel.tests;

import SystemModel.DatabaseEntity;
import SystemModel.SystemModelFactory;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Database Entity</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class DatabaseEntityTest extends TestCase {

	/**
	 * The fixture for this Database Entity test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DatabaseEntity fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(DatabaseEntityTest.class);
	}

	/**
	 * Constructs a new Database Entity test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DatabaseEntityTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Database Entity test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(DatabaseEntity fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Database Entity test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DatabaseEntity getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(SystemModelFactory.eINSTANCE.createDatabaseEntity());
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

} //DatabaseEntityTest
