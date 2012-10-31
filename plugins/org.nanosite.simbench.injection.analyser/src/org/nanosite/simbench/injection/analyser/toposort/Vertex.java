package org.nanosite.simbench.injection.analyser.toposort;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
	private List<Vertex> successors = new ArrayList<Vertex>();
//	private int N;
//	private int L;
	private boolean visited = false;

	public void addSuccessor (Vertex v) {
		successors.add(v);
	}

	public int getNSuccessors() {
		return successors.size();
	}

	public Vertex getSuccessor(int i) {
		return successors.get(i);
	}

	public boolean hasSuccessor (Vertex v) {
		return successors.contains(v);
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited() {
		visited = true;
	}

//	public void setL(int v) {
//		L = v;
//	}
//
//	public void setN(int v) {
//		N = v;
//	}
//
//	public int getL() {
//		return L;
//	}
//
//	public int getN() {
//		return N;
//	}
}
