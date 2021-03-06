package org.nanosite.simbench.ide.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nanosite.common.util.graphviz.GraphvizWrapper;
import org.nanosite.simbench.backend.warp.WarpRunner;
import org.nanosite.simbench.ide.Activator;

public class PreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public PreferencesPage() {
		super(GRID);
	}

	public PreferencesPage(String title, int style) {
		super(title, style);
		// TODO Auto-generated constructor stub
	}

	public PreferencesPage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench arg0) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(GraphvizWrapper.ID_GRAPHVIZ_PATH, "Dot path:",
	            getFieldEditorParent()));
		addField(new FileFieldEditor(WarpRunner.ID_WARP_PATH, "Warp path:",
	            getFieldEditorParent()));
	}

}
