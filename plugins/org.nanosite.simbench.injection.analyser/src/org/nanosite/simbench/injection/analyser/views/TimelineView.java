package org.nanosite.simbench.injection.analyser.views;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.impl.CompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.IURIEditorOpener;

import com.google.inject.Inject;
import org.nanosite.simbench.HbsimHelper;
import org.nanosite.simbench.hbsim.Step;
import org.nanosite.simbench.injection.analyser.Activator;
import org.nanosite.simbench.injection.hbinj.InjIOActivity;
import org.nanosite.simbench.injection.hbinj.InjStep;


public class TimelineView extends XtextEditorObservingView {

	@Inject
	private IURIEditorOpener uriEditorOpener;

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.nanosite.simbench.injection.analyser.views.TimelineView";

	private static final DecimalFormat formatMB = new DecimalFormat("####.###");
	private static final DecimalFormat formatMillisec = new DecimalFormat("####");

	private TableViewer viewer;
	private List<TableViewerColumn> columns;

	private Image imageStartEvent = Activator.getImageDescriptor("icons/green-arrow.png").createImage();
	private Image imageLastEvent = Activator.getImageDescriptor("icons/exit.gif").createImage();

	// toolbar actions
	private Action actionReload;
	private Action actionCumulateResourceUsagePerBehaviour;
	private Action actionFilterMarked;
	private Action actionFilterMilestones;

	// context-popup actions
	private Action actionMarkSelectedBehaviourEvents;
	private Action actionMarkSelectedFBEvents;
	private Action actionMarkPredecessors;
	private Action actionMarkSuccessors;
	private Action actionGotoHbsimLocation;
	private Action actionGotoHbinjLocation;

	// control flags for marking
	private boolean cumulateResourceUsage = true;
	private TraceComparison.TraceEvent selectedFB = null;
	private TraceComparison.TraceEvent selectedBehaviour = null;
	private TraceComparison.TraceEvent markPredecessorsOf = null;
	private TraceComparison.TraceEvent markSuccessorsOf = null;

	// control flags for filtering
	private boolean filterMarked = false;
	private boolean filterMilestones = false;

