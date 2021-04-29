package br.ufsc.gsigma.infrastructure.util;

public class ObjectHolder<T> {

	private T object;

	public ObjectHolder() {
	}

	public ObjectHolder(T object) {
		this.object = object;
	}

	public T get() {
		return object;
	}

	public void set(T object) {
		this.object = object;
	}

}
