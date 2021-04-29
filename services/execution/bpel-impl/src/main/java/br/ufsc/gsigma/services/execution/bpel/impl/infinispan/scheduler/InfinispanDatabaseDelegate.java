package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.scheduler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

import br.ufsc.gsigma.services.execution.bpel.impl.ExecutionJob;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.DatabaseDelegate;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.DatabaseException;
import br.ufsc.gsigma.services.execution.bpel.ode.scheduler.Job;

public class InfinispanDatabaseDelegate implements DatabaseDelegate {

	private static final Logger logger = Logger.getLogger(InfinispanDatabaseDelegate.class);

	private InfinispanDatabase database;

	private Cache<String, InfinispanSchedulerJob> cache;

	public InfinispanDatabaseDelegate(InfinispanDatabase database) {
		this.database = database;
		this.cache = this.database.getCache(InfinispanSchedulerJob.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<String> getNodeIds() throws DatabaseException {

		SearchManager searchManager = Search.getSearchManager(cache);
		CacheQuery cacheQuery = searchManager.getQuery(new MatchAllDocsQuery(), InfinispanSchedulerJob.class);
		cacheQuery.projection("nodeId");

		LinkedHashSet<String> nodes = new LinkedHashSet<String>();

		for (Object o : cacheQuery.list()) {
			String nodeId = (String) ((Object[]) o)[0];
			nodes.add(nodeId);
		}

		return new ArrayList<String>(nodes);
	}

	@Override
	public List<Job> dequeueImmediate(String nodeId, long maxtime, int maxjobs) throws DatabaseException {

		SearchManager searchManager = Search.getSearchManager(cache);

		org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

		NumericRangeQuery<Long> range = NumericRangeQuery.newLongRange("schedDate", null, maxtime, false, true);

		b.add(new TermQuery(new Term("nodeId", nodeId)), Occur.MUST);
		b.add(range, Occur.MUST);

		// Query q = b.build();
		Query q = range;

		CacheQuery<InfinispanSchedulerJob> cacheQuery = searchManager.getQuery(q, InfinispanSchedulerJob.class);

		cacheQuery.maxResults(maxjobs);

		List<InfinispanSchedulerJob> queryResult = cacheQuery.list();

		List<Job> result = new ArrayList<Job>(queryResult.size());

		for (InfinispanSchedulerJob infinispanSchedulerJob : queryResult) {

			InfinispanSchedulerJobDetails infinispanSchedulerJobDetails = infinispanSchedulerJob.getDetail();

			Scheduler.JobDetails details = null;

			details = new Scheduler.JobDetails();
			details.instanceId = infinispanSchedulerJobDetails.getInstanceId();
			details.mexId = infinispanSchedulerJobDetails.getMexId();
			details.processId = infinispanSchedulerJobDetails.getProcessId();
			details.type = infinispanSchedulerJobDetails.getType();
			details.channel = infinispanSchedulerJobDetails.getChannel();
			details.correlatorId = infinispanSchedulerJobDetails.getCorrelatorId();
			details.correlationKeySet = infinispanSchedulerJobDetails.getCorrelationKeySet();
			details.retryCount = infinispanSchedulerJobDetails.getRetryCount();
			details.inMem = infinispanSchedulerJobDetails.getInMem();
			details.detailsExt = infinispanSchedulerJobDetails.getDetailsExt();

			// For compatibility reasons, we check whether there are entries inside
			// jobDetailsExt blob, which correspond to extracted entries. If so, we
			// use them.

			Map<String, Object> detailsExt = details.getDetailsExt();
			if (detailsExt.get("type") != null) {
				details.type = (String) detailsExt.get("type");
			}
			if (detailsExt.get("iid") != null) {
				details.instanceId = (Long) detailsExt.get("iid");
			}
			if (detailsExt.get("pid") != null) {
				details.processId = (String) detailsExt.get("pid");
			}
			if (detailsExt.get("inmem") != null) {
				details.inMem = (Boolean) detailsExt.get("inmem");
			}
			if (detailsExt.get("ckey") != null) {
				details.correlationKeySet = (String) detailsExt.get("ckey");
			}
			if (detailsExt.get("channel") != null) {
				details.channel = (String) detailsExt.get("channel");
			}
			if (detailsExt.get("mexid") != null) {
				details.mexId = (String) detailsExt.get("mexid");
			}
			if (detailsExt.get("correlatorId") != null) {
				details.correlatorId = (String) detailsExt.get("correlatorId");
			}
			if (detailsExt.get("retryCount") != null) {
				details.retryCount = Integer.parseInt((String) detailsExt.get("retryCount"));
			}

			Job job = new ExecutionJob( //
					infinispanSchedulerJob.getSchedDate(), //
					infinispanSchedulerJob.getJobId(), //
					infinispanSchedulerJob.isTransacted(), //
					details, //
					infinispanSchedulerJob.getExecutionContext());

			result.add(job);
		}

		if (logger.isDebugEnabled() && !result.isEmpty()) {
			StringBuilder sb = new StringBuilder("dequeueImmediate (nodeId=" + nodeId + ", maxtime=" + maxtime + ", maxjobs=" + maxjobs + ")");
			for (Job e : result) {
				sb.append("\n\t" + e);
			}
			logger.debug(sb.toString());
		}

		return result;
	}

	@Override
	public boolean insertJob(Job job, String nodeId, boolean loaded) throws DatabaseException {

		if (logger.isDebugEnabled())
			logger.debug("insertJob " + job.getJobId() + " on node " + nodeId + " loaded=" + loaded + " --> " + job);

		InfinispanSchedulerJob infinispanSchedulerJob = new InfinispanSchedulerJob();
		infinispanSchedulerJob.setJobId(job.getJobId());
		infinispanSchedulerJob.setNodeId(nodeId);
		infinispanSchedulerJob.setSchedDate(job.schedDate);
		infinispanSchedulerJob.setLoaded(loaded);
		infinispanSchedulerJob.setTransacted(job.isTransacted());

		updateExecutionContext(infinispanSchedulerJob, job);

		JobDetails details = job.getDetail();

		InfinispanSchedulerJobDetails infinispanSchedulerJobDetails = new InfinispanSchedulerJobDetails();
		infinispanSchedulerJobDetails.setInstanceId(details.instanceId);
		infinispanSchedulerJobDetails.setMexId(details.mexId);
		infinispanSchedulerJobDetails.setProcessId(details.processId);
		infinispanSchedulerJobDetails.setType(details.type);
		infinispanSchedulerJobDetails.setChannel(details.channel);
		infinispanSchedulerJobDetails.setCorrelatorId(details.correlatorId);
		infinispanSchedulerJobDetails.setCorrelationKeySet(details.correlationKeySet);
		infinispanSchedulerJobDetails.setRetryCount(details.retryCount);
		infinispanSchedulerJobDetails.setInMem(details.inMem);
		infinispanSchedulerJobDetails.setDetailsExt(details.detailsExt);

		infinispanSchedulerJob.setDetail(infinispanSchedulerJobDetails);

		cache.put(infinispanSchedulerJob.getJobId(), infinispanSchedulerJob);

		return true;
	}

	private void updateExecutionContext(InfinispanSchedulerJob infinispanSchedulerJob, Job job) {
		if (job instanceof ExecutionJob && ((ExecutionJob) job).getExecutionContext() != null) {
			infinispanSchedulerJob.setExecutionContext(((ExecutionJob) job).getExecutionContext());
		}
	}

	@Override
	public boolean updateJob(Job job) throws DatabaseException {

		if (logger.isDebugEnabled())
			logger.debug("updateJob " + job.getJobId() + " retryCount=" + job.getDetail().getRetryCount());

		InfinispanSchedulerJob infinispanSchedulerJob = cache.get(job.getJobId());

		if (infinispanSchedulerJob != null) {
			infinispanSchedulerJob.setSchedDate(job.schedDate);
			infinispanSchedulerJob.getDetail().setRetryCount(job.getDetail().getRetryCount());
			updateExecutionContext(infinispanSchedulerJob, job);

			cache.put(infinispanSchedulerJob.getId(), infinispanSchedulerJob);

			return true;

		} else {
			return false;
		}
	}

	@Override
	public boolean deleteJob(String jobid, String nodeId) throws DatabaseException {

		InfinispanSchedulerJob job = cache.get(jobid);

		// if (job != null && nodeId.equals(job.getNodeId())) {
		if (job != null) {
			return cache.remove(jobid) != null;
		} else {
			return false;
		}
	}

	@Override
	public int updateAssignToNode(String nodeId, int x, int y, long maxtime) throws DatabaseException {

		List<InfinispanSchedulerJob> jobs = InfinispanDatabase.query(InfinispanSchedulerJob.class, new Term("nodeId", "NULL"));

		for (InfinispanSchedulerJob job : jobs) {
			job.setNodeId(nodeId);
			InfinispanDatabase.updateCacheEntry(job);
		}

		return jobs.size();
	}

	@Override
	public int updateReassign(String oldnode, String newnode) throws DatabaseException {
		// throw new UnsupportedOperationException("Not implemented yet");
		return 0;
	}

	@Override
	public void acquireTransactionLocks() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
