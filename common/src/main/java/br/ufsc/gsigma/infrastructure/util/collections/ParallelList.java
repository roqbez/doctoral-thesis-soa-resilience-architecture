package br.ufsc.gsigma.infrastructure.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ParallelList<T> implements Iterable<List<T>> {

	private final List<List<T>> lists;

	@SafeVarargs
	public ParallelList(List<T>... lists) {
		this.lists = new ArrayList<List<T>>(lists.length);
		for (List<T> l : lists)
			this.lists.add(new ArrayList<T>(l));
	}

	public ParallelList(Collection<List<T>> lists) {
		this(lists, 0);
	}

	public ParallelList(Collection<List<T>> lists, int start) {
		this.lists = new ArrayList<List<T>>(lists.size());
		for (List<T> l : lists)
			this.lists.add(new ArrayList<T>(l.subList(start, l.size())));
	}

	public int getLastCommonIndex() {

		Integer min = null;

		for (List<T> list : lists) {

			if (min == null)
				min = list.size();
			else
				min = Math.min(min, list.size());
		}

		return min - 1;
	}

	public List<List<T>> getLists() {
		return lists;
	}

	public Iterator<List<T>> iterator() {
		return new Iterator<List<T>>() {
			private int loc = 0;

			public boolean hasNext() {
				boolean hasNext = false;
				for (List<T> list : lists) {
					hasNext |= (loc < list.size());
				}
				return hasNext;
			}

			public List<T> next() {
				List<T> vals = new ArrayList<T>(lists.size());
				for (int i = 0; i < lists.size(); i++) {
					vals.add(loc < lists.get(i).size() ? lists.get(i).get(loc) : null);
				}
				loc++;
				return vals;
			}

			public void remove() {
				for (List<T> list : lists) {
					if (loc < list.size()) {
						list.remove(loc);
					}
				}
			}
		};
	}
}