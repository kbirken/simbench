package org.nanosite.systemmodel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

public class DBTableProxy {

	private String tablename;

	private class DBTableItemProxy {
		private int id;
		private String name;

		private EObject eObject;

		public DBTableItemProxy(int id, String name) {
			this.id = id;
			this.name = name;
			this.eObject = null;
		}

		public void setEObject (EObject obj) {
			eObject = obj;
		}

		public EObject getEObject() {
			return eObject;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}


	private List<DBTableItemProxy> rows = new ArrayList<DBTableItemProxy>();
	private Map<EObject,Integer> indexMap = new HashMap<EObject,Integer>();
	private Map<Integer,Integer> idMap = new HashMap<Integer,Integer>();

	public DBTableProxy (String tablename) {
		this.tablename = tablename;
		reload();
	}

	public int size() {
		return rows.size();
	}

	public EObject getEObject (int index) {
		if (index >= rows.size()) {
			return null;
		}
		return rows.get(index).getEObject();
	}

	public boolean setEObject (int index, EObject obj) {
		if (index >= rows.size()) {
			return false;
		}
		rows.get(index).setEObject(obj);
		indexMap.put(obj, index);
		return true;
	}

	public int getId (EObject obj) {
		DBTableItemProxy item = getItem(obj);
		if (item==null)
			return 0;
		return item.getId();
	}

	public String getName (int index) {
		return rows.get(index).getName();
	}

	public String getName (EObject obj) {
		DBTableItemProxy item = getItem(obj);
		if (item==null)
			return "";
		return item.getName();
	}

	public EObject getObjectById (int id) {
		if (! idMap.containsKey(id))
			return null;

		int index = idMap.get(id);
		return rows.get(index).getEObject();
	}

	public String getAttributeString(EObject obj, String name) {
		DBTableItemProxy item = getItem(obj);
		if (item==null)
			return null;

		return readString(item.getId(), name);
	}

	public Integer getAttributeInt(EObject obj, String name) {
		DBTableItemProxy item = getItem(obj);
		if (item==null)
			return null;

		return readInt(item.getId(), name);
	}

	private DBTableItemProxy getItem (EObject obj) {
		if (! indexMap.containsKey(obj))
			return null;
		return rows.get(indexMap.get(obj));
	}

	private static String dbURI = "jdbc:mysql://modeler.itemis.de:3306/hbsystemmodel";
	private static String dbUser = "MySystemModel";
	private static String dbPW = "nero";

	public void reload() {
		rows.clear();
		indexMap.clear();
		idMap.clear();

		Connection cn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection(dbURI, dbUser, dbPW);
			st = cn.createStatement();
			rs = st.executeQuery("SELECT ID,NAME FROM prefix_" + tablename + " ORDER BY name ASC");

			// get meta data
			//ResultSetMetaData rsmd = rs.getMetaData();
			//int n = rsmd.getColumnCount();
			//System.out.println("DB: " + n + "entries.");

			while (rs.next()) {
				DBTableItemProxy item = new DBTableItemProxy(rs.getInt(1), rs.getString(2));
				rows.add(item);
				idMap.put(item.getId(), rows.size()-1);
			}

		} catch (Exception ex) {
			System.err.println(ex);
		} finally {
			try { if (rs!=null) rs.close(); } catch(Exception ex) {};
			try { if (st!=null) st.close(); } catch(Exception ex) {};
			try { if (cn!=null) cn.close(); } catch(Exception ex) {};
		}
	}

	private String readString (int id, String name) {
		Connection cn = null;
		Statement st = null;
		ResultSet rs = null;
		String val = "";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection(dbURI, dbUser, dbPW);
			st = cn.createStatement();
			rs = st.executeQuery("SELECT " + name + " FROM prefix_" + tablename + " WHERE ID=" + id);

			if (rs.next()) {
				val = rs.getString(1);
			}

		} catch (Exception ex) {
			System.err.println(ex);
		} finally {
			try { if (rs!=null) rs.close(); } catch(Exception ex) {};
			try { if (st!=null) st.close(); } catch(Exception ex) {};
			try { if (cn!=null) cn.close(); } catch(Exception ex) {};
		}

		return val;
	}

	private int readInt (int id, String name) {
		Connection cn = null;
		Statement st = null;
		ResultSet rs = null;
		int val = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection(dbURI, dbUser, dbPW);
			st = cn.createStatement();
			rs = st.executeQuery("SELECT " + name + " FROM prefix_" + tablename + " WHERE ID=" + id);

			if (rs.next()) {
				val = rs.getInt(1);
			}

		} catch (Exception ex) {
			System.err.println(ex);
		} finally {
			try { if (rs!=null) rs.close(); } catch(Exception ex) {};
			try { if (st!=null) st.close(); } catch(Exception ex) {};
			try { if (cn!=null) cn.close(); } catch(Exception ex) {};
		}

		return val;
	}
}
