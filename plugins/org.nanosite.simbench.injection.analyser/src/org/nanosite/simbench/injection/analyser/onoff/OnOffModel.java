package org.nanosite.simbench.injection.analyser.onoff;

import java.util.ArrayList;
import java.util.List;

public class OnOffModel {
	private List<Interface> interfaces = new ArrayList<Interface>();
	private List<Process> processes = new ArrayList<Process>();

	public void addInterface (Interface intf) {
		interfaces.add(intf);
	}

	public Interface getInterface (int idx) {
		return interfaces.get(idx);
	}

	public void addProcess (Process proc) {
		processes.add(proc);
	}
}
