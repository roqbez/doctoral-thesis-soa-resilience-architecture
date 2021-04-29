package br.ufsc.gsigma.servicediscovery.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BulkScorer;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

import br.ufsc.gsigma.servicediscovery.util.DiscoveryUtil;
import br.ufsc.gsigma.services.resilience.interfaces.ResilienceService;
import br.ufsc.gsigma.services.resilience.model.ServicesCheckResult;

public class ServicesCheckQuery extends Query {

	private static final Logger logger = Logger.getLogger(ServicesCheckQuery.class);

	private Query baseQuery;

	boolean rewritten = false;

	private ResilienceService resilienceService;

	private Set<String> availableEndpoints = new HashSet<String>();

	private Set<String> unavailableEndpoints = new HashSet<String>();

	public ServicesCheckQuery(Query baseQuery, ResilienceService resilienceService) {
		this.baseQuery = baseQuery;
		this.resilienceService = resilienceService;
	}

	@Override
	public String toString(String field) {
		return getClass().getName();
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {

		if (!rewritten) {

			Query query = baseQuery;
			for (Query rewrittenQuery = query.rewrite(reader); rewrittenQuery != query; rewrittenQuery = query.rewrite(reader)) {
				query = rewrittenQuery;
			}

			baseQuery = query;
			rewritten = true;
		}
		return this;
	}

	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
		Weight delegate = baseQuery.createWeight(searcher, needsScores);

		return new Weight(this) {

			@Override
			public void extractTerms(Set<Term> terms) {

			}

			@Override
			public Explanation explain(LeafReaderContext context, int doc) throws IOException {
				return delegate.explain(context, doc);
			}

			@Override
			public float getValueForNormalization() throws IOException {
				return delegate.getValueForNormalization();
			}

			@Override
			public void normalize(float norm, float boost) {
				delegate.normalize(norm, boost);
			}

			@Override
			public Scorer scorer(LeafReaderContext context) throws IOException {
				return delegate.scorer(context);
			}

			@Override
			public BulkScorer bulkScorer(LeafReaderContext context) throws IOException {

				Scorer scorer = scorer(context);

				if (scorer == null) {
					return null;
				}

				DocIdSetIterator iterator = scorer.iterator();

				LeafReader reader = context.reader();

				Map<String, Collection<Integer>> endpointsToCheckToDocs = new TreeMap<String, Collection<Integer>>();

				Set<String> serviceNamespaces = new HashSet<String>();

				for (int docID = iterator.nextDoc(); docID != DocIdSetIterator.NO_MORE_DOCS; docID = iterator.nextDoc()) {

					String serviceClassification = reader.getTermVector(docID, "serviceClassification").getMax().utf8ToString();

					serviceNamespaces.add(DiscoveryUtil.serviceClassificationToNamespace(serviceClassification));

					String serviceEndpointURL = reader.getTermVector(docID, "serviceEndpointURL").getMax().utf8ToString();

					Collection<Integer> docs = endpointsToCheckToDocs.get(serviceEndpointURL);

					if (docs == null) {
						docs = new HashSet<Integer>();
						endpointsToCheckToDocs.put(serviceEndpointURL, docs);
					}

					docs.add(docID);
				}

				if (serviceNamespaces.size() > 1) {
					throw new IllegalArgumentException("More than one namespace is not allowed for services check");
				}

				List<String> endpointsToCheck = new ArrayList<String>(endpointsToCheckToDocs.size());

				for (String endpoint : endpointsToCheckToDocs.keySet()) {
					if (!availableEndpoints.contains(endpoint) && !unavailableEndpoints.contains(endpoint)) {
						endpointsToCheck.add(endpoint);
					}
				}

				if (!endpointsToCheck.isEmpty()) {

					try {
						ServicesCheckResult checkResult = resilienceService.checkServicesEndpointsAvailable(endpointsToCheck, serviceNamespaces.iterator().next());

						availableEndpoints.addAll(checkResult.getAvailableServiceEndpointURLs());
						unavailableEndpoints.addAll(checkResult.getUnavailableServiceEndpointURLs());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

				Set<Integer> unavailableDocs = new HashSet<Integer>();

				for (String endpoint : unavailableEndpoints) {
					Collection<Integer> docs = endpointsToCheckToDocs.get(endpoint);
					if (docs != null) {
						unavailableDocs.addAll(docs);
					}
				}

				Bits acceptDocsBits = new Bits() {

					@Override
					public boolean get(int index) {
						return !unavailableDocs.contains(index);
					}

					@Override
					public int length() {
						return reader.maxDoc();
					}

				};

				// Renew the score
				scorer = scorer(context);

				return new DefaultBulkScorer(scorer) {
					@Override
					public void score(LeafCollector collector, Bits acceptDocs) throws IOException {
						super.score(collector, acceptDocsBits);
					}
				};
			}

		};
	}

}
