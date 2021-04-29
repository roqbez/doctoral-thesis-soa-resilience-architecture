package br.ufsc.gsigma.infrastructure.util;

public abstract class DerbyUtil {

	public static void silentLogging() {
		System.setProperty("derby.stream.error.field", "br.ufsc.gsigma.infrastructure.util.NullOutputStream.INSTANCE");
	}

}
