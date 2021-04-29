package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.CorrelatorMessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.utils.uuid.UUID;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

@Indexed
public class InfinispanCorrelatorDAO implements CorrelatorDAO, InfinispanObject<String> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(InfinispanCorrelatorDAO.class);

	private String id;

	@Field(analyze = Analyze.NO)
	private String correlatorId;

	@Field(analyze = Analyze.NO)
	private String processId;

	public InfinispanCorrelatorDAO(String correlatorId, InfinispanProcessDAO process) {
		this.id = new UUID().toString();
		this.correlatorId = correlatorId;
		this.processId = process.getId();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getCorrelatorId() {
		return correlatorId;
	}

	@Override
	public void setCorrelatorId(String correlatorId) {
		this.correlatorId = correlatorId;
	}

	public String getCorrelatorKey() {
		return correlatorId;
	}

	@Override
	public void addRoute(String routeGroupId, ProcessInstanceDAO target, int index, CorrelationKeySet correlationKeySet, String routePolicy) {

		InfinispanMessageRouteDAO mr = new InfinispanMessageRouteDAO(correlationKeySet, routeGroupId, index, (InfinispanProcessInstanceDAO) target, this, routePolicy);

		if (logger.isDebugEnabled()) {
			logger.debug("addRoute --> " + mr);
		}

		InfinispanDatabase.addCacheEntry(mr);
	}

	@Override
	public List<MessageRouteDAO> findRoute(CorrelationKeySet correlationKeySet) {
		List<MessageRouteDAO> result = findRouteInternal(correlationKeySet);
		return result;
	}

	private List<MessageRouteDAO> findRouteInternal(CorrelationKeySet correlationKeySet) {

		ProcessDAO process = InfinispanDatabase.getCacheEntry(InfinispanProcessDAO.class, processId);

		BooleanQuery.Builder qb = new BooleanQuery.Builder();

		for (CorrelationKeySet c : correlationKeySet.findSubSets()) {
			qb.add(new TermQuery(new Term("correlationKey", c.toCanonicalString())), Occur.SHOULD);
		}

		List<InfinispanMessageRouteDAO> candidateRoutes = InfinispanDatabase.query(InfinispanMessageRouteDAO.class, //
				new TermQuery(new Term("processType", process.getType().toString())), //
				new TermQuery(new Term("correlatorKey", getCorrelatorKey())), //
				qb.build());

		if (candidateRoutes.size() > 0) {
			List<MessageRouteDAO> matchingRoutes = new ArrayList<MessageRouteDAO>();
			boolean routed = false;
			for (MessageRouteDAO route : candidateRoutes) {
				if ("all".equals(route.getRoute())) {
					matchingRoutes.add(route);
				} else {
					if (!routed) {
						matchingRoutes.add(route);
					}
					routed = true;
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("findRoute (" + correlationKeySet.toCanonicalString() + ") --> " + matchingRoutes);
			}

			return matchingRoutes;

		} else {

			if (logger.isDebugEnabled()) {
				logger.debug("findRoute (" + correlationKeySet.toCanonicalString() + ") --> NO ROUTES FOUND ");
			}

			return null;
		}
	}

	@Override
	public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
		((InfinispanProcessInstanceDAO) target).removeRoutes(routeGroupId);
	}

	void removeLocalRoutes(String routeGroupId, ProcessInstanceDAO target) {
		for (MessageRouteDAO mr : getAllRoutes()) {
			if (mr.getGroupId().equals(routeGroupId) && mr.getTargetInstance().equals(target)) {
				InfinispanDatabase.removeCacheEntry((InfinispanMessageRouteDAO) mr);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection<MessageRouteDAO> getAllRoutes() {
		Collection routes = (Collection) InfinispanDatabase.query(InfinispanMessageRouteDAO.class, new Term("correlatorId", getId()));
		return routes;
	}

	@Override
	public boolean checkRoute(CorrelationKeySet correlationKeySet) {
		return true;
	}

	@Override
	public void enqueueMessage(MessageExchangeDAO mex, CorrelationKeySet correlationKeySet) {

		InfinispanMessageExchangeDAO mexImpl = (InfinispanMessageExchangeDAO) mex;

		mexImpl.setCorrelationKeySet(correlationKeySet);
		mexImpl.setCorrelator(this);

		if (logger.isDebugEnabled()) {
			logger.debug("enqueueMessage (correlatorId=" + getId() + ", correlationKeys=" + correlationKeySet.toCanonicalString() + ") --> " + mexImpl);
		}

		InfinispanDatabase.addCacheEntry(mexImpl);
	}

	@Override
	public MessageExchangeDAO dequeueMessage(CorrelationKeySet correlationKeySet) {

		InfinispanMessageExchangeDAO mexImpl = InfinispanDatabase.querySingleResult(InfinispanMessageExchangeDAO.class, //
				new Term("correlatorId", getId()), //
				new Term("correlationKeys", correlationKeySet.toCanonicalString()));

		if (mexImpl != null) {

			InfinispanDatabase.removeCacheEntry(mexImpl);

			if (logger.isDebugEnabled()) {
				logger.debug("dequeueMessage (correlatorId=" + getId() + ", correlationKeys=" + correlationKeySet.toCanonicalString() + ") --> " + mexImpl);
			}

			return mexImpl;
		} else {

			// if (logger.isDebugEnabled()) {
			// logger.debug("dequeueMessage --> MEX NOT FOUND for correlationKeySet=" + correlationKeySet.toCanonicalString());
			// }

			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<CorrelatorMessageDAO> getAllMessages() {
		Collection messages = (Collection) InfinispanDatabase.query(InfinispanMessageExchangeDAO.class, new Term("correlatorId", getId()));
		return messages;
	}

}
