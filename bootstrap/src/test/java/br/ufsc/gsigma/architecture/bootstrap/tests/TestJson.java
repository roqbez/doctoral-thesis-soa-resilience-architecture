package br.ufsc.gsigma.architecture.bootstrap.tests;

import br.ufsc.services.core.util.json.JsonUtil;

public class TestJson {

	public static void main(String[] args) {

		System.out.println(JsonUtil.decode("[ \"Ford\", \"BMW\", \"Fiat\" ]", String.class));

	}

}
