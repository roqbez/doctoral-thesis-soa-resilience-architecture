package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.infinispan.multimap.api.embedded.MultimapCache;

public class InfinispanManyToMany<K, V> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final MultimapCache<K, V> keysToValues;

	private final MultimapCache<V, K> valuesToKeys;

	public InfinispanManyToMany(MultimapCache<K, V> keysToValues, MultimapCache<V, K> valuesToKeys) {
		this.keysToValues = keysToValues;
		this.valuesToKeys = valuesToKeys;
	}

	public Collection<V> getValues(K key) {
		try {
			return Collections.unmodifiableCollection(keysToValues.get(key).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<K> getKeys(V value) {
		try {
			return Collections.unmodifiableCollection(valuesToKeys.get(value).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void put(K key, V value) {
		try {
			keysToValues.put(key, value).get();
			valuesToKeys.put(value, key).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void remove(K key, V value) {
		try {
			keysToValues.remove(key, value).get();
			valuesToKeys.remove(value, key).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void putAll(K key, Iterable<? extends V> values) {
		for (V value : values) {
			put(key, value);
		}
	}
}