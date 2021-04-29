package br.ufsc.gsigma.infrastructure.tests;

import java.util.Arrays;
import java.util.List;

import br.ufsc.gsigma.infrastructure.util.collections.ParallelList;

public class TesteParallelList {

	public static void main(String[] args) {

		List<String> l1 = Arrays.asList("S1", "S2", "S3");

		List<String> l2 = Arrays.asList("S4", "S5", "S6", "S7");

		ParallelList<String> parallelList = new ParallelList<String>(Arrays.asList(l1, l2));

		for (List<String> l : parallelList) {
			System.out.println(l);
		}

	}
}
