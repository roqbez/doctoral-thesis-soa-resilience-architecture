package br.ufsc.gsigma.catalog.plugin.test;

import br.ufsc.gsigma.catalog.services.locator.CatalogServiceLocator;

public class Test {

	public static void main(String[] args) {

		System.out.println(CatalogServiceLocator.get().getProcessById(1).getName());
	}

}
