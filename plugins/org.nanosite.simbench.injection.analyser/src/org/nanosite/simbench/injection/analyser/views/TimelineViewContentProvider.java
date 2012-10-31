package org.nanosite.simbench.injection.analyser.views;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

import org.nanosite.simbench.injection.hbinj.InjModel;

public class TimelineViewContentProvider implements IStructuredContentProvider {

	private InjModel root = null;
	private TraceComparison traceComparison = new TraceComparison();

	protected static final Object[] NO_OBJECTS = new Object[0];

	@Override
	public void inputChanged(Viewer arg0, Object oldInput, Object newInput) {
		if (null != newInput)
		{
			final IXtextDocument document = (IXtextDocument)newInput;
			InjModel newRoot = document.readOnly(new IUnitOfWork<InjModel, XtextResource>()
					{
						public InjModel exec(final XtextResource state) throws Exception
						{
							if ( state.getContents().isEmpty() )
							{
								return null;
							}

							// TODO: this is not generic (as the class name suggests), but specific to org.nanosite.diagnostics.mid.hmid.Model
							EObject root = state.getContents().get(0);
							if (! (root instanceof InjModel))
								return null;
							return (InjModel)root;
						}
					});

			// if root switches to null, we continue displaying the current model
			if (newRoot!=null && newRoot!=root)
				root = newRoot;
		}
		else
		{
			root = null;
		}
	}


	@Override
	public void dispose() {
	}


	@Override
	public Object[] getElements(Object arg0) {
		if (null == root) {
			return NO_OBJECTS;
		}

		traceComparison.readModel(root);
		return traceComparison.getEvents().toArray();
	}


	// force reload of traces
	public void forceReload() {
		traceComparison.forceReload();
	}
}
