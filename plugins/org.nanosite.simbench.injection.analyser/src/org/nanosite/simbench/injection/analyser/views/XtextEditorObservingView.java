/*
 * Project: org.nanositebecker.diagnostics.diagdev.uiextensions
 * (c) copyright 2010 by Harman/Becker Automotive Systems GmbH
 * Secrecy Level STRICTLY CONFIDENTIAL
 */
package org.nanosite.simbench.injection.analyser.views;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import org.nanosite.simbench.injection.hbinj.InjModel;

/**
 * A common base class for our views observing an {@link XtextEditor}. It attaches itself
 * to the editor and listens for model changes. Derived classes have to provide an
 * {@link ITreeContentProvider} and a {@link ILabelProvider}.
 *
 * @author DaWeber
 */
public abstract class XtextEditorObservingView extends ViewPart implements IPartListener,
      IModelDataProvider
{
   private InjModel root;

   private static final class ViewUpdateOnModelChange implements IXtextModelListener
   {
      private final Viewer                   v;
      private final XtextEditorObservingView view;

      private ViewUpdateOnModelChange(Viewer v, XtextEditorObservingView view)
      {
         this.v = v;
         this.view = view;
      }

      public void modelChanged(XtextResource resource)
      {
         runInSWTThread(new Runnable()
         {
            public void run()
            {
               Object[] expandedElements = null;
               if(v instanceof TreeViewer)
               {
                  expandedElements = ((TreeViewer)v).getExpandedElements();
               }
               IXtextDocument document = view.activeXTextEditor.getDocument();
               view.extractModel(document);
               v.setInput(document);
               v.refresh();
               if(null != expandedElements && expandedElements.length > 0)
               {
                  ((TreeViewer)v).setExpandedElements(expandedElements);
               }
            }

         });
      }

      /**
       * Runs the runnable in the SWT thread. (Simply runs the runnable if the current
       * thread is the UI thread, otherwise calls the runnable in asyncexec.)
       */
      private void runInSWTThread(Runnable runnable)
      {
         if(Display.getCurrent() == null)
         {
            Display.getDefault().asyncExec(runnable);
         }
         else
         {
            runnable.run();
         }
      }
   }
   private XtextEditor             activeXTextEditor;
   private ViewUpdateOnModelChange modelChangeListener;

   private void extractModel(IXtextDocument document)
   {
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

   public void createPartControl(Composite parent)
   {
	   createObservingPartControl(parent);
	   modelChangeListener = new ViewUpdateOnModelChange(getViewer(), this);
   }

   public abstract void createObservingPartControl(Composite parent);
   public abstract Viewer getViewer();

   /**
    * Passing the focus request to the viewer's control.
    */
   public void setFocus()
   {
      getViewer().getControl().setFocus();
   }

   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
    */
   @Override
   public void init(IViewSite site) throws PartInitException
   {
      super.init(site);
      site.getPage().addPartListener(this);
   }

   @Override
   public void dispose()
   {
      removeModelListener();
      getSite().getPage().removePartListener(this);
      super.dispose();
   }

   private void removeModelListener()
   {
      if(null != activeXTextEditor && null != activeXTextEditor.getDocument())
      {
         activeXTextEditor.getDocument().removeModelListener(modelChangeListener);
      }
      activeXTextEditor = null;
   }


   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
    */
   public final void partActivated(IWorkbenchPart part)
   {
      if(part instanceof XtextEditor)
      {
         if(getViewer().getInput() != ((XtextEditor)part).getDocument())
         {
            removeModelListener();
            this.activeXTextEditor = (XtextEditor)part;
            extractModel(activeXTextEditor.getDocument());
            getViewer().setInput(activeXTextEditor.getDocument());
            activeXTextEditor.getDocument().addModelListener(modelChangeListener);
         }
      }
      else if(part instanceof EditorPart)
      {
         removeModelListener();
         getViewer().setInput(null);
      }
   }

   public void partClosed(IWorkbenchPart part)
   {
      if(part == activeXTextEditor)
      {
         removeModelListener();
         getViewer().setInput(null);
      }
   }

   public void partBroughtToTop(IWorkbenchPart part)
   {
   }

   public void partDeactivated(IWorkbenchPart part)
   {
   }

   public void partOpened(IWorkbenchPart part)
   {
   }

   public InjModel getModel()
   {
      return root;
   }
}


