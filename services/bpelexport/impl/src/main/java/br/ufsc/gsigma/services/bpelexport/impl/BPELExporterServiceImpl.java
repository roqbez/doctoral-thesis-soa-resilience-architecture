package br.ufsc.gsigma.services.bpelexport.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.catalog.services.model.Connection;
import br.ufsc.gsigma.catalog.services.model.Decision;
import br.ufsc.gsigma.catalog.services.model.EndEvent;
import br.ufsc.gsigma.catalog.services.model.FlowControlComponent;
import br.ufsc.gsigma.catalog.services.model.Fork;
import br.ufsc.gsigma.catalog.services.model.ITConfiguration;
import br.ufsc.gsigma.catalog.services.model.InputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Merge;
import br.ufsc.gsigma.catalog.services.model.OutputBranch;
import br.ufsc.gsigma.catalog.services.model.OutputContactPoint;
import br.ufsc.gsigma.catalog.services.model.ProcessStandard;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.catalog.services.model.ServiceAssociation;
import br.ufsc.gsigma.catalog.services.model.ServiceConfig;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.catalog.services.model.StartEvent;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.Util;
import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.services.bpelexport.interfaces.BPELExporterService;
import br.ufsc.gsigma.services.bpelexport.model.BPELAssign;
import br.ufsc.gsigma.services.bpelexport.model.BPELConnectableComponent;
import br.ufsc.gsigma.services.bpelexport.model.BPELCopy;
import br.ufsc.gsigma.services.bpelexport.model.BPELFlow;
import br.ufsc.gsigma.services.bpelexport.model.BPELInvoke;
import br.ufsc.gsigma.services.bpelexport.model.BPELInvoke.JoinCondition;
import br.ufsc.gsigma.services.bpelexport.model.BPELLink;
import br.ufsc.gsigma.services.bpelexport.model.BPELService;
import br.ufsc.gsigma.services.bpelexport.model.BPELWhile;
import br.ufsc.gsigma.services.bpelexport.output.BPELProcessBundle;
import br.ufsc.gsigma.services.bpelexport.support.GraphCycle;
import br.ufsc.gsigma.services.bpelexport.support.GraphNode;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class BPELExporterServiceImpl implements BPELExporterService {

	private static final ClassLoader classLoader = BPELExporterServiceImpl.class.getClassLoader();

	private static final Configuration freemarkerCfg = new Configuration();

	private static final DOMBuilder domBuilder = new DOMBuilder();

	private static final Logger logger = Logger.getLogger(BPELExporterServiceImpl.class);

	private CatalogService catalogService = null;

	private CatalogService getCatalogSpecificationService() {
		if (catalogService == null)
			catalogService = WebServiceLocator.locateService(WebServiceLocator.CATALOG_SERVICE_UDDI_SERVICE_KEY, CatalogService.class);

		return catalogService;
	}

	@Override
	public BPELProcessBundle convertToBPEL(br.ufsc.gsigma.catalog.services.model.Process process, ITConfiguration itConfiguration) throws Exception {

		File processDir = null;

		try {

			logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_CONVERT, "Converting process " + process.getName() + " to BPEL format"));

			ServicesInformation servicesInformation = itConfiguration.getServicesInformation();

			freemarkerCfg.setTemplateLoader(new ClassTemplateLoader(BPELExporterServiceImpl.class, "/br/ufsc/gsigma/services/bpelexport/templates"));

			CatalogService catalogService = getCatalogSpecificationService();

			Map<String, Object> root = new HashMap<String, Object>();

			String processName = process.getName().replaceAll(" ", "");

			// TODO: tirar..
			String processURL = "http://" + ServicesAddresses.EXECUTION_SERVICE_HOSTNAME + ":" + ServicesAddresses.EXECUTION_SERVICE_PORT + "/ode/processes/" + processName;

			File destDir = FileUtils.createTempDir();

			String basePath = destDir + "/" + processName;

			File baseDir = new File(basePath);
			baseDir.mkdirs();

			processDir = new File(basePath);
			processDir.mkdirs();

			root.put("processName", processName);

			root.put("processURL", processName);

			root.put("processInMemory", String.valueOf(itConfiguration != null ? !itConfiguration.isPersistentProcess() : false));

			String processNamespace = "http://my.ubl.oasis.services/" + process.getName().toLowerCase().trim().replaceAll(" ", "");

			root.put("processNamespace", processNamespace);

			Set<Map<String, String>> serviceTypes = new HashSet<Map<String, String>>();
			root.put("serviceTypes", serviceTypes);

			Map<String, Integer> serviceTypeIdMap = new HashMap<String, Integer>();

			List<BPELService> services = new ArrayList<BPELService>();
			root.put("services", services);

			Map<Task, BPELInvoke> taskInvokeMap = new HashMap<Task, BPELInvoke>();
			Map<Task, BPELService> taskServiceMap = new HashMap<Task, BPELService>();

			List<BPELFlow> flows = new ArrayList<BPELFlow>();
			root.put("flows", flows);
			BPELFlow mainFlow = new BPELFlow("main_flow");
			flows.add(mainFlow);

			Map<Integer, InputContactPoint> inputContactPointMap = new HashMap<Integer, InputContactPoint>();
			Map<Integer, OutputContactPoint> outputContactPointMap = new HashMap<Integer, OutputContactPoint>();

			Map<ConnectableComponent, List<Connection>> mapInputConnectionsWithDocuments = new HashMap<ConnectableComponent, List<Connection>>();
			Map<ConnectableComponent, List<Connection>> mapOutputConnectionsWithDocuments = new HashMap<ConnectableComponent, List<Connection>>();

			Map<ConnectableComponent, List<Connection>> mapInputConnections = new HashMap<ConnectableComponent, List<Connection>>();
			Map<ConnectableComponent, List<Connection>> mapOutputConnections = new HashMap<ConnectableComponent, List<Connection>>();

			Set<ProcessStandard> processesStandards = new HashSet<ProcessStandard>();

			// Fixing input and output contact points of the connectable
			// components
			List<ConnectableComponent> listConnectableComponent = new ArrayList<ConnectableComponent>();

			listConnectableComponent.addAll(process.getStartEvents());
			listConnectableComponent.addAll(process.getEndEvents());
			listConnectableComponent.addAll(process.getTasks());
			listConnectableComponent.addAll(process.getDecisions());
			listConnectableComponent.addAll(process.getJunctions());
			listConnectableComponent.addAll(process.getForks());
			listConnectableComponent.addAll(process.getMerges());

			for (ConnectableComponent c : listConnectableComponent) {
				for (InputContactPoint inputContactPoint : c.getInputContactPoints()) {
					inputContactPoint.setConnectableComponent(c);
					inputContactPointMap.put(inputContactPoint.getId(), inputContactPoint);

					// If the document is not null, add the Process Standard
					// which
					// it belongs to
					if (inputContactPoint.getAssociatedDocument() != null && !inputContactPoint.getAssociatedDocument().isPrimitive()) {
						processesStandards.add(inputContactPoint.getAssociatedDocument().getProcessStandard());
					}
				}

				for (OutputContactPoint outputContactPoint : c.getOutputContactPoints()) {
					outputContactPoint.setConnectableComponent(c);
					outputContactPointMap.put(outputContactPoint.getId(), outputContactPoint);

					// If the document is not null, add the Process Standard
					// which
					// it belongs to
					if (outputContactPoint.getAssociatedDocument() != null && !outputContactPoint.getAssociatedDocument().isPrimitive()) {
						processesStandards.add(outputContactPoint.getAssociatedDocument().getProcessStandard());
					}
				}
			}

			// Tasks
			for (Task task : process.getTasks()) {

				// Getting Service WSDL
				String classification = task.getTaxonomyClassification();

				String wsdlXML = catalogService.getServiceWSDLFromTaxonomyClassification(classification);

				ServiceWSDLInfo info = getServiceWSDLInfo(wsdlXML);

				Integer currentServiceId = (serviceTypeIdMap.get(info.serviceType) != null) ? serviceTypeIdMap.get(info.serviceType) : 1;

				BPELService bpelService = new BPELService(info.serviceType, info.namespace, classification, currentServiceId);
				services.add(bpelService);

				serviceTypeIdMap.put(info.serviceType, ++currentServiceId);

				// Candidate Services
				Set<String> candidateServices = new HashSet<String>();

				ServiceConfig servicesConfig = servicesInformation.getServiceConfig(classification);

				if (servicesConfig != null) {

					// Services Association
					if (servicesConfig.getServiceAssociations() != null) {
						for (ServiceAssociation serviceAssociation : servicesConfig.getServiceAssociations()) {
							String serviceEndpoint = serviceAssociation.getServiceEndpoint();
							candidateServices.add(serviceEndpoint);
						}
					}

					bpelService.getCandidateServices().addAll(candidateServices);

					// QoS Criterions
					if (servicesConfig.getQoSCriterions() != null) {
						for (QoSCriterion qoSCriterion : servicesConfig.getQoSCriterions())
							bpelService.getQoSConstraints().add(qoSCriterion);
					}
				}

				Map<String, String> serviceType = new HashMap<String, String>();
				serviceType.put("type", info.serviceType);
				serviceType.put("prefix", bpelService.getPrefix());
				serviceType.put("namespace", bpelService.getNamespace());
				serviceType.put("wsdlLocation", bpelService.getWsdlLocation());
				serviceType.put("partnerLinkName", bpelService.getPartnerLinkName());
				serviceType.put("classification", bpelService.getTaxonomyClassification());
				serviceTypes.add(serviceType);

				ServiceConfig serviceConfig = servicesInformation.getServiceConfig(bpelService.getTaxonomyClassification());

				if (serviceConfig != null) {
					serviceConfig.setNamespace(bpelService.getNamespace());
					serviceConfig.setClassification(bpelService.getTaxonomyClassification());
					serviceConfig.setPartnerLinkName(bpelService.getPartnerLinkName());
				}

				// Writing the WSDL to folder
				Util.writeStringToFile(processDir + "/" + bpelService.getWsdlLocation(), wsdlXML);

				String callbackSOAPAction = processNamespace + "/callbacks/" + bpelService.getName().toLowerCase().replaceAll(" ", "");

				bpelService.setCallbackSOAPAction(callbackSOAPAction);
				bpelService.setCallbackOperation(bpelService.getName().toLowerCase().replaceAll(" ", "") + "_callback");

				bpelService.getInputVariable().setMessagePayload(getServiceMessagePayload(wsdlXML, "input", processURL, callbackSOAPAction, bpelService.getInputVariable().getPayloadAttributes()));

				bpelService.getOutputVariable().setMessagePayload(getServiceMessagePayload(wsdlXML, "output", processURL, callbackSOAPAction, bpelService.getOutputVariable().getPayloadAttributes()));

				bpelService.getOutputCallbackVariable().setMessagePayload(getServiceMessagePayload(wsdlXML, "outputCallback", processURL, callbackSOAPAction, bpelService.getOutputCallbackVariable().getPayloadAttributes()));

				String invokeName = "invoke_service_" + bpelService.getName().toLowerCase().replaceAll(" ", "");
				String receiveName = "receive_service_" + bpelService.getName().toLowerCase().replaceAll(" ", "");
				String replyName = "reply_service_" + bpelService.getName().toLowerCase().replaceAll(" ", "");
				String callbackOperation = bpelService.getName().toLowerCase().replaceAll(" ", "") + "_callback";

				BPELInvoke invoke = new BPELInvoke(invokeName, receiveName, replyName, callbackOperation, bpelService);

				invoke.setEquivalentTask(task);

				taskInvokeMap.put(task, invoke);
				taskServiceMap.put(task, bpelService);

				mapOutputConnectionsWithDocuments.put(task, new ArrayList<Connection>());
				mapInputConnectionsWithDocuments.put(task, new ArrayList<Connection>());

				mainFlow.getInvokes().add(invoke);

			}

			// Initialize Assign
			BPELAssign initializeAssign = new BPELAssign("initialize_variables");
			mainFlow.setInitializeAssign(initializeAssign);

			for (BPELService bpelService : services) {

				// Input
				BPELCopy copy = new BPELCopy();
				copy.getFrom().setLiteral(bpelService.getInputVariable().getMessagePayload());
				copy.getTo().setVariable(bpelService.getInputVariable().getName());
				copy.getTo().setPart("payload");
				initializeAssign.getCopies().add(copy);

				// Callback input
				copy = new BPELCopy();
				copy.getFrom().setLiteral(bpelService.getOutputVariable().getMessagePayload());
				copy.getTo().setVariable(bpelService.getOutputVariable().getName());
				copy.getTo().setPart("payload");
				initializeAssign.getCopies().add(copy);

				// Callback output
				copy = new BPELCopy();
				copy.getFrom().setLiteral(bpelService.getOutputCallbackVariable().getMessagePayload());
				copy.getTo().setVariable(bpelService.getOutputCallbackVariable().getName());
				copy.getTo().setPart("payload");
				initializeAssign.getCopies().add(copy);

				// Set processId to input
				copy = new BPELCopy();
				copy.getFrom().setVariable("process_output");
				copy.getFrom().setPart("payload");
				copy.getFrom().setQueryLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
				copy.getFrom().setQuery("<![CDATA[/processId]]>");
				copy.getTo().setVariable(bpelService.getInputVariable().getName());
				copy.getTo().setPart("payload");
				copy.getTo().setQueryLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
				copy.getTo().setQuery("<![CDATA[/processContext/processId]]>");
				initializeAssign.getCopies().add(copy);

				// Set processId to input
				copy = new BPELCopy();
				copy.getFrom().setVariable("process_output");
				copy.getFrom().setPart("payload");
				copy.getFrom().setQueryLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
				copy.getFrom().setQuery("<![CDATA[/processId]]>");
				copy.getTo().setVariable(bpelService.getOutputCallbackVariable().getName());
				copy.getTo().setPart("payload");
				copy.getTo().setQueryLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
				copy.getTo().setQuery("<![CDATA[/processContext/processId]]>");
				initializeAssign.getCopies().add(copy);

			}

			// Set inputs and outputs for the connectables components
			for (Connection connection : process.getConnections()) {

				OutputContactPoint outputContactPoint = outputContactPointMap.get(connection.getOutput().getId());
				InputContactPoint inputContactPoint = inputContactPointMap.get(connection.getInput().getId());

				ConnectableComponent outputComponent = (outputContactPoint != null) ? outputContactPoint.getConnectableComponent() : null;
				ConnectableComponent inputComponent = (inputContactPoint != null) ? inputContactPoint.getConnectableComponent() : null;

				// All connections
				List<Connection> connections = mapOutputConnections.get(outputComponent);
				if (connections == null) {
					connections = new ArrayList<Connection>();
					mapOutputConnections.put(outputComponent, connections);
				}
				connections.add(connection);

				// Connections with documents
				if (outputComponent != null && connection.getOutput().getAssociatedDocument() != null && connection.getInput().getAssociatedDocument() != null && connection.getInput().getAssociatedDocument().equals(connection.getOutput().getAssociatedDocument())) {

					connection.getOutput().setConnectableComponent(outputComponent);

					List<Connection> connectionsWithDocuments = mapOutputConnectionsWithDocuments.get(outputComponent);
					if (connectionsWithDocuments == null) {
						connectionsWithDocuments = new ArrayList<Connection>();
						mapOutputConnectionsWithDocuments.put(inputComponent, connectionsWithDocuments);
					}
					connectionsWithDocuments.add(connection);

				}

				// All connections
				connections = mapInputConnections.get(inputComponent);
				if (connections == null) {
					connections = new ArrayList<Connection>();
					mapInputConnections.put(inputComponent, connections);
				}
				connections.add(connection);

				// Connections with documents
				if (inputComponent != null && connection.getInput().getAssociatedDocument() != null && connection.getOutput().getAssociatedDocument() != null && connection.getInput().getAssociatedDocument().equals(connection.getOutput().getAssociatedDocument())) {

					connection.getInput().setConnectableComponent(inputComponent);

					List<Connection> connectionsWithDocuments = mapInputConnectionsWithDocuments.get(inputComponent);
					if (connectionsWithDocuments == null) {
						connectionsWithDocuments = new ArrayList<Connection>();
						mapInputConnectionsWithDocuments.put(inputComponent, connectionsWithDocuments);
					}
					connectionsWithDocuments.add(connection);
				}
			}

			// Parsing decisions and forks connections
			Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskInputs = new HashMap<FlowControlComponent, Set<Task>>();
			Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskOutputs = new HashMap<FlowControlComponent, Set<Task>>();
			handleDecisionsAndForksConnections(inputContactPointMap, outputContactPointMap, mapFlowControlComponentToTaskInputs, mapFlowControlComponentToTaskOutputs, mapInputConnections, mapOutputConnections, process.getConnections());

			// Creating input assign to BPEL Invoke
			for (BPELInvoke invoke : mainFlow.getInvokes()) {

				List<Connection> inputConnections = mapInputConnectionsWithDocuments.get(invoke.getEquivalentTask());

				if (inputConnections.size() > 0) {

					BPELAssign assign = new BPELAssign("assign_input_" + invoke.getName());

					HashSet<Task> alreadyHandledTask = new HashSet<Task>();

					// Getting the source connection component
					for (Connection c : inputConnections) {

						// The connection starts from a task
						if (c.getOutput().getConnectableComponent() instanceof Task) {
							Task task = (Task) c.getOutput().getConnectableComponent();

							BPELService sourceService = taskServiceMap.get(task);

							assign.getCopies().add(new BPELCopy(sourceService.getOutputVariable().getName(), c.getOutput().getName(), invoke.getService().getInputVariable().getName(), c.getInput().getName()));

							// invoke.getInputAssigns().add(assign);

							mainFlow.getAssigns().add(assign);

							Set<BPELAssign> assigns = mainFlow.getMapInvokeToAssigns().get(invoke);
							if (assigns == null) {
								assigns = new LinkedHashSet<BPELAssign>();
								mainFlow.getMapInvokeToAssigns().put(invoke, assigns);
							}
							assigns.add(assign);

						}

						// The connection starts from a decision or from a fork
						// TODO: melhorar essa parte
						if (c.getOutput().getConnectableComponent() instanceof Decision || c.getOutput().getConnectableComponent() instanceof Fork) {

							FlowControlComponent flowControlComponent = (FlowControlComponent) c.getOutput().getConnectableComponent();

							OutputContactPoint sourceContactPoint = null;

							InputContactPoint targetInputContactPoint = c.getInput();

							// Try to match the flow component target component
							// input with the flow component input (by the
							// contact
							// point name and the associated document type)
							for (Connection flowControlConnection : mapInputConnectionsWithDocuments.get(flowControlComponent)) {
								if (flowControlConnection.getInput().getName().equals(targetInputContactPoint.getName()) && flowControlConnection.getInput().getAssociatedDocument().equals(targetInputContactPoint.getAssociatedDocument())) {
									sourceContactPoint = flowControlConnection.getOutput();
									break;
								}
							}
							// If the sourceContactPoint is still null, try to
							// match
							// only by the document type
							if (sourceContactPoint == null && flowControlComponent.getInputBranches().size() == 1) {
								for (Connection flowControlConnection : mapInputConnectionsWithDocuments.get(flowControlComponent)) {
									if (flowControlConnection.getInput().getAssociatedDocument().equals(targetInputContactPoint.getAssociatedDocument())) {
										sourceContactPoint = flowControlConnection.getOutput();
										break;
									}
								}
							}

							// TODO: alterado em 05/09/2018 (funcionar com Billing with Credit Note Process)
							if (sourceContactPoint.getConnectableComponent() instanceof Fork) {

								Fork fork = (Fork) sourceContactPoint.getConnectableComponent();

								Set<Task> forkTaskInput = mapFlowControlComponentToTaskInputs.get(fork);

								if (forkTaskInput != null && forkTaskInput.size() == 1) {

									Task sourceTask = forkTaskInput.iterator().next();

									for (OutputContactPoint out : sourceTask.getOutputContactPoints()) {

										if (ObjectUtils.equals(out.getAssociatedDocument(), sourceContactPoint.getAssociatedDocument())) {
											sourceContactPoint = out;
											break;
										}
									}
								}
							}

							if (sourceContactPoint.getConnectableComponent() instanceof Merge) {
								// //////////////////////
								Merge merge = (Merge) sourceContactPoint.getConnectableComponent();

								int k = 1;
								for (Connection c2 : mapInputConnectionsWithDocuments.get(merge)) {
									if (c2.getOutput().getName().equals(sourceContactPoint.getName()) && c2.getOutput().getAssociatedDocument().equals(sourceContactPoint.getAssociatedDocument()) && c2.getOutput().getConnectableComponent() instanceof Task) {

										BPELService sourceService = taskServiceMap.get(c2.getOutput().getConnectableComponent());

										String outputName = sourceContactPoint.getName();

										assign = new BPELAssign("assign_input_" + invoke.getName() + "_" + (k++));

										assign.getCopies().add(new BPELCopy(sourceService.getOutputVariable().getName(), outputName, invoke.getService().getInputVariable().getName(), c.getInput().getName()));

										// invoke.getInputAssigns().add(assign);

										mainFlow.getAssigns().add(assign);

										Set<BPELAssign> assigns = mainFlow.getMapInvokeToAssigns().get(invoke);
										if (assigns == null) {
											assigns = new HashSet<BPELAssign>();
											mainFlow.getMapInvokeToAssigns().put(invoke, assigns);
										}
										assigns.add(assign);

										invoke.getMapSourceServiceToAssign().put(sourceService, assign);

									}
								}

								// /////////////////////////////////////

							} else if (sourceContactPoint.getConnectableComponent() instanceof Decision) {

								// //////////////////////
								Decision decision = (Decision) sourceContactPoint.getConnectableComponent();

								int k = 1;
								for (Connection c2 : mapInputConnectionsWithDocuments.get(decision)) {

									if ((c2.getOutput().getName().equals("outputDocument")) && c2.getOutput().getAssociatedDocument().equals(sourceContactPoint.getAssociatedDocument()) && c2.getOutput().getConnectableComponent() instanceof Task) {

										logger.debug("****c2.getOutput().getName()=" + c2.getOutput().getName());

										BPELService sourceService = taskServiceMap.get(c2.getOutput().getConnectableComponent());

										String outputName = c2.getOutput().getName();

										assign = new BPELAssign("assign_input_" + invoke.getName() + "_" + (k++));

										assign.getCopies().add(new BPELCopy(sourceService.getOutputVariable().getName(), outputName, invoke.getService().getInputVariable().getName(), c.getInput().getName()));

										// invoke.getInputAssigns().add(assign);

										mainFlow.getAssigns().add(assign);

										Set<BPELAssign> assigns = mainFlow.getMapInvokeToAssigns().get(invoke);
										if (assigns == null) {
											assigns = new HashSet<BPELAssign>();
											mainFlow.getMapInvokeToAssigns().put(invoke, assigns);
										}
										assigns.add(assign);

										invoke.getMapSourceServiceToAssign().put(sourceService, assign);

									}
								}

								// /////////////////////////////////////

							} else if (sourceContactPoint.getConnectableComponent() instanceof Task) {

								BPELService sourceService = taskServiceMap.get(sourceContactPoint.getConnectableComponent());

								String outputName = sourceContactPoint.getName();

								assign.getCopies().add(new BPELCopy(sourceService.getOutputVariable().getName(), outputName, invoke.getService().getInputVariable().getName(), c.getInput().getName()));

								// invoke.getInputAssigns().add(assign);

								mainFlow.getAssigns().add(assign);

								Set<BPELAssign> assigns = mainFlow.getMapInvokeToAssigns().get(invoke);
								if (assigns == null) {
									assigns = new HashSet<BPELAssign>();
									mainFlow.getMapInvokeToAssigns().put(invoke, assigns);
								}
								assigns.add(assign);

								invoke.getMapSourceServiceToAssign().put(sourceService, assign);

							}
						}

						// The connection starts from a merge
						// TODO: melhorar essa parte
						if (c.getOutput().getConnectableComponent() instanceof Merge) {

							Task targetTask = (Task) c.getInput().getConnectableComponent();

							Merge merge = (Merge) c.getOutput().getConnectableComponent();

							int k = 1;

							for (Task task : mapFlowControlComponentToTaskInputs.get(merge)) {

								if (!alreadyHandledTask.contains(task)) {

									BPELService sourceService = taskServiceMap.get(task);

									assign = new BPELAssign("assign_input_" + invoke.getName() + "_" + k++);

									for (Connection targetConnection : mapInputConnectionsWithDocuments.get(targetTask)) {

										if (targetConnection.getOutput().getConnectableComponent().equals(merge)) {

											String fromQuery = targetConnection.getOutput().getName();

											String toQuery = targetConnection.getInput().getName();

											// TODO: alterado em 29/08/2018 (funcionar com Fulfilment with Receipt Advice)
											// toQuery = c.getInput().getName();

											if (fromQuery.startsWith("output") && fromQuery.length() > 6) {
												if (toQuery.startsWith("input") && toQuery.length() > 5) {
													if (!fromQuery.substring(6).equalsIgnoreCase(toQuery.substring(5))) {
														continue;
													}
												}
											}

											if (!sourceService.getOutputVariable().getPayloadAttributes().contains(fromQuery)) {
												continue;
											}

											BPELCopy bpelCopy = new BPELCopy(sourceService.getOutputVariable().getName(), fromQuery, invoke.getService().getInputVariable().getName(), toQuery);

											if (!assign.getCopies().contains(bpelCopy)) {
												assign.getCopies().add(bpelCopy);
											}
										}
									}
									// invoke.getInputAssigns().add(assign);

									mainFlow.getAssigns().add(assign);

									Set<BPELAssign> assigns = mainFlow.getMapInvokeToAssigns().get(invoke);
									if (assigns == null) {
										assigns = new HashSet<BPELAssign>();
										mainFlow.getMapInvokeToAssigns().put(invoke, assigns);
									}
									assigns.add(assign);

									invoke.getMapSourceServiceToAssign().put(sourceService, assign);

									alreadyHandledTask.add(task);

								}
							}
						}

					}
				}
			}

			// Creating Links

			HashSet<ComponentsConnectionEntry> alreadyConnected = new HashSet<ComponentsConnectionEntry>();

			int[] i = new int[] { 1 };

			for (Connection connection : process.getConnections()) {

				OutputContactPoint outputContactPoint = outputContactPointMap.get(connection.getOutput().getId());
				InputContactPoint inputContactPoint = inputContactPointMap.get(connection.getInput().getId());

				ConnectableComponent outputComponent = (outputContactPoint != null) ? outputContactPoint.getConnectableComponent() : null;
				ConnectableComponent inputComponent = (inputContactPoint != null) ? inputContactPoint.getConnectableComponent() : null;

				if (outputComponent instanceof Task && inputComponent instanceof EndEvent) {
					BPELInvoke invoke = taskInvokeMap.get(outputComponent);
					invoke.setEndEvent(true);
					continue;
				}

				BPELLink link = new BPELLink("link" + i[0]++);

				boolean canAddLink = false;

				boolean isStartLink = false;

				if (outputComponent instanceof StartEvent && inputComponent != null) {

					isStartLink = true;

					// TODO: alterado em 06/09/2018 (funcionar com Billing with Credit Note Process)
					if (inputComponent instanceof Merge) {
						Merge merge = (Merge) inputComponent;

						Set<Task> tasks = mapFlowControlComponentToTaskOutputs.get(merge);

						inputComponent = !CollectionUtils.isEmpty(tasks) ? tasks.iterator().next() : inputComponent;

					}

					initializeAssign.getSourceLinks().add(link);

					link.setOutputComponent(initializeAssign);
					canAddLink = true;
				}

				boolean inputContainsTask = false;

				if (mapInputConnections.get(outputComponent) != null) {
					for (Connection c : mapInputConnections.get(outputComponent)) {
						if (c.getOutput().getConnectableComponent() instanceof Task) {
							inputContainsTask = true;
							break;
						}
					}
				}

				// Flow Control Component
				// TODO: colocado || inputComponent instanceof Fork (04/10/2010)
				if (outputComponent instanceof FlowControlComponent && (inputComponent instanceof Task || inputComponent instanceof Fork)) {

					// If it is a fork and the input of it is a task
					if (outputComponent instanceof Fork && inputContainsTask) {
						BPELInvoke invokeSource = taskInvokeMap.get(mapFlowControlComponentToTaskInputs.get((Fork) outputComponent).iterator().next());
						invokeSource.getSourceLinks().add(link);
						link.setOutputComponent(invokeSource);
						canAddLink = true;
					}

					// If it is a decision with a task target, setup the
					// transition condition
					if (outputComponent instanceof Decision && inputComponent instanceof Task) {
						handleDecision(mapFlowControlComponentToTaskInputs, mapFlowControlComponentToTaskOutputs, alreadyConnected, taskInvokeMap, mainFlow.getLinks(), link, outputContactPoint, i, (Decision) outputComponent, (Task) inputComponent);
						canAddLink = false;
					}

					// If it is a decision with a fork target, setup the
					// transition condition
					if (outputComponent instanceof Decision && inputComponent instanceof Fork) {

						for (Task task : mapFlowControlComponentToTaskOutputs.get(inputComponent)) {
							handleDecision(mapFlowControlComponentToTaskInputs, mapFlowControlComponentToTaskOutputs, alreadyConnected, taskInvokeMap, mainFlow.getLinks(), link, outputContactPoint, i, (Decision) outputComponent, task);
						}

						canAddLink = false;
					}

					// If it is a merge, connect the merge input tasks to the
					// merge
					// output task
					if (outputComponent instanceof Merge) {
						handleMerge(mapFlowControlComponentToTaskInputs, mapFlowControlComponentToTaskOutputs, alreadyConnected, taskInvokeMap, mainFlow.getLinks(), i, (Merge) outputComponent, (Task) inputComponent);
						canAddLink = false;
					}

				}

				if (outputComponent instanceof Task && inputComponent instanceof Task) {
					BPELInvoke invokeSource = taskInvokeMap.get(outputComponent);
					invokeSource.getSourceLinks().add(link);
					link.setOutputComponent(invokeSource);
					canAddLink = true;
				}

				if (inputComponent instanceof Task && !(outputComponent instanceof Merge || outputComponent instanceof Decision || (outputComponent instanceof Fork && !inputContainsTask))) {
					BPELInvoke invokeTarget = taskInvokeMap.get(inputComponent);

					BPELAssign bpelAssign = null;

					// if (invokeTarget.getInputAssigns().size() > 0)
					// bpelAssign =
					// invokeTarget.getInputAssigns().iterator().next();

					// TODO: alterado em 29/08/2018 (funcionar com Fulfilment with Receipt Advice) (usando LinkedHashSet)
					// TODO: alterado em 06/09/2018 (!isStartLink ? ...) (funcionar com Billing with Credit Note)
					Set<BPELAssign> invokeAssigns = !isStartLink ? mainFlow.getMapInvokeToAssigns().get(invokeTarget) : null;

					if (invokeAssigns != null && invokeAssigns.size() > 0) {

						// if (invokeAssigns.size() > 1) {
						// } else {
						bpelAssign = invokeAssigns.iterator().next();
						// }
					}

					if (bpelAssign != null) {

						BPELLink linkAssignToTask = new BPELLink("link" + (i[0]++));

						bpelAssign.getTargetLinks().add(link);
						link.setInputComponent(bpelAssign);

						bpelAssign.getSourceLinks().add(linkAssignToTask);
						linkAssignToTask.setOutputComponent(bpelAssign);

						invokeTarget.getTargetLinks().add(linkAssignToTask);
						linkAssignToTask.setInputComponent(invokeTarget);

						mainFlow.getLinks().add(linkAssignToTask);

					} else {
						invokeTarget.getTargetLinks().add(link);
						link.setInputComponent(invokeTarget);
					}

					canAddLink = true;

				}

				// check if link is repeated

				for (BPELLink l : mainFlow.getLinks()) {

					boolean sameInput = ObjectUtils.equals(link.getInputComponent(), l.getInputComponent());
					boolean sameOutput = ObjectUtils.equals(link.getOutputComponent(), l.getOutputComponent());
					boolean sameTransitionCondition = ObjectUtils.equals(link.getTransitionCondition(), l.getTransitionCondition());

					if (sameInput && sameOutput && sameTransitionCondition) {
						canAddLink = false;
						break;
					}
				}

				if (canAddLink) {
					mainFlow.getLinks().add(link);
				} else {
					if (link.getOutputComponent() != null) {
						link.getOutputComponent().getSourceLinks().remove(link);
					}
					if (link.getInputComponent() != null) {
						link.getInputComponent().getTargetLinks().remove(link);
					}
				}

			}

			// Handling cycles, which are not allowed in BPEL
			GraphCycle graphCycle = findCyclesInProcess(process);

			if (graphCycle != null) {

				Object sourceObject = graphCycle.getSourceNode().getObject();
				Object targetObject = graphCycle.getTargetNode().getObject();

				// TODO: alterado em 07/09/2018 (funcionar com Billing with Credit Note Process)
				if ((sourceObject instanceof Task || sourceObject instanceof Decision) && (targetObject instanceof Merge || targetObject instanceof Task)) {

					BPELInvoke cycleStartInvoke = null;
					BPELInvoke cycleEndInvoke = null;

					Task cycleStartTask = null;
					Task cycleEndTask = null;

					// TODO: alterado 08/09/2018 (funcionar com Billing with Credit Note Process)
					if (targetObject instanceof FlowControlComponent) {
						cycleStartTask = mapFlowControlComponentToTaskOutputs.get(targetObject).iterator().next();
						cycleStartInvoke = taskInvokeMap.get(cycleStartTask);
					} else {
						cycleStartTask = (Task) targetObject;
						cycleStartInvoke = taskInvokeMap.get(targetObject);
					}

					if (sourceObject instanceof FlowControlComponent) {
						cycleEndTask = mapFlowControlComponentToTaskInputs.get(sourceObject).iterator().next();
						cycleEndInvoke = taskInvokeMap.get(cycleEndTask);
					} else {
						cycleEndTask = (Task) sourceObject;
						cycleEndInvoke = taskInvokeMap.get(sourceObject);
					}

					// Removing the Cycle Link
					BPELLink cycleLink = null;
					for (BPELLink l : cycleEndInvoke.getSourceLinks()) {

						for (BPELLink l2 : l.getInputComponent().getSourceLinks()) {
							if (cycleStartInvoke.getTargetLinks().contains(l2)) {
								cycleLink = l2;
								cycleStartInvoke.getTargetLinks().remove(cycleLink);
								l.getInputComponent().getSourceLinks().remove(cycleLink);
								mainFlow.getLinks().remove(cycleLink);
								break;
							}
						}

						if (cycleStartInvoke.getTargetLinks().contains(l)) {
							cycleLink = l;
							cycleStartInvoke.getTargetLinks().remove(cycleLink);
							cycleEndInvoke.getSourceLinks().remove(cycleLink);
							mainFlow.getLinks().remove(cycleLink);
							break;
						}
					}

					BPELWhile bpelWhile = new BPELWhile("loop_while");
					mainFlow.getWhiles().add(bpelWhile);
					BPELFlow newFlow = new BPELFlow("loop_while_flow");

					bpelWhile.getFlows().add(newFlow);

					// Cycle start invoke link
					for (BPELLink l : cycleStartInvoke.getTargetLinks()) {
						bpelWhile.getTargetLinks().add(l);
						l.setInputComponent(bpelWhile);
					}
					cycleStartInvoke.getTargetLinks().clear();

					// While condition

					String transitionCondition = null;

					// TODO: alterado 08/09/2018 (funcionar com Billing with Credit Note Process)
					if (sourceObject instanceof Decision) {

						Decision decision = (Decision) sourceObject;

						List<Connection> connections = mapOutputConnections.get(decision);

						branches: for (OutputBranch ob : decision.getOutputBranches()) {
							for (OutputContactPoint oc : ob.getOutputContactPoints()) {
								for (Connection conn : connections) {
									if (conn.getOutput().equals(oc) && conn.getInput().getConnectableComponent().equals(cycleStartTask)) {
										transitionCondition = "$" + cycleEndInvoke.getService().getOutputVariable().getName() + ".payload/" + "outputDecision = '" + ob.getCondition() + "'";
										break branches;
									}
								}
							}
						}

					} else {
						transitionCondition = searchParentTransitionCondition(cycleEndInvoke);
					}

					if (transitionCondition != null) {
						String outputVariableName = transitionCondition.substring(0, transitionCondition.indexOf("="));
						bpelWhile.setCondition("$abort_process = false() and (string-length(" + outputVariableName.trim() + ") = 0 or " + transitionCondition + ")");
					}

					// /////////////////////////////////////////////////////////

					Set<BPELLink> links = new HashSet<BPELLink>();
					Set<BPELConnectableComponent> connectableComponents = new HashSet<BPELConnectableComponent>();

					searchBPELComponents(cycleStartInvoke, links, connectableComponents);

					for (BPELLink link : links) {
						mainFlow.getLinks().remove(link);
						newFlow.getLinks().add(link);

					}

					for (BPELConnectableComponent c : connectableComponents) {
						mainFlow.getInvokes().remove(c);
						mainFlow.getAssigns().remove(c);
						mainFlow.getWhiles().remove(c);

						if (c instanceof BPELInvoke)
							newFlow.getInvokes().add((BPELInvoke) c);

						if (c instanceof BPELAssign)
							newFlow.getAssigns().add((BPELAssign) c);

						if (c instanceof BPELWhile)
							newFlow.getWhiles().add((BPELWhile) c);

					}

				}

			}

			// Writing Service Discovery WSDL
			// Util.writeStringToFile(processDir + "/DiscoveryService.wsdl", UddiLocator.getServiceWSDL(WebServiceLocator.DISCOVERY_SERVICE_UDDI_SERVICE_KEY));

			// Writing XSD Documents
			new File(processDir + "/xsd").mkdirs();

			// Copying XSD support files
			IOUtils.copy(classLoader.getResourceAsStream("br/ufsc/gsigma/services/bpelexport/xsd/processContext.xsd"), new FileOutputStream(basePath + "/xsd/processContext.xsd"));

			for (ProcessStandard p : processesStandards) {
				byte[] xsdZip = catalogService.getDocumentsXSDZipFromProcessStandardSpecification(p.getId());
				ZipUtils.unzip(xsdZip, new File(processDir + "/xsd/" + p.getId().toLowerCase()));
			}

			// Generating BPEL Code
			freemarkerCfg.getTemplate("bpelProcess.ftl", "UTF-8").process(root, new FileWriter(processDir + "/" + root.get("processName") + ".bpel"));
			freemarkerCfg.getTemplate("bpelProcessArtifacts.ftl", "UTF-8").process(root, new FileWriter(processDir + "/" + root.get("processName") + "Artifacts.wsdl"));
			freemarkerCfg.getTemplate("bpelOdeDeploy.ftl", "UTF-8").process(root, new FileWriter(processDir + "/deploy.xml"));

			try (FileOutputStream out = new FileOutputStream(new File(processDir, "businessProcess.xml"))) {
				JAXBSerializerUtil.write(process, out);
			}

			try (FileOutputStream out = new FileOutputStream(new File(processDir, "servicesInformation.xml"))) {
				JAXBSerializerUtil.write(servicesInformation, out);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				ZipUtils.zip(baseDir, baseDir, zos);
			}

			byte[] data = baos.toByteArray();

			File saveDir = new File("bpel_processes", processName);
			org.apache.commons.io.FileUtils.deleteDirectory(saveDir);
			org.apache.commons.io.FileUtils.copyDirectory(processDir, saveDir);

			return new BPELProcessBundle(processName, data);

		} catch (

		Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
		}

	}

	private String searchParentTransitionCondition(BPELConnectableComponent component) {
		for (BPELLink link : component.getTargetLinks()) {
			if (link.getTransitionCondition() != null)
				return link.getTransitionCondition();
			else
				return searchParentTransitionCondition(link.getOutputComponent());
		}
		return null;
	}

	private void searchBPELComponents(BPELConnectableComponent component, Set<BPELLink> linksFound, Set<BPELConnectableComponent> connectableComponentsFound) {

		connectableComponentsFound.add(component);

		linksFound.addAll(component.getTargetLinks());
		linksFound.addAll(component.getSourceLinks());

		for (BPELLink l : component.getSourceLinks())
			searchBPELComponents(l.getInputComponent(), linksFound, connectableComponentsFound);

	}

	private ServiceWSDLInfo getServiceWSDLInfo(String wsdlXML) throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc = db.parse(new ByteArrayInputStream(wsdlXML.getBytes()));

		Document jdomDocument = domBuilder.build(doc);

		return new ServiceWSDLInfo(jdomDocument.getRootElement().getAttributeValue("name"), jdomDocument.getRootElement().getAttributeValue("targetNamespace"));

	}

	private void handleDecisionsAndForksConnections(Map<Integer, InputContactPoint> inputContactPointMap, Map<Integer, OutputContactPoint> outputContactPointMap, Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskInputs,
			Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskOutputs, Map<ConnectableComponent, List<Connection>> mapInputConnections, Map<ConnectableComponent, List<Connection>> mapOutputConnections, List<Connection> connections) {

		for (Connection connection : connections) {

			OutputContactPoint outputContactPoint = outputContactPointMap.get(connection.getOutput().getId());
			InputContactPoint inputContactPoint = inputContactPointMap.get(connection.getInput().getId());

			ConnectableComponent outputComponent = (outputContactPoint != null) ? outputContactPoint.getConnectableComponent() : null;
			ConnectableComponent inputComponent = (inputContactPoint != null) ? inputContactPoint.getConnectableComponent() : null;

			if (inputComponent instanceof FlowControlComponent) {

				Set<Task> tasks = mapFlowControlComponentToTaskInputs.get((FlowControlComponent) inputComponent);

				if (tasks == null) {
					tasks = new HashSet<Task>();
					mapFlowControlComponentToTaskInputs.put((FlowControlComponent) inputComponent, tasks);
				}

				if (outputComponent instanceof Task) {

					tasks.add((Task) outputComponent);

					// TODO: alterado (|| outputComponent instanceof Fork) em 05/09/2018 (funcionar com Billing with Credit Note Process)
				} else if (outputComponent instanceof Merge || outputComponent instanceof Decision || outputComponent instanceof Fork) {

					for (Connection c2 : mapInputConnections.get(outputComponent)) {
						if (c2.getOutput().getConnectableComponent() instanceof Task)
							tasks.add((Task) c2.getOutput().getConnectableComponent());
					}

				}

			} else if (outputComponent instanceof FlowControlComponent) {

				Set<Task> tasks = mapFlowControlComponentToTaskOutputs.get((FlowControlComponent) outputComponent);
				if (tasks == null) {
					tasks = new HashSet<Task>();
					mapFlowControlComponentToTaskOutputs.put((FlowControlComponent) outputComponent, tasks);
				}

				if (inputComponent instanceof Task) {
					tasks.add((Task) inputComponent);
				}
			}
		}
	}

	private boolean handleDecision(Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskInputs, Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskOutputs, HashSet<ComponentsConnectionEntry> alreadyConnected, Map<Task, BPELInvoke> taskInvokeMap, Set<BPELLink> links,
			BPELLink link, OutputContactPoint outputContactPoint, int[] i, Decision decision, Task targetTask) {

		boolean result = false;

		BPELInvoke invokeTarget = taskInvokeMap.get(targetTask);
		invokeTarget.setJoinCondition(JoinCondition.OR);

		// TODO: continuar

		for (Task task : mapFlowControlComponentToTaskInputs.get(decision)) {

			ComponentsConnectionEntry connectionEntry = new ComponentsConnectionEntry(task, targetTask);

			if (!alreadyConnected.contains(connectionEntry)) {

				BPELInvoke invokeSource = taskInvokeMap.get(task);

				// Discover the output branch assoaciated with the output
				// contact point
				OutputBranch outputBranch = null;
				for (OutputBranch ob : ((Decision) decision).getOutputBranches())
					for (OutputContactPoint ocp : ob.getOutputContactPoints())
						if (ocp.getName().equals(outputContactPoint.getName())) {
							outputBranch = ob;
							break;
						}

				link = new BPELLink("link" + (i[0]++));

				String transitionCondition = "$" + invokeSource.getService().getOutputVariable().getName() + ".payload/" + "outputDecision = '" + outputBranch.getCondition() + "'";
				link.setTransitionCondition(transitionCondition);

				logger.debug("transitionCondition=" + transitionCondition);

				links.add(link);

				invokeSource.getSourceLinks().add(link);
				link.setOutputComponent(invokeSource);

				BPELAssign bpelAssign = invokeTarget.getMapSourceServiceToAssign().get(invokeSource.getService());

				bpelAssign.getTargetLinks().add(link);
				link.setInputComponent(bpelAssign);

				BPELLink linkAssignToTask = new BPELLink("link" + (i[0]++));

				bpelAssign.getSourceLinks().add(linkAssignToTask);
				linkAssignToTask.setOutputComponent(bpelAssign);

				invokeTarget.getTargetLinks().add(linkAssignToTask);
				linkAssignToTask.setInputComponent(invokeTarget);

				links.add(linkAssignToTask);

				alreadyConnected.add(connectionEntry);

				result = result || true;

			}

		}

		return result;

	}

	private boolean handleMerge(Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskInputs, Map<FlowControlComponent, Set<Task>> mapFlowControlComponentToTaskOutputs, HashSet<ComponentsConnectionEntry> alreadyConnected, Map<Task, BPELInvoke> taskInvokeMap, Set<BPELLink> links, int[] i,
			Merge outputComponent, Task inputComponent) {

		boolean result = false;

		Task targetTask = inputComponent;

		for (Task task : mapFlowControlComponentToTaskInputs.get(outputComponent)) {

			ComponentsConnectionEntry connectionEntry = new ComponentsConnectionEntry(task, targetTask);

			if (!alreadyConnected.contains(connectionEntry)) {

				BPELLink linkMerge = new BPELLink("link" + (i[0]++));

				BPELInvoke invokeSource = taskInvokeMap.get(task);
				BPELInvoke invokeTarget = taskInvokeMap.get(targetTask);

				invokeSource.getSourceLinks().add(linkMerge);
				linkMerge.setOutputComponent(invokeSource);

				links.add(linkMerge);

				BPELAssign bpelAssign = invokeTarget.getMapSourceServiceToAssign().get(invokeSource.getService());

				bpelAssign.getTargetLinks().add(linkMerge);
				linkMerge.setInputComponent(bpelAssign);

				BPELLink linkAssignToTask = new BPELLink("link" + (i[0]++));
				bpelAssign.getSourceLinks().add(linkAssignToTask);
				linkAssignToTask.setOutputComponent(bpelAssign);
				invokeTarget.getTargetLinks().add(linkAssignToTask);
				linkAssignToTask.setInputComponent(invokeTarget);

				links.add(linkAssignToTask);

				alreadyConnected.add(connectionEntry);

				result = result || true;

			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private String getServiceMessagePayload(String wsdlXML, String type, String callbackEndpoint, String callbackSOAPAction, List<String> collectedOutputParams) throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc = db.parse(new ByteArrayInputStream(wsdlXML.getBytes()));

		Document jdomDocument = domBuilder.build(doc);

		String namespace = jdomDocument.getRootElement().getAttributeValue("targetNamespace");

		String serviceName = jdomDocument.getRootElement().getAttributeValue("name");

		String servicePortType = serviceName;

		String[] split = servicePortType.split("_");
		String serviceOperation = split[split.length - 1].substring(0, 1).toLowerCase() + split[split.length - 1].substring(1);

		// XPath xPath = XPath//
		// .newInstance("//wsdl:definitions/wsdl:portType[@name='" +
		// servicePortType + "']/wsdl:operation[@name='" + serviceOperation
		// + "']/wsdl:" + type + "/@message");
		// xPath.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");

		// String messageName = ((Attribute)
		// xPath.selectSingleNode(jdomDocument)).getValue().split(":")[1];

		String messageName = "";

		if (type.equals("input"))
			messageName = serviceOperation + "AsyncRequestMsg";
		else if (type.equals("output"))
			messageName = serviceOperation + "AsyncCallbackRequestMsg";
		else if (type.equals("outputCallback"))
			messageName = serviceOperation + "AsyncCallbackResponseMsg";

		XPath xPath = XPath//
				.newInstance("//wsdl:definitions/wsdl:message[@name='" + messageName + "']/wsdl:part[@name='payload']/@element");
		xPath.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");

		String typeName = ((Attribute) xPath.selectSingleNode(jdomDocument)).getValue().split(":")[1];

		xPath = XPath//
				.newInstance("//wsdl:definitions/wsdl:types/xsd:schema[@targetNamespace='" + namespace + "']/xsd:element[@name='" + typeName + "']/xsd:complexType/xsd:sequence/xsd:element");
		xPath.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		xPath.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

		List<Element> params = (List<Element>) xPath.selectNodes(jdomDocument);

		Element rootElement = new Element(typeName);
		rootElement.setNamespace(Namespace.getNamespace("tns", namespace));

		for (Element e : params) {

			if (e.getAttributeValue("name").equals("processContext")) {
				Element el = new Element(e.getAttributeValue("name"));

				el.getChildren().add(new Element("processId"));

				Element callbackEndpointEl = new Element("callbackEndpoint");
				callbackEndpointEl.setText(callbackEndpoint);
				el.getChildren().add(callbackEndpointEl);

				Element callbackSOAPActionEl = new Element("callbackSOAPAction");
				callbackSOAPActionEl.setText(callbackSOAPAction);
				el.getChildren().add(callbackSOAPActionEl);

				rootElement.addContent(el);

			} else {

				String attName = e.getAttributeValue("name");

				rootElement.addContent(new Element(attName));

				if (collectedOutputParams != null) {
					collectedOutputParams.add(attName);
				}
			}

			// <processId></processId>
			// <callbackEndpoint>http://localhost:8080/ode/processes/PaymentProcess</callbackEndpoint>
			// <callbackSOAPAction>http://my.ubl.oasis.services/paymentprocess/callbacks/ubl_paymentprocess_accountingcustomer_authorizepayment1</callbackSOAPAction>

		}

		StringWriter writer = new StringWriter();
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.output(rootElement, writer);

		return writer.toString();
	}

	private class ServiceWSDLInfo {

		String serviceType;
		String namespace;

		public ServiceWSDLInfo(String serviceType, String namespace) {
			this.serviceType = serviceType;
			this.namespace = namespace;
		}

	}

	class ComponentsConnectionEntry {
		protected ConnectableComponent source;
		protected ConnectableComponent target;

		public ComponentsConnectionEntry(ConnectableComponent source, ConnectableComponent target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ComponentsConnectionEntry other = (ComponentsConnectionEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}

		private BPELExporterServiceImpl getOuterType() {
			return BPELExporterServiceImpl.this;
		}

	}

	private static GraphCycle findCyclesInProcess(br.ufsc.gsigma.catalog.services.model.Process process) {

		Map<Integer, InputContactPoint> inputContactPointMap = new HashMap<Integer, InputContactPoint>();
		Map<Integer, OutputContactPoint> outputContactPointMap = new HashMap<Integer, OutputContactPoint>();

		Map<ConnectableComponent, GraphNode> mapConnectableComponentToGraphNode = new HashMap<ConnectableComponent, GraphNode>();

		List<ConnectableComponent> listConnectableComponent = new ArrayList<ConnectableComponent>();

		listConnectableComponent.addAll(process.getStartEvents());
		listConnectableComponent.addAll(process.getEndEvents());
		listConnectableComponent.addAll(process.getTasks());
		listConnectableComponent.addAll(process.getDecisions());
		listConnectableComponent.addAll(process.getJunctions());
		listConnectableComponent.addAll(process.getForks());
		listConnectableComponent.addAll(process.getMerges());

		for (ConnectableComponent c : listConnectableComponent) {
			for (InputContactPoint inputContactPoint : c.getInputContactPoints()) {
				inputContactPoint.setConnectableComponent(c);
				inputContactPointMap.put(inputContactPoint.getId(), inputContactPoint);
			}

			for (OutputContactPoint outputContactPoint : c.getOutputContactPoints()) {
				outputContactPoint.setConnectableComponent(c);
				outputContactPointMap.put(outputContactPoint.getId(), outputContactPoint);
			}

			mapConnectableComponentToGraphNode.put(c, new GraphNode(c));
		}

		for (Connection c : process.getConnections()) {

			GraphNode sourceGraphNode = mapConnectableComponentToGraphNode.get(outputContactPointMap.get(c.getOutput().getId()).getConnectableComponent());
			GraphNode targetGraphNode = mapConnectableComponentToGraphNode.get(inputContactPointMap.get(c.getInput().getId()).getConnectableComponent());

			sourceGraphNode.getOutputs().add(targetGraphNode);
			targetGraphNode.getInputs().add(sourceGraphNode);

		}

		GraphNode startNode = null;

		for (GraphNode n : mapConnectableComponentToGraphNode.values()) {
			if (n.getInputs().size() == 0) {
				startNode = n;
				break;
			}
		}

		if (startNode != null) {

			Set<GraphNode> nodesToVisit = new LinkedHashSet<GraphNode>(startNode.getOutputs());
			Set<GraphNode> visitedNodes = new LinkedHashSet<GraphNode>();

			while (!nodesToVisit.isEmpty()) {
				for (GraphNode n : new ArrayList<GraphNode>(nodesToVisit)) {

					if (!visitedNodes.contains(n)) {
						nodesToVisit.addAll(n.getOutputs());
						visitedNodes.add(n);
					}

					nodesToVisit.remove(n);
				}
			}

			for (GraphNode n : visitedNodes) {
				GraphCycle result = visitNode(n);
				if (result != null) {
					return result;
				}
			}
		}
		return null;

	}

	// TODO: alterado em 29/08/2018 (funcionar com Fulfilment with Receipt Advice)
	private static GraphCycle visitNode(GraphNode n) {

		Set<GraphNode> visitedNodes = new HashSet<GraphNode>();

		visitedNodes.add(n);

		LinkedHashSet<GraphNode> toVisit = new LinkedHashSet<GraphNode>(n.getOutputs());

		while (!toVisit.isEmpty()) {

			for (GraphNode node : new LinkedList<GraphNode>(toVisit)) {

				for (GraphNode c : node.getOutputs()) {

					if (c == n) {
						return new GraphCycle(node, n);
					} else if (!visitedNodes.contains(c)) {
						toVisit.add(c);
						visitedNodes.add(c);
					}
				}

				toVisit.remove(node);
			}
		}

		return null;
	}

	// public static void main(String[] args) throws Exception {
	// CatalogService service = WebServiceLocator.locateService(WebServiceLocator.CATALOG_SERVICE_UDDI_SERVICE_KEY, CatalogService.class);
	//
	// br.ufsc.gsigma.catalog.services.model.Process p = service
	// .getProcessFromProcessStandardSpecification("ubl/sourcing/catalogueprovision/createcatalogueprocess");
	//
	// // br.ufsc.gsigma.catalog.services.model.Process p =
	// // service.getProcessById(177);
	//
	// QoSCriterion crit = new QoSCriterion(1, "xx", "yy", "second");
	// crit.setQoSValue(10);
	//
	// p.getTasks().get(0).getQoSCriterions().add(crit);
	//
	// BPELProcessBundle bundle = new BPELExporterAsyncServiceImpl().convertToBPEL(p);
	//
	// ZipUtils.unzip(bundle.getZipContent(), new File("process_tmp/" + bundle.getProcessName()));
	//
	// }
}
