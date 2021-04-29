package br.ufsc.gsigma.infrastructure.util;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

public class SerializableCountDownLatch extends CountDownLatch implements Serializable {

	private static final long serialVersionUID = 1L;

	public SerializableCountDownLatch(int count) {
		super(count);
	}

}
