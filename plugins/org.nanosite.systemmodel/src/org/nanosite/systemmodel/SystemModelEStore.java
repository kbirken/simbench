package org.nanosite.systemmodel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;

import SystemModel.Model;
import SystemModel.SystemModelFactory;


public class SystemModelEStore implements EStore {

	Model model = null;

	// mapping from EClass name to DB-table name
	Map<String,String> tablenames = new HashMap<String,String>();

	private SystemModelEStore() {
		//model = (Model)SystemModelFactory.eINSTANCE.create(SystemModelPackage.Literals.MODEL);
	}


	@Override
	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		if (object instanceof Model) {
			// a certain EClass instance is requested
			EReference ref = (EReference)feature;

			// check first if it has already been created
			EObject obj = getEObject(ref, index);
			return obj;
		} else {
			DBTableProxy table = getTable(object.eClass());
			if (feature.getName().equals("id")) {
				return table.getId(object);
			} else if (feature.getName().equals("name")) {
				return table.getName(object);
			} else {
				if (feature instanceof EReference) {
					EReference eref = (EReference)feature;
					DBTableProxy refTable = getTable(eref.getEReferenceType());
					int refId = table.getAttributeInt(object, feature.getName());
					return refTable.getObjectById(refId);
				} else {
					String fType = feature.getEType().getName();
					if (fType.equals("EString")) {
						return table.getAttributeString(object, feature.getName());
					} else if (fType.equals("EInt")) {
						return table.getAttributeInt(object, feature.getName());
					} else {
						System.err.println("SystemModelEStore: Unknown feature type " + fType +
								" (" + feature.getName() + ")");
					}
				}
			}
		}
		return null;
	}

	@Override
	public Object set(InternalEObject object, EStructuralFeature feature,
			int index, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		if (object instanceof Model) {
			return true;
//			if (feature.getName().equals("design_parts")) {
//				return true;
//			}
		}
		return false;
	}

	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size(InternalEObject object, EStructuralFeature feature) {
		if (object instanceof Model && feature instanceof EReference) {
			EReference eref = (EReference)feature;
			String tablename = feature.getName();
			tablenames.put(eref.getEReferenceType().getName(), tablename);
			return getTable(tablename).size();
		}
		return 0;
	}

	@Override
	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		if (object instanceof Model) {
			return true;
		}
		return false;
	}

	@Override
	public int indexOf(InternalEObject object, EStructuralFeature feature,
			Object value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(InternalEObject object, EStructuralFeature feature,
			Object value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature,
			int index, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature,
			int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object move(InternalEObject object, EStructuralFeature feature,
			int targetIndex, int sourceIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(InternalEObject object, EStructuralFeature feature,
			T[] array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode(InternalEObject object, EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public InternalEObject getContainer(InternalEObject object) {
		if (object instanceof Model)
			return null;
		else
			return (InternalEObject)model;
	}

	@Override
	public EStructuralFeature getContainingFeature(InternalEObject object) {
		return object.eContainingFeature();
//		if (object instanceof DesignPart)
//			return SystemModelPackage.Literals.MODEL__DESIGN_PARTS;  // TODO
//		if (object instanceof ProjectInstance)
//			return SystemModelPackage.Literals.MODEL__PROJECT_INSTANCES;  // TODO
//		return null;
	}

	@Override
	public EObject create(EClass eClass) {
		// TODO Auto-generated method stub
		return null;
	}

	// the singleton instance
	public static EStore INSTANCE = new SystemModelEStore();

	// mapping from DB-table name to TableProxy
	Map<String,DBTableProxy> tablesCache = new HashMap<String,DBTableProxy>();

	// create DB-table on demand
	private DBTableProxy getTable(String tablename) {
		if (! tablesCache.containsKey(tablename)) {
			// create it
			DBTableProxy table = new DBTableProxy(tablename);
			tablesCache.put(tablename, table);
		}
		return tablesCache.get(tablename);
	}

	private DBTableProxy getTable(EClass eclass) {
		String tablename = tablenames.get(eclass.getName());
		return getTable(tablename);
	}

	// create EObject in DB-table on demand
	private EObject getEObject (EReference ref, int index) {
		DBTableProxy table = getTable(ref.getName());
		EObject obj = table.getEObject(index);
		if (obj==null) {
			// create it
			obj = SystemModelFactory.eINSTANCE.create(ref.getEReferenceType());
			table.setEObject(index, obj);
			System.out.println("created " + ref.getEReferenceType().getName() + " " +
					table.getName(index) +
					" for index " + index);
		}
		return obj;
	}
}
