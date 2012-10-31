package org.nanosite.simbench.injection.analyser.toposort;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TopoSorter {
	private Stack<Vertex> stk;

	@SuppressWarnings("unchecked")
	public <T extends Vertex> List<T> sort (List<T> dag) {
		this.stk = new Stack<Vertex>();
		//this.stkArray = new ArrayList<Vertex>();
		//this.c = 1;

		// while there is an "unvisited" v do dfs(v)
		for(int i=0; i<dag.size(); i++) {
			if (! dag.get(i).isVisited())
				dfs(dag.get(i));
		}

		// pop() until STACK=empty and write
		List<T> result = new ArrayList<T>();
		for(int i=0; i<dag.size(); i++) {
			result.add((T)stk.pop());
		}

		return result;
	}

	private void dfs (Vertex v) {
		// mark v visited
		v.setVisited();

		// get the number of OUT(V)
		int numouts = v.getNSuccessors();

		// copies v.getOuts(i), the out vertices of v, to a temporary array outs[i]
		Vertex[] outs = new Vertex[numouts+1];
		for(int i=0; i<numouts; i++)
			outs[i] = v.getSuccessor(i);

		//for each w in OUT(v) do
		for(int i=0; i<numouts; i++) {
			Vertex w = outs[i];

			// if w is unvisited then dfs(w)
			if (!w.isVisited()) {
				dfs(w);
			}
		}

		// push(v) into STACK
		stk.push(v);
	}
}