	/**
	 * The constructor.
	 */
	public TimelineView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createObservingPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new TimelineViewContentProvider());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		columns = new ArrayList<TableViewerColumn>();
		columns.add(createColumn(SWT.NONE, "Event", 250, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				cell.setText(ev.getName());

				if (ev.isStartEvent())
					cell.setImage(imageStartEvent);
				else if (ev.isLastEvent())
					cell.setImage(imageLastEvent);

				if (ev.isMilestone()) {
					Display display = cell.getControl().getDisplay();
					cell.setForeground(display.getSystemColor(SWT.COLOR_RED));
				}

				markupPredecessor(cell);
				markupSuccessor(cell);
				if ((selectedFB!=null && ev.getFunctionBlock()==selectedFB.getFunctionBlock()) ||
					(selectedBehaviour!=null && ev.getBehaviour()==selectedBehaviour.getBehaviour())) {
					Display display = cell.getControl().getDisplay();
					cell.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
				}
			}
		}));
		columns.add(createColumn(SWT.NONE, "CPU", 80, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				cell.setText(ev.getCPU());
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Time real [ms]", 90, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				if (ev.getTimeReal()!=0)
					cell.setText(Integer.toString(ev.getTimeReal()));

				if (ev.isMilestone()) {
					Display display = cell.getControl().getDisplay();
					cell.setForeground(display.getSystemColor(SWT.COLOR_RED));
				}
				markupPredecessor(cell);
				markupSuccessor(cell);
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Time simu [ms]", 90, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				if (ev.getTimeSimu()>=0)
					cell.setText(Integer.toString(ev.getTimeSimu()));

				markupPredecessor(cell);
				markupSuccessor(cell);
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Delta [ms]", 80, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				if (ev.getTimeSimu()>=0 && ev.getTimeReal()>0) {
					int delta = ev.getTimeSimu()-ev.getTimeReal();
					cell.setText(Integer.toString(delta));
					markupDeltaTime(cell, delta, 10);
				}
			}
		}));
		columns.add(createColumn(SWT.NONE, "Marker", 220, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				String injMarker = ev.getMarkerString();
				if (injMarker.isEmpty()) {
					// no marker in hbinj file found, check if a marker has been modeled and show in red color
					Step step = ev.getStep();
					if (step!=null) {
						if (step.getMarker_set()!=null) {
							Display display = cell.getControl().getDisplay();
							cell.setForeground(display.getSystemColor(SWT.COLOR_RED));
							cell.setText(HbsimHelper.getMarkerString(step.getMarker_set()));
						}
					}
				} else {
					cell.setText(ev.getMarkerString());
				}
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Used real [ms]", 90, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				double used = ev.getUsedCPU(cumulateResourceUsage);
				if (used>0.001)
					cell.setText(formatMillisec.format(used));
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Used simu [ms]", 90, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				double used = ev.getUsedCPUSimu(cumulateResourceUsage);
				if (used>0.001) {
					cell.setText(formatMillisec.format(used));
					double usedReal = ev.getUsedCPU(cumulateResourceUsage);
					if (usedReal>0.0)
						markupDeltaTime(cell, used-usedReal, 2);
				}
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Read real [MB]", 90, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				InjStep step = ev.getInjStep();
				if (step!=null) {
					double amount = 0.0;
					for(InjIOActivity io : step.getAction().getIo()) {
						if (io.getOp().equals("read"))
							amount += Double.parseDouble(io.getAmount());
					}
					if (amount>0.001)
						cell.setText(formatMB.format(amount));
				}
			}
		}));
		columns.add(createColumn(SWT.RIGHT, "Write real [MB]", 90, new CellLabelProvider() {
			public void update(ViewerCell cell) {
				TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
				InjStep step = ev.getInjStep();
				if (step!=null) {
					double amount = 0.0;
					for(InjIOActivity io : step.getAction().getIo()) {
						if (io.getOp().equals("write"))
							amount += Double.parseDouble(io.getAmount());
					}
					if (amount>0.001)
						cell.setText(formatMB.format(amount));
				}
			}
		}));

		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				TraceComparison.TraceEvent ev1 = (TraceComparison.TraceEvent)e1;
				TraceComparison.TraceEvent ev2 = (TraceComparison.TraceEvent)e2;
				return ev1.compare(ev2);
			}
		});


		// markedFilter shows only events which have been marked by some action
		ViewerFilter markedFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (filterMarked) {
					TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)element;

					if (selectedFB!=null && ev.getFunctionBlock()==selectedFB.getFunctionBlock())
						return true;
					if (selectedBehaviour!=null && ev.getBehaviour()==selectedBehaviour.getBehaviour())
						return true;
					if (markPredecessorsOf!=null) {
						if (ev.getName().equals(markPredecessorsOf.getName()))
							return true;
						if (ev.isPredecessorOf(markPredecessorsOf))
							return true;
					}
					if (markSuccessorsOf!=null) {
						if (ev.getName().equals(markSuccessorsOf.getName()))
							return true;
						if (ev.isSuccessorOf(markSuccessorsOf))
							return true;
					}

					return false;
				}
				return true;
			}
		};
		viewer.addFilter(markedFilter);

		// milestonesFilter shows only milestone events
		ViewerFilter milestonesFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (filterMilestones) {
					TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)element;
					return ev.isMilestone();
				}
				return true;
			}
		};
		viewer.addFilter(milestonesFilter);


		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.nanosite.simbench.injection.analyser.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void markupDeltaTime (ViewerCell cell, double delta, int factor) {
		// colorize cell according to absolute delta
		Display display = cell.getControl().getDisplay();
		if (delta<0) delta=-delta;
		if (delta<10*factor) {
			cell.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		} else if (delta<50*factor) {
			cell.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
		} else {
			cell.setBackground(display.getSystemColor(SWT.COLOR_RED));
		}
	}

	private void markupPredecessor (ViewerCell cell) {
		if (markPredecessorsOf==null)
			return;

		TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
		Display display = cell.getControl().getDisplay();
		if (ev.getName().equals(markPredecessorsOf.getName())) { // TODO why does (ev==markPredecessorsOf) not work?
			cell.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		}

		if (ev.isPredecessorOf(markPredecessorsOf))
			cell.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
	}

	private void markupSuccessor (ViewerCell cell) {
		if (markSuccessorsOf==null)
			return;

		TraceComparison.TraceEvent ev = (TraceComparison.TraceEvent)cell.getElement();
		Display display = cell.getControl().getDisplay();
		if (ev.getName().equals(markSuccessorsOf.getName())) { // TODO why does (ev==markPredecessorsOf) not work?
			cell.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		}

		if (ev.isSuccessorOf(markSuccessorsOf))
			cell.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
	}

	private TableViewerColumn createColumn (
			int style, String heading, int width,
			CellLabelProvider labelProvider)
	{
		TableViewerColumn col = new TableViewerColumn(viewer, style);
		col.getColumn().setText(heading);
		col.getColumn().setWidth(width);
		col.setLabelProvider(labelProvider);
		return col;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TimelineView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionReload);
		manager.add(actionCumulateResourceUsagePerBehaviour);
		manager.add(actionFilterMarked);
		manager.add(actionFilterMilestones);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionMarkSelectedBehaviourEvents);
		manager.add(actionMarkSelectedFBEvents);
		manager.add(new Separator());
		manager.add(actionMarkPredecessors);
		manager.add(actionMarkSuccessors);
		manager.add(new Separator());
		manager.add(actionGotoHbsimLocation);
		manager.add(actionGotoHbinjLocation);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionReload);
		manager.add(actionCumulateResourceUsagePerBehaviour);
		manager.add(actionFilterMarked);
		manager.add(actionFilterMilestones);
	}

	private void makeActions() {
		actionMarkSelectedFBEvents = createAction("Mark function block's events", null, new Action() {
			public void run() {
					selectedFB = getEvent();
					selectedBehaviour = null;
					viewer.refresh();
			}
		});

		actionMarkSelectedBehaviourEvents = createAction("Mark behaviour's events", null, new Action() {
			public void run() {
				selectedFB = null;
				selectedBehaviour = getEvent();
				viewer.refresh();
			}
		});

		actionMarkPredecessors = createAction("Mark predecessors",
				Activator.getImageDescriptor("icons/pigr-icon.png"),
				new Action() {
					public void run() {
						markPredecessorsOf = getEvent();
						markSuccessorsOf = null;
						viewer.refresh();
					}
		});

		actionMarkSuccessors = createAction("Mark successors",
				Activator.getImageDescriptor("icons/pig-icon.png"),
				new Action() {
					public void run() {
						markPredecessorsOf = null;
						markSuccessorsOf = getEvent();
						viewer.refresh();
					}
		});

		actionGotoHbsimLocation = createAction("Open in hbsim model", null, new Action() {
			public void run() {
				TraceComparison.TraceEvent ev = getEvent();
				if (ev==null)
					return;
				EObject target = ev.getStep()!=null ? ev.getStep() : ev.getBehaviour();
				openXtextTarget(target);
			}
		});

		actionGotoHbinjLocation = createAction("Open in hbinj file", null, new Action() {
			public void run() {
				TraceComparison.TraceEvent ev = getEvent();
				if (ev==null)
					return;
				EObject target = ev.getMarker()!=null ? ev.getMarker().eContainer() : ev.getInjStep();
				openXtextTarget(target);
			}
		});

		actionReload = createAction("Reload traces",
				Activator.getImageDescriptor("icons/Button-Reload-icon32.png"),
				new Action() {
					public void run() {
						// TODO: trigger reload
						if (viewer.getContentProvider() instanceof TimelineViewContentProvider) {
							TimelineViewContentProvider cp = (TimelineViewContentProvider)viewer.getContentProvider();
							cp.forceReload();
						} else {
							System.err.println("TimelineView error: invalid content provider class!");
						}
						viewer.refresh();
					}
		});

		actionCumulateResourceUsagePerBehaviour = createAction("Cumulate resource usage per behaviour",
				Activator.getImageDescriptor("icons/abacus-icon32.png"),
				new Action("", Action.AS_CHECK_BOX) {
					public void run() {
						cumulateResourceUsage = isChecked();
						viewer.refresh();
					}
		});
		actionCumulateResourceUsagePerBehaviour.setChecked(cumulateResourceUsage);

		actionFilterMarked = createAction("Show marked events only",
				Activator.getImageDescriptor("icons/Button-Cancel-icon32.png"),
				new Action("", Action.AS_CHECK_BOX) {
					public void run() {
						filterMarked = isChecked();
						viewer.refresh();
					}
		});

		actionFilterMilestones = createAction("Show milestones only",
				Activator.getImageDescriptor("icons/referee-flag-icon32.png"),
				new Action("", Action.AS_CHECK_BOX) {
					public void run() {
						filterMilestones = isChecked();
						viewer.refresh();
					}
		});
	}

	private Action createAction (String text, ImageDescriptor image, Action action) {
		action.setText(text);
		action.setToolTipText(text);
		if (image==null) {
			action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		} else {
			action.setImageDescriptor(image);
		}
		return action;
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				actionGotoHbinjLocation.run();
			}
		});
	}

	private TraceComparison.TraceEvent getEvent() {
		ISelection selection = viewer.getSelection();
		IStructuredSelection ssel = (IStructuredSelection)selection;
		Object obj = ssel.getFirstElement();
		return (TraceComparison.TraceEvent)obj;
	}


	private void openXtextTarget (EObject target) {
		if (target==null)
			return;

		if (target.eResource()==null) {
			System.err.println("TimelineView.openXtextTarget error: missing eResource for target " + target.toString());
			return;
		}
        URI uri = target.eResource().getURI();
        IEditorPart openEditor = uriEditorOpener.open(uri, true);
        if (openEditor instanceof ITextEditor)
        {
           ICompositeNode node = NodeModelUtils.getNode(target);
           if (null != node) {
              ((ITextEditor)openEditor).selectAndReveal(node.getOffset(), node.getLength());
           }
        }
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public Viewer getViewer() {
		return viewer;
	}

}