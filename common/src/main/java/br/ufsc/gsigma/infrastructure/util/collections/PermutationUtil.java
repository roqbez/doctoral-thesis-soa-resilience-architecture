package br.ufsc.gsigma.infrastructure.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class PermutationUtil {

	public static <K> List<List<K>> permutation(Collection<K> s, int n) {
		List<List<K>> permutation = new LinkedList<List<K>>();
		permutationsInternal(s, new LinkedList<K>(), permutation, n);
		return permutation;
	}

	private static <K> void permutationsInternal(Collection<K> items, List<K> aux, List<List<K>> resultPermutation, int size) {

		if (aux.size() == size) {
			resultPermutation.add(new ArrayList<K>(aux));
		}

		for (K i : new HashSet<K>(items)) {
			aux.add(i);
			items.remove(i);
			permutationsInternal(items, aux, resultPermutation, size);
			items.add(aux.remove(aux.size() - 1));
		}
	}

}
