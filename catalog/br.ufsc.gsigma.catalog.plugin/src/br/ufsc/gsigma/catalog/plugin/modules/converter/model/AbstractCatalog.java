package br.ufsc.gsigma.catalog.plugin.modules.converter.model;

public abstract class AbstractCatalog {

	private String id;
	private String name;

	public AbstractCatalog() {

	}

	public AbstractCatalog(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract boolean isEmpty();

}
