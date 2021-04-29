package br.ufsc.gsigma.infrastructure.util;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {

	public static final OutputStream INSTANCE = new NullOutputStream();

	public void write(int b) {
	}
}
