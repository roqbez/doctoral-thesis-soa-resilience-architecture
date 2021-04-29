package br.ufsc.gsigma.servicediscovery.support.struct;

public class EndStructure extends Structure {

	private static final long serialVersionUID = 1L;

	public static final EndStructure INSTANCE = new EndStructure();

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EndStructure;
	}

}
