package br.ufsc.gsigma.servicediscovery.indexing;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.search.filter.FullTextFilter;
import org.hibernate.search.query.engine.spi.FacetManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.FetchOptions;
import org.infinispan.query.ResultIterator;
import org.infinispan.query.SearchManager;

import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequestItem;

@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public class DiscoveryCacheQuery implements CacheQuery {

	private CacheQuery delegate;

	private Query luceneQuery;

	private int maxResults;

	private String[] projections;

	private SearchManager searchManager;

	private List<DiscoveryRequestItem> itens;

	private String participantName;

	public DiscoveryCacheQuery(SearchManager searchManager, Query luceneQuery, String[] projections, List<DiscoveryRequestItem> itens, String participantName) {

		this.searchManager = searchManager;

		this.delegate = searchManager.getQuery(luceneQuery, IndexedService.class);

		if (projections != null && projections.length > 0) {
			this.delegate.projection(projections);
		}

		this.luceneQuery = luceneQuery;
		this.projections = projections;

		this.itens = itens;
		this.participantName = participantName;
	}

	public Query getLuceneQuery() {
		return luceneQuery;
	}

	public DiscoveryCacheQuery changeLuceneQuery(Query luceneQuery) {
		DiscoveryCacheQuery r = new DiscoveryCacheQuery(this.searchManager, luceneQuery, this.projections, this.itens, this.participantName);
		r.maxResults(maxResults);
		return r;
	}

	public String getParticipantName() {
		return participantName;
	}

	public String[] getProjections() {
		return projections;
	}

	public List<DiscoveryRequestItem> getItens() {
		return itens;
	}

	public void setProjections(String[] projections) {
		this.projections = projections;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public CacheQuery maxResults(int numResults) {
		this.maxResults = numResults;
		return delegate.maxResults(numResults);
	}

	public void forEach(Consumer action) {
		delegate.forEach(action);
	}

	public List<Object> list() {
		return delegate.list();
	}

	public ResultIterator iterator(FetchOptions fetchOptions) {
		return delegate.iterator(fetchOptions);
	}

	public ResultIterator iterator() {
		return delegate.iterator();
	}

	public Spliterator<Object> spliterator() {
		return delegate.spliterator();
	}

	public CacheQuery firstResult(int index) {
		return delegate.firstResult(index);
	}

	public FacetManager getFacetManager() {
		return delegate.getFacetManager();
	}

	public int getResultSize() {
		return delegate.getResultSize();
	}

	public Explanation explain(int documentId) {
		return delegate.explain(documentId);
	}

	public CacheQuery sort(Sort s) {
		return delegate.sort(s);
	}

	public CacheQuery projection(String... fields) {
		return delegate.projection(fields);
	}

	public FullTextFilter enableFullTextFilter(String name) {
		return delegate.enableFullTextFilter(name);
	}

	public CacheQuery disableFullTextFilter(String name) {
		return delegate.disableFullTextFilter(name);
	}

	public CacheQuery filter(Filter f) {
		return delegate.filter(f);
	}

	public CacheQuery timeout(long timeout, TimeUnit timeUnit) {
		return delegate.timeout(timeout, timeUnit);
	}

	public CacheQuery getDelegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return luceneQuery.toString();
	}

}
