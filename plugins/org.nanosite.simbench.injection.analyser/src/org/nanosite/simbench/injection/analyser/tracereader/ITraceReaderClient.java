package org.nanosite.simbench.injection.analyser.tracereader;

public interface ITraceReaderClient {
	// times in millisec

	// detected start of behaviour
	abstract void processStartEvent (int line, int time, String fb, String behaviour);

	// detected finished step of behaviour
	abstract void processEventDone (int line, int time, String fb, String behaviour, String step);
}
