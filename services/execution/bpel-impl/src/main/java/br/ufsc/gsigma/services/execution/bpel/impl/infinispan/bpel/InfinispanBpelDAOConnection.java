package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ObjectUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessManagementDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.utils.ISO8601DateParser;
import org.springframework.util.CollectionUtils;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public class InfinispanBpelDAOConnection implements BpelDAOConnection {

	public static final InfinispanBpelDAOConnection INSTANCE = new InfinispanBpelDAOConnection();

	private InfinispanBpelDAOConnection() {
	}

	@Override
	public ProcessDAO getProcess(QName processId) {
		InfinispanProcessDAO process = InfinispanDatabase.getCacheEntry(InfinispanProcessDAO.class, processId.toString());
		return process;
	}

	@Override
	public int getNumInstances(QName processId) {
		ProcessDAO process = getProcess(processId);
		if (process != null)
			return process.getNumInstances();
		else
			return -1;
	}

	@Override
	public ScopeDAO getScope(Long siidl) {
		InfinispanScopeDAO scope = InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, siidl);
		return scope;
	}

	@Override
	public ProcessInstanceDAO getInstance(Long iid) {
		InfinispanProcessInstanceDAO instance = InfinispanDatabase.getCacheEntry(InfinispanProcessInstanceDAO.class, iid);
		return instance;
	}

	@Override
	public MessageExchangeDAO getMessageExchange(String mexid) {
		return InfinispanDatabase.getCacheEntry(InfinispanMessageExchangeDAO.class, mexid);
	}

	public MessageExchangeDAO createMessageExchange(char dir) {
		InfinispanMessageExchangeDAO ret = new InfinispanMessageExchangeDAO(dir);
		InfinispanDatabase.addCacheEntry(ret);
		return ret;
	}

	public ProcessDAO createProcess(QName pid, QName type, String guid, long version) {
		InfinispanProcessDAO ret = new InfinispanProcessDAO(pid, type, guid, version);
		InfinispanDatabase.addCacheEntry(ret);
		return ret;
	}

	public ProcessDAO createTransientProcess(Serializable id) {
		InfinispanProcessDAO ret = new InfinispanProcessDAO(null, null, null, 0);
		// ret.setId(String.valueOf(id));

		return ret;
	}

	@Override
	public Collection<CorrelationSetDAO> getActiveCorrelationSets() {

		List<InfinispanProcessInstanceDAO> instances = InfinispanDatabase.query(InfinispanProcessInstanceDAO.class, //
				new Term("state", String.valueOf(ProcessState.STATE_ACTIVE)));

		Collection<CorrelationSetDAO> result = new HashSet<CorrelationSetDAO>();

		for (InfinispanProcessInstanceDAO i : instances) {
			for (ScopeDAO s : i.getScopes()) {
				result.addAll(s.getCorrelationSets());
			}
		}

		return result;
	}

	@Override
	public Map<Long, Collection<CorrelationSetDAO>> getCorrelationSets(Collection<ProcessInstanceDAO> instances) {

		BooleanQuery.Builder b = new BooleanQuery.Builder();

		for (ProcessInstanceDAO i : instances) {
			b.add(new TermQuery(new Term("processInstanceId", String.valueOf(i.getInstanceId()))), Occur.SHOULD);
		}

		List<InfinispanScopeDAO> scopes = InfinispanDatabase.query(InfinispanScopeDAO.class, b.build());

		Map<Long, Collection<CorrelationSetDAO>> map = new HashMap<Long, Collection<CorrelationSetDAO>>();

		for (InfinispanScopeDAO scope : scopes) {
			Long id = scope.getProcessInstance().getInstanceId();
			Collection<CorrelationSetDAO> existing = map.get(id);
			if (existing == null) {
				existing = new ArrayList<CorrelationSetDAO>();
				map.put(id, existing);
			}
			existing.addAll(scope.getCorrelationSets());
		}
		return map;
	}

	public void insertBpelEvent(BpelEvent event, ProcessDAO process, ProcessInstanceDAO instance) {
		s_insertBpelEvent(event, process, instance);
	}

	public static void s_insertBpelEvent(BpelEvent event, ProcessDAO process, ProcessInstanceDAO instance) {

		InfinispanEventDAO eventDao = new InfinispanEventDAO();
		eventDao.setTstamp(new Timestamp(System.currentTimeMillis()));
		eventDao.setType(BpelEvent.eventName(event));
		String evtStr = event.toString();
		eventDao.setDetail(evtStr.substring(0, Math.min(254, evtStr.length())));

		if (process != null) {
			eventDao.setProcess((InfinispanProcessDAO) process);
		}

		if (instance != null) {
			eventDao.setInstance(((InfinispanProcessInstanceDAO) instance));
		}

		if (event instanceof ScopeEvent) {
			eventDao.setScopeId(((ScopeEvent) event).getScopeId());
		}

		eventDao.setEvent(event);

		InfinispanDatabase.addCacheEntry(eventDao);
	}

	@Override
	public void close() {
	}

	@Override
	public List<BpelEvent> bpelEventQuery(InstanceFilter ifilter, BpelEventFilter efilter) {

		List<Query> queries = new LinkedList<Query>();

		if (ifilter != null) {
			if (!CollectionUtils.isEmpty(ifilter.getIidFilter())) {

				org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

				for (String iid : ifilter.getIidFilter()) {
					b.add(new TermQuery(new Term("processInstanceId", iid)), Occur.SHOULD);
				}
				queries.add(b.build());
			}

			if (!CollectionUtils.isEmpty(ifilter.getPidFilter())) {

				org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

				for (String pid : ifilter.getPidFilter()) {
					b.add(new TermQuery(new Term("processId", pid)), Occur.SHOULD);
				}
				queries.add(b.build());
			}
		}

		Collection<InfinispanEventDAO> events = (Collection<InfinispanEventDAO>) InfinispanDatabase.query(InfinispanEventDAO.class, queries.toArray(new Query[queries.size()]));

		List<BpelEvent> result = new ArrayList<BpelEvent>(events.size());

		for (InfinispanEventDAO ievt : events) {
			result.add(ievt.getEvent());
		}

		Collections.sort(result, new Comparator<BpelEvent>() {
			@Override
			public int compare(BpelEvent e1, BpelEvent e2) {
				return ObjectUtils.compare(e1.getTimestamp(), e2.getTimestamp());
			}
		});

		return result;
	}

	@Override
	public List<Date> bpelEventTimelineQuery(InstanceFilter ifilter, BpelEventFilter efilter) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter filter) {

		if (filter.getLimit() == 0) {
			return Collections.EMPTY_LIST;
		}
		List<ProcessInstanceDAO> matched = new ArrayList<ProcessInstanceDAO>();
		// Selecting
		selectionCompleted: for (InfinispanProcessDAO proc : InfinispanDatabase.getCacheEntries(InfinispanProcessDAO.class)) {
			boolean pmatch = true;
			if (filter.getNameFilter() != null && !equalsOrWildcardMatch(filter.getNameFilter(), proc.getProcessId().getLocalPart()))
				pmatch = false;
			if (filter.getNamespaceFilter() != null && !equalsOrWildcardMatch(filter.getNamespaceFilter(), proc.getProcessId().getNamespaceURI()))
				pmatch = false;

			if (pmatch) {
				for (ProcessInstanceDAO inst : proc.getInstances()) {
					boolean match = true;

					if (filter.getStatusFilter() != null) {
						boolean statusMatch = false;
						for (Short status : filter.convertFilterState()) {
							if (inst.getState() == status.byteValue())
								statusMatch = true;
						}
						if (!statusMatch)
							match = false;
					}
					if (filter.getStartedDateFilter() != null && !dateMatch(filter.getStartedDateFilter(), inst.getCreateTime(), filter))
						match = false;
					if (filter.getLastActiveDateFilter() != null && !dateMatch(filter.getLastActiveDateFilter(), inst.getLastActiveTime(), filter))
						match = false;

					// if (filter.getPropertyValuesFilter() != null) {
					// for (Map.Entry propEntry : filter.getPropertyValuesFilter().entrySet()) {
					// boolean entryMatched = false;
					// for (ProcessPropertyDAO prop : proc.getProperties()) {
					// if (prop.getName().equals(propEntry.getKey())
					// && (propEntry.getValue().equals(prop.getMixedContent())
					// || propEntry.getValue().equals(prop.getSimpleContent()))) {
					// entryMatched = true;
					// }
					// }
					// if (!entryMatched) {
					// match = false;
					// }
					// }
					// }

					if (match) {
						matched.add(inst);
						if (matched.size() == filter.getLimit()) {
							break selectionCompleted;
						}
					}
				}
			}
		}
		// And ordering
		if (filter.getOrders() != null) {
			final List<String> orders = filter.getOrders();

			Collections.sort(matched, new Comparator<ProcessInstanceDAO>() {
				public int compare(ProcessInstanceDAO o1, ProcessInstanceDAO o2) {
					for (String orderKey : orders) {
						int result = compareInstanceUsingKey(orderKey, o1, o2);
						if (result != 0)
							return result;
					}
					return 0;
				}
			});
		}

		return matched;
	}

	@Override
	public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
		return null;
	}

	@Override
	public ProcessManagementDAO getProcessManagement() {
		return new InfinispanProcessManagementDAO();
	}

	private boolean equalsOrWildcardMatch(String s1, String s2) {
		if (s1 == null || s2 == null)
			return false;
		if (s1.equals(s2))
			return true;
		if (s1.endsWith("*")) {
			if (s2.startsWith(s1.substring(0, s1.length() - 1)))
				return true;
		}
		if (s2.endsWith("*")) {
			if (s1.startsWith(s2.substring(0, s2.length() - 1)))
				return true;
		}
		return false;
	}

	private boolean dateMatch(List<String> dateFilters, Date instanceDate, InstanceFilter filter) {
		boolean match = true;
		for (String ddf : dateFilters) {
			String isoDate = ISO8601DateParser.format(instanceDate);
			String critDate = Filter.getDateWithoutOp(ddf);
			if (ddf.startsWith("=")) {
				if (!isoDate.startsWith(critDate))
					match = false;
			} else if (ddf.startsWith("<=")) {
				if (!isoDate.startsWith(critDate) && isoDate.compareTo(critDate) > 0)
					match = false;
			} else if (ddf.startsWith(">=")) {
				if (!isoDate.startsWith(critDate) && isoDate.compareTo(critDate) < 0)
					match = false;
			} else if (ddf.startsWith("<")) {
				if (isoDate.compareTo(critDate) > 0)
					match = false;
			} else if (ddf.startsWith(">")) {
				if (isoDate.compareTo(critDate) < 0)
					match = false;
			}
		}
		return match;
	}

	private int compareInstanceUsingKey(String key, ProcessInstanceDAO instanceDAO1, ProcessInstanceDAO instanceDAO2) {
		String s1 = null;
		String s2 = null;
		boolean ascending = true;
		String orderKey = key;
		if (key.startsWith("+") || key.startsWith("-")) {
			orderKey = key.substring(1, key.length());
			if (key.startsWith("-"))
				ascending = false;
		}
		ProcessDAO process1 = getProcess(instanceDAO1.getProcess().getProcessId());
		ProcessDAO process2 = getProcess(instanceDAO2.getProcess().getProcessId());
		if ("pid".equals(orderKey)) {
			s1 = process1.getProcessId().toString();
			s2 = process2.getProcessId().toString();
		} else if ("name".equals(orderKey)) {
			s1 = process1.getProcessId().getLocalPart();
			s2 = process2.getProcessId().getLocalPart();
		} else if ("namespace".equals(orderKey)) {
			s1 = process1.getProcessId().getNamespaceURI();
			s2 = process2.getProcessId().getNamespaceURI();
		} else if ("version".equals(orderKey)) {
			s1 = "" + process1.getVersion();
			s2 = "" + process2.getVersion();
		} else if ("status".equals(orderKey)) {
			s1 = "" + instanceDAO1.getState();
			s2 = "" + instanceDAO2.getState();
		} else if ("started".equals(orderKey)) {
			s1 = ISO8601DateParser.format(instanceDAO1.getCreateTime());
			s2 = ISO8601DateParser.format(instanceDAO2.getCreateTime());
		} else if ("last-active".equals(orderKey)) {
			s1 = ISO8601DateParser.format(instanceDAO1.getLastActiveTime());
			s2 = ISO8601DateParser.format(instanceDAO2.getLastActiveTime());
		}
		if (ascending)
			return s1.compareTo(s2);
		else
			return s2.compareTo(s1);
	}

}
