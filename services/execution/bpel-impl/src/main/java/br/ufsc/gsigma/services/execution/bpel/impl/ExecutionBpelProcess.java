package br.ufsc.gsigma.services.execution.bpel.impl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis2.util.XMLUtils;
import org.apache.log4j.Logger;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.engine.BpelRuntimeContextImpl;
import org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl;
import org.apache.ode.bpel.engine.PartnerLinkMyRoleImpl.RoutingInfo;
import org.apache.ode.bpel.evt.NewProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.pmapi.ManagementException;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.bpel.runtime.Selector;
import org.apache.ode.bpel.runtime.channels.PickResponseChannel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;

public class ExecutionBpelProcess extends BpelProcess {

	private static final Logger logger = Logger.getLogger(ExecutionBpelProcess.class);

	private static final Method mResponseReceived;

	private static final Method mSetStatus;

	private static final Method mSetResponse;

	static {
		try {

			mResponseReceived = MyRoleMessageExchangeImpl.class.getDeclaredMethod("responseReceived");
			mResponseReceived.setAccessible(true);

			mSetStatus = MyRoleMessageExchangeImpl.class.getSuperclass().getDeclaredMethod("setStatus", Status.class);
			mSetStatus.setAccessible(true);

			mSetResponse = MyRoleMessageExchangeImpl.class.getSuperclass().getDeclaredMethod("setResponse", Message.class);
			mSetResponse.setAccessible(true);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ExecutionBpelProcess(ProcessConf conf) {
		super(conf);
	}

	@Override
	public void invokeProcess(MyRoleMessageExchangeImpl mex, final InvokeHandler invokeHandler) {

		InvokeHandler decorated = new InvokeHandler() {
			@Override
			public boolean invoke(PartnerLinkMyRoleImpl target, RoutingInfo routingInfo, boolean createInstance) {

				boolean answer = invokeHandler.invoke(target, routingInfo, createInstance);

				if (!answer) {
					try {
						logger.warn("Can't find any route for mex=" + mex + ", notifying waiting thread");

						Message resp = mex.createMessage(new QName("", "response"));

						Document doc = XMLUtils.newDocument();
						Element element = doc.createElement("message");

						Element payload = doc.createElement("payload");
						element.appendChild(payload);

						Element ignore = doc.createElement("ignore");
						ignore.setTextContent("true");

						payload.appendChild(ignore);

						resp.setMessage(element);

						mSetStatus.invoke(mex, Status.REQUEST);
						mSetResponse.invoke(mex, resp);

						// mResponseReceived.invoke(mex);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

				return answer;
			}
		};

		super.invokeProcess(mex, decorated);
	}

	@Override
	protected BpelRuntimeContextImpl createRuntimeContext(ProcessInstanceDAO dao, PROCESS template, MyRoleMessageExchangeImpl instantiatingMessageExchange) {
		return new BpelRuntimeContextImpl(this, dao, template, instantiatingMessageExchange) {
			@Override
			public void execute() {
				super.execute();
				if (ProcessState.isFinished(dao.getState()) && !alreadyFinished(dao.getInstanceId())) { //
					try {
						ExecutionEventsListener.processInstanceFinished(getConf(), dao);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

			@Override
			public void select(PickResponseChannel pickResponseChannel, Date timeout, boolean createInstance, Selector[] selectors) throws FaultException {
				try {
					super.select(pickResponseChannel, timeout, createInstance, selectors);
				} finally {

					Scheduler scheduler = ((ExecutionBpelEngine) getEngine()).getScheduler();

					scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
						public void afterCompletion(boolean success) {
							BindingServiceExternalService.getInvokeSync(dao.getInstanceId()).notifyReceiveReady();
						}

						public void beforeCompletion() {
						}
					});
				}
			}
		};
	}

	protected boolean terminateProcessInstance(Long instanceId) {
		try {

			ProcessInstanceDAO instance = getProcessDAO().getInstance(instanceId);
			if (instance == null)
				throw new ManagementException("InstanceNotFound:" + instanceId);

			ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
			evt.setOldState(instance.getState());
			instance.setState(ProcessState.STATE_TERMINATED);
			evt.setNewState(ProcessState.STATE_TERMINATED);
			evt.setProcessInstanceId(instanceId);
			ProcessDAO process = instance.getProcess();
			QName processName = process.getType();
			evt.setProcessName(processName);
			QName processId = process.getProcessId();
			evt.setProcessId(processId);
			saveEvent(evt, instance);

			return true;
		} catch (ManagementException me) {
			return false;
		}
	}

	public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instance, List<String> scopeNames) {

		super.saveEvent(event, instance, scopeNames);

		ExecutionBpelEngine engine = (ExecutionBpelEngine) getEngine();

		// Process Instantiated
		if (event instanceof NewProcessInstanceEvent) {
			try {
				ExecutionEventsListener.processInstanceCreated(engine.getExecutorService(), getConf(), instance);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		} else if (ProcessState.isFinished(instance.getState()) && !alreadyFinished(instance.getInstanceId())) { //
			try {
				ExecutionEventsListener.processInstanceFinished(getConf(), instance);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	private boolean alreadyFinished(Long processId) {

		Set<Long> instances = (Set<Long>) ThreadLocalHolder.getThreadLocalMap().get("FINISHED_PROCESS_INSTANCES");

		if (instances == null) {
			instances = new HashSet<Long>();
			ThreadLocalHolder.getThreadLocalMap().put("FINISHED_PROCESS_INSTANCES", instances);
		}

		if (!instances.contains(processId)) {
			instances.add(processId);
			return false;
		} else {
			return true;
		}
	}

	public boolean isHydrationLazy() {
		return true;
	}

	public boolean isHydrationLazySet() {
		return true;
	}

}
