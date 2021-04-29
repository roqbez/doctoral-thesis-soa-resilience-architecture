package br.ufsc.gsigma.infrastructure.util.collections;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

public class ManyToMany<K, V> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final SetMultimap<K, V> keysToValues = LinkedHashMultimap.create();

	private final SetMultimap<V, K> valuesToKeys = LinkedHashMultimap.create();

	public Set<K> keySet() {
		return Collections.unmodifiableSet(keysToValues.keySet());
	}

	public Set<V> valuesSet() {
		return Collections.unmodifiableSet(valuesToKeys.keySet());
	}

	public Set<V> getValues(K key) {
		return Collections.unmodifiableSet(keysToValues.get(key));
	}

	public Set<K> getKeys(V value) {
		return Collections.unmodifiableSet(valuesToKeys.get(value));
	}

	public boolean put(K key, V value) {
		return keysToValues.put(key, value) && valuesToKeys.put(value, key);
	}

	public boolean remove(K key, V value) {
		return keysToValues.remove(key, value) && valuesToKeys.remove(value, key);
	}

	public boolean putAll(K key, Iterable<? extends V> values) {
		boolean changed = false;
		for (V value : values) {
			changed = put(key, value) || changed;
		}
		return changed;
	}
}