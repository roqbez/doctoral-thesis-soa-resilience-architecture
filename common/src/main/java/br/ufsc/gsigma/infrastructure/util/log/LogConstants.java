package br.ufsc.gsigma.infrastructure.util.log;

public interface LogConstants {

	public static final String MESSAGE_ID_PROCESS_DEPLOYMENT = "PROCESS_DEPLOYMENT";

	public static final String MESSAGE_ID_PROCESS_CONVERT = "PROCESS_CONVERT";

	public static final String MESSAGE_ID_PROCESS_SERVICE_BINDING = "PROCESS_SERVICE_BINDING";

	public static final String MESSAGE_ID_PROCESS_INSTANCE = "PROCESS_INSTANCE";

	public static final String MESSAGE_ID_SERVICE_INVOKE = "SERVICE_INVOKE";

	public static final String MESSAGE_ID_SERVICE_MEDIATION = "SERVICE_MEDIATION";

	public static final String MESSAGE_ID_SERVICE_MEDIATION_REPLY = "SERVICE_MEDIATION_REPLY";

	public static final String MESSAGE_ID_SERVICE_CHECK = "SERVICE_CHECK";

	public static final String MESSAGE_ID_RESILIENCE_REACTION = "RESILIENCE_REACTION";

	public static final String MESSAGE_ID_RESILIENCE_ANALYSIS = "RESILIENCE_ANALYSIS";

	public static final String MESSAGE_ID_SERVICE_ACCESS_REQUEST = "SERVICE_ACCESS_REQUEST";

	////

	public static final String HOSTNAME = "hostname";

	public static final String EVENT_TIMESTAMP = "eventTimestamp";

	public static final String APPLICATION_ID = "applicationId";

	public static final String EXECUTION_ID = "executionId";

	public static final String PROCESS_NAME = "processName";

	public static final String PROCESS_INSTANCE_ID = "processInstanceId";

	public static final String PROCESS_CREATION_TIME = "processCreationTime";

	public static final String PROCESS_FINISH_TIME = "processFinishTime";

	public static final String PROCESS_STATE = "processState";

	public static final String PROCESS_STATE_CODE = "processStateCode";

	public static final String PROCESS_STARTED = "processStarted";

	public static final String PROCESS_FINISHED = "processFinished";

	public static final String PROCESS_DURATION = "processDuration";

	public static final String SERVICE_TARGET_URL = "serviceTargetUrl";

	public static final String SERVICE_CLASSIFICATION = "serviceClassification";

	public static final String SERVICE_PROTOCOL = "serviceProtocol";

	public static final String SERVICE_UDDI_SERVICE_KEY = "uddiServiceKey";

	public static final String SERVICE_UDDI_SERVICE_BINDING_KEY = "uddiBindingKey";

	public static final String SERVICE_INVOKED_SERVICES = "invokedServices";

	public static final String SERVICE_INVOKED_TASKS = "invokedTasks";

	public static final String SERVICE_INVOKED_SERVICE_PROVIDERS = "invokedServiceProviders";

	public static final String SERVICE_RESPONSE_CODE = "serviceResponseCode";

	public static final String SERVICE_RESPONSE_TEXT = "serviceResponseText";

	public static final String SERVICE_RESPONSE = "serviceResponse";

	public static final String NUMBER_OF_INVOKED_SERVICES = "numberOfInvokedServices";

	public static final String DISTINCT_NUMBER_OF_INVOKED_SERVICES = "distinctNumberOfInvokedServices";

	public static final String NUMBER_OF_INVOKED_SERVICE_PROVIDERS = "numberOfInvokedServiceProviders";

	public static final String PREFIX_INVOKED_SERVICE = "invokedService_";

	public static final String INVOKED_SERVICES_FORKS = "invokedServicesForks";

	public static final String NUMBER_OF_BINDING_RECONFIGURATIONS = "numberOfBindingReconfigurations";

	public static final String MAX_BINDING_CONFIGURATION_VERSION = "maxBindingConfigurationVersion";

	public static final String RESILIENCE_RECONFIGURATION_DONE = "reconfigurationDone";

	public static final String RESILIENCE_TEMPORARY_RECONFIGURATION = "temporaryReconfiguration";

	public static final String RESILIENCE_REACTION_MEAN_TIME = "resilienceReactionMeanTime";

	public static final String RESILIENCE_DISCOVERY_MEAN_TIME = "resilienceDiscoveryMeanTime";

	public static final String RESILIENCE_DEPLOYMENT_MEAN_TIME = "resilienceDeploymentMeanTime";

	public static final String RESILIENCE_SERVICES_CHECK_MEAN_TIME = "resilienceServicesCheckMeanTime";

	public static final String RESILIENCE_REACTION_DURATION = "resilienceReactionDuration";

	public static final String RESILIENCE_DISCOVERY_DURATION = "resilienceDiscoveryDuration";

	public static final String RESILIENCE_DEPLOYMENT_DURATION = "resilienceDeploymentDuration";

	public static final String RESILIENCE_SERVICES_CHECK_DURATION = "resilienceServicesCheckDuration";

	public static final String RESILIENCE_SERVICES_NUMBER_OF_UNAVAILABLE_SERVICES = "resilienceNumberOfUnavailableServices";

	public static final String RESILIENCE_MONITORING_EXTERNAL_EVENT = "resilienceMonitoringExternalEvent";

}
