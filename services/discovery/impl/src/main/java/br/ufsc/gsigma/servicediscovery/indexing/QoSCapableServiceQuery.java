package br.ufsc.gsigma.servicediscovery.indexing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;
import org.infinispan.Cache;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.support.QoSMinMax;

public class QoSCapableServiceQuery extends CustomScoreQuery {

	private QoSMinMax qoSMinMax;

	private Map<String, Double> qoSWeights;

	private Map<String, Double> docUtility = new HashMap<String, Double>();

	private Cache<String, DiscoveredService> servicesCache;

	private Map<String, QoSAttribute> qoSAttributes;

	public QoSCapableServiceQuery(Query subQuery, QoSMinMax qoSMinMax, Map<String, Double> qoSWeights, Cache<String, DiscoveredService> servicesCache, Map<String, QoSAttribute> qoSAttributes) {
		super(subQuery);
		this.qoSMinMax = qoSMinMax;
		this.qoSWeights = qoSWeights;
		this.servicesCache = servicesCache;
		this.qoSAttributes = qoSAttributes;
	}

	public QoSCapableServiceQuery changeSubQuery(Query subQuery) {
		return new QoSCapableServiceQuery(subQuery, qoSMinMax, qoSWeights, servicesCache, qoSAttributes);
	}

	protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {
		return new QoSCapableServiceScoreProvider(context, this);
	}

	public QoSMinMax getQoSMinMax() {
		return qoSMinMax;
	}

	public Map<String, Double> getQoSWeights() {
		return qoSWeights;
	}

	public Map<String, Double> getDocUtility() {
		return docUtility;
	}

	public Cache<String, DiscoveredService> getServicesCache() {
		return servicesCache;
	}

	public Map<String, QoSAttribute> getQoSAttributes() {
		return qoSAttributes;
	}

}