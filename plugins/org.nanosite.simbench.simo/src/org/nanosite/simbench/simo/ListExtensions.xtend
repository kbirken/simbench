package org.nanosite.simbench.simo

import java.util.List

class ListExtensions {
	
	def static <T> List<T> withoutFirst(List<T> input) {
		input.subList(1, input.size)
	}
	
	def static long sumlist(Iterable<Long> input) {
		if (input.empty)
			0
		else
			input.reduce[a,b | a + b]
	}
}
