/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
