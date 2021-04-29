package br.ufsc.gsigma.servicediscovery.support.struct;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;

public class GroupStructure extends Structure implements List<ConnectableComponent> {

	private static final long serialVersionUID = 1L;

	private boolean leaf;

	@Override
	protected String getLabel() {
		return "";
	}

	@Override
	protected void appendChilds(StringBuilder sb, Integer depth) {
		super.appendChilds(sb, depth);
	}

	public GroupStructure(List<ConnectableComponent> components) {
		for (ConnectableComponent c : components) {
			getChilds().add(c);
		}
		this.leaf = true;
	}

	public GroupStructure(Collection<List<ConnectableComponent>> groupsOfComponents) {
		for (List<ConnectableComponent> l : groupsOfComponents) {
			getChilds().add(new GroupStructure(l));
		}
	}

	public boolean add(ConnectableComponent e) {
		return getChilds().add(e);
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void forEach(Consumer<? super ConnectableComponent> action) {
		getChilds().forEach(action);
	}

	public int size() {
		return getChilds().size();
	}

	public boolean isEmpty() {
		return getChilds().isEmpty();
	}

	public boolean contains(Object o) {
		return getChilds().contains(o);
	}

	public Iterator<ConnectableComponent> iterator() {
		return getChilds().iterator();
	}

	public Object[] toArray() {
		return getChilds().toArray();
	}

	public <T> T[] toArray(T[] a) {
		return getChilds().toArray(a);
	}

	public boolean remove(Object o) {
		return getChilds().remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return getChilds().containsAll(c);
	}

	public boolean addAll(Collection<? extends ConnectableComponent> c) {
		return getChilds().addAll(c);
	}

	public boolean addAll(int index, Collection<? extends ConnectableComponent> c) {
		return getChilds().addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return getChilds().removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return getChilds().retainAll(c);
	}

	public void replaceAll(UnaryOperator<ConnectableComponent> operator) {
		getChilds().replaceAll(operator);
	}

	public boolean removeIf(Predicate<? super ConnectableComponent> filter) {
		return getChilds().removeIf(filter);
	}

	public void sort(Comparator<? super ConnectableComponent> c) {
		getChilds().sort(c);
	}

	public void clear() {
		getChilds().clear();
	}

	public boolean equals(Object o) {
		return getChilds().equals(o);
	}

	public int hashCode() {
		return getChilds().hashCode();
	}

	public ConnectableComponent get(int index) {
		return getChilds().get(index);
	}

	public ConnectableComponent set(int index, ConnectableComponent element) {
		return getChilds().set(index, element);
	}

	public void add(int index, ConnectableComponent element) {
		getChilds().add(index, element);
	}

	public Stream<ConnectableComponent> stream() {
		return getChilds().stream();
	}

	public ConnectableComponent remove(int index) {
		return getChilds().remove(index);
	}

	public Stream<ConnectableComponent> parallelStream() {
		return getChilds().parallelStream();
	}

	public int indexOf(Object o) {
		return getChilds().indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return getChilds().lastIndexOf(o);
	}

	public ListIterator<ConnectableComponent> listIterator() {
		return getChilds().listIterator();
	}

	public ListIterator<ConnectableComponent> listIterator(int index) {
		return getChilds().listIterator(index);
	}

	public List<ConnectableComponent> subList(int fromIndex, int toIndex) {
		return getChilds().subList(fromIndex, toIndex);
	}

	public Spliterator<ConnectableComponent> spliterator() {
		return getChilds().spliterator();
	}
}
