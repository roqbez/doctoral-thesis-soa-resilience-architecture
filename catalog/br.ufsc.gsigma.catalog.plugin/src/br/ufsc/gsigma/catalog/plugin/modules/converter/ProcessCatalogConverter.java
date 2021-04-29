package br.ufsc.gsigma.catalog.plugin.modules.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.ProjectDefinition;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.data.BMBusinessItem;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.data.BMDataCatalog;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMAbstractComponent;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMConnection;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMDecision;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMDeploymentServer;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMEnd;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMFork;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMInfrastructureProvider;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMInputBranch;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMInputContactPoint;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMMerge;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMOutputBranch;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMOutputContactPoint;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcess;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessCatalog;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMResilienceConfiguration;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMRoleResourceRequisite;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMStart;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTask;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskResilienceConfiguration;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMResourceCatalog;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMRole;
import br.ufsc.gsigma.catalog.plugin.util.ModelExtensionUtils;
import br.ufsc.gsigma.catalog.plugin.util.ServiceAssociationUtil;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.locator.CatalogServiceLocator;
import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.catalog.services.model.Connection;
import br.ufsc.gsigma.catalog.services.model.Decision;
import br.ufsc.gsigma.catalog.services.model.DeploymentServer;
import br.ufsc.gsigma.catalog.services.model.Document;
import br.ufsc.gsigma.catalog.services.model.EndEvent;
import br.ufsc.gsigma.catalog.services.model.FlowControlComponent;
import br.ufsc.gsigma.catalog.services.model.Fork;
import br.ufsc.gsigma.catalog.services.model.ITConfiguration;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider;
import br.ufsc.gsigma.catalog.services.model.InfrastructureProvider.InfrastructureServerType;
import br.ufsc.gsigma.catalog.services.model.InputBranch;
import br.ufsc.gsigma.catalog.services.model.InputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Merge;
import br.ufsc.gsigma.catalog.services.model.OutputBranch;
import br.ufsc.gsigma.catalog.services.model.OutputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Participant;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ResilienceConfiguration;
import br.ufsc.gsigma.catalog.services.model.ServiceConfig;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.catalog.services.model.StartEvent;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.catalog.services.model.TaskParticipant;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ProcessCatalogConverter {

	private final static Configuration freemarkerCfg = new Configuration();

	public File outputEditorXML(ProjectDefinition projectDefinition, String output) {

		File xmlFile = null;

		try {
			xmlFile = new File(output);
			if (!(xmlFile.exists())) {
				xmlFile.getParentFile().mkdirs();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		OutputStreamWriter out = null;

		try {
			out = new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8");
			parseProjectDefinition(projectDefinition, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}

		return xmlFile;
	}

	private void parseProjectDefinition(ProjectDefinition projectDefinition, Writer writer) throws TemplateException, IOException, URISyntaxException {

		freemarkerCfg.setTemplateLoader(new ClassTemplateLoader(ProcessCatalogConverter.class, "/br/ufsc/gsigma/catalog/plugin/modules/converter/templates"));

		Template t = freemarkerCfg.getTemplate("wsbm.ftl", "UTF-8");

		Map<String, Object> root = new HashMap<String, Object>();

		root.put("resourceModel", projectDefinition.getResourceModel());
		root.put("processModel", projectDefinition.getProcessModel());
		root.put("dataModel", projectDefinition.getDataModel());

		t.process(root, writer);

	}

	private void buildInputAndOutputContactPoints(Map<InputContactPoint, BMInputContactPoint> inputContactPointEquivalence, Map<OutputContactPoint, BMOutputContactPoint> outputContactPointEquivalence, BMDataCatalog dataCatalog, Map<Document, BMBusinessItem> businessItemEquivalence,
			List<InputContactPoint> inputContactPoints, List<OutputContactPoint> outputContactPoints, BMAbstractComponent component) {
		component.clearContactPoints();

		for (InputContactPoint inputContactPoint : inputContactPoints) {
			BMInputContactPoint bmInputContactPoint = component.addInputContactPoint(inputContactPoint.getName());

			inputContactPointEquivalence.put(inputContactPoint, bmInputContactPoint);

			// If the contact point has a document assignement
			if (inputContactPoint.getAssociatedDocument() != null) {
				if (!inputContactPoint.getAssociatedDocument().isPrimitive()) {
					BMBusinessItem bmBusinessItem = businessItemEquivalence.get(inputContactPoint.getAssociatedDocument());
					if (bmBusinessItem == null) {
						bmBusinessItem = dataCatalog.addBusinessItem(inputContactPoint.getAssociatedDocument().getName());
						businessItemEquivalence.put(inputContactPoint.getAssociatedDocument(), bmBusinessItem);
					}
					bmInputContactPoint.setAssociatedData(bmBusinessItem);
				} else {
					bmInputContactPoint.setAssociatedData(new BMBusinessItem(inputContactPoint.getAssociatedDocument().getName()));
				}
			}
		}

		for (OutputContactPoint outputContactPoint : outputContactPoints) {
			BMOutputContactPoint bmOutputContactPoint = component.addOutputContactPoint(outputContactPoint.getName());

			outputContactPointEquivalence.put(outputContactPoint, bmOutputContactPoint);

			// If the contact point has a document assignement
			if (outputContactPoint.getAssociatedDocument() != null) {
				if (!outputContactPoint.getAssociatedDocument().isPrimitive()) {
					BMBusinessItem bmBusinessItem = businessItemEquivalence.get(outputContactPoint.getAssociatedDocument());
					if (bmBusinessItem == null) {
						bmBusinessItem = dataCatalog.addBusinessItem(outputContactPoint.getAssociatedDocument().getName());
						businessItemEquivalence.put(outputContactPoint.getAssociatedDocument(), bmBusinessItem);
					}
					bmOutputContactPoint.setAssociatedData(bmBusinessItem);
				} else {
					bmOutputContactPoint.setAssociatedData(new BMBusinessItem(outputContactPoint.getAssociatedDocument().getName()));
				}
			}
		}
	}

	public ProjectDefinition convertToEditorFormat(Process p) {
		try {

			if (p != null) {

				Map<InputContactPoint, BMInputContactPoint> inputContactPointEquivalence = new HashMap<InputContactPoint, BMInputContactPoint>();
				Map<OutputContactPoint, BMOutputContactPoint> outputContactPointEquivalence = new HashMap<OutputContactPoint, BMOutputContactPoint>();

				Map<Participant, BMRole> roleEquivalence = new HashMap<Participant, BMRole>();

				Map<Document, BMBusinessItem> businessItemEquivalence = new HashMap<Document, BMBusinessItem>();

				ServicesInformation servicesInformation = p.getItConfiguration() != null ? p.getItConfiguration().getServicesInformation() : null;

				ProjectDefinition projectDefinition = new ProjectDefinition();

				BMResourceCatalog resourceCatalog = projectDefinition.getResourceModel().getResourceCatalogs().get(0);
				BMProcessCatalog processCatalog = projectDefinition.getProcessModel().getProcessCatalogs().get(0);
				BMDataCatalog dataCatalog = projectDefinition.getDataModel().getDataCatalogs().get(0);

				// //Creating the Process
				BMProcess bmProcess = processCatalog.addProcess(p.getName());

				BMProcessInformationExtension processExtension = new BMProcessInformationExtension();

				if (servicesInformation != null) {

					if (servicesInformation.getGlobalQoSDelta() != null)
						processExtension.setGlobalQoSDelta(servicesInformation.getGlobalQoSDelta());

					if (servicesInformation.getQoSWeights() != null)
						processExtension.setQoSWeights(servicesInformation.getQoSWeights());

					if (servicesInformation.getQoSCriterions() != null)
						processExtension.setQoSCriterions(servicesInformation.getQoSCriterions());

					bmProcess.setProcessInformationExtension(processExtension);
				}

				// Creating Start and End Events
				for (StartEvent startEvent : p.getStartEvents()) {
					BMStart bmStart = new BMStart(startEvent.getName());
					// Creating contact points
					buildInputAndOutputContactPoints(inputContactPointEquivalence, outputContactPointEquivalence, dataCatalog, businessItemEquivalence, startEvent.getInputContactPoints(), startEvent.getOutputContactPoints(), bmStart);
					bmProcess.getStartNodes().add(bmStart);
				}
				for (EndEvent endEvent : p.getEndEvents()) {
					BMEnd bmEnd = new BMEnd(endEvent.getName());
					// Creating contact points
					buildInputAndOutputContactPoints(inputContactPointEquivalence, outputContactPointEquivalence, dataCatalog, businessItemEquivalence, endEvent.getInputContactPoints(), endEvent.getOutputContactPoints(), bmEnd);
					bmProcess.getEndNodes().add(bmEnd);
				}

				// Creating the Tasks
				for (Task t : p.getTasks()) {
					BMTask bmTask = new BMTask(t.getName());

					// Creating contact points
					buildInputAndOutputContactPoints(inputContactPointEquivalence, outputContactPointEquivalence, dataCatalog, businessItemEquivalence, t.getInputContactPoints(), t.getOutputContactPoints(), bmTask);

					// Resources
					for (TaskParticipant tp : t.getParticipants()) {

						BMRole bmRole = roleEquivalence.get(tp.getParticipant());
						if (bmRole == null) {
							bmRole = resourceCatalog.addRole(tp.getParticipant().getName());
							roleEquivalence.put(tp.getParticipant(), bmRole);
						}

						BMRoleResourceRequisite bmRoleResourceRequisite = new BMRoleResourceRequisite(tp.getName(), bmRole);
						bmTask.getResourceRequisites().add(bmRoleResourceRequisite);
					}

					// Extension
					if (servicesInformation != null) {
						ServiceConfig cfg = servicesInformation.getServiceConfig(t.getTaxonomyClassification());
						if (cfg != null) {
							BMTaskInformationExtension extension = new BMTaskInformationExtension();
							extension.setTaxonomyClassification(t.getTaxonomyClassification());
							extension.setQoSCriterions(cfg.getQoSCriterions());
							extension.setManagedQoSCriterions(cfg.getManagedQoSCriterions());
							extension.setServiceAssociations(ServiceAssociationUtil.getBMServiceAssociations(cfg.getServiceAssociations()));
							bmTask.setTaskInformationExtension(extension);
						}
					}
					bmProcess.getTasks().add(bmTask);
				}

				// Creating Decisions
				for (Decision d : p.getDecisions()) {
					BMDecision bmDecision = new BMDecision(d.getName());

					// Creating contact points
					buildInputAndOutputContactPoints(inputContactPointEquivalence, outputContactPointEquivalence, dataCatalog, businessItemEquivalence, d.getInputContactPoints(), d.getOutputContactPoints(), bmDecision);

					// Creating Branches
					for (InputBranch ib : d.getInputBranches()) {
						BMInputBranch bmInputBranch = new BMInputBranch(ib.getName());
						for (InputContactPoint icp : ib.getInputContactPoints())
							bmInputBranch.getInputContactPoints().add(inputContactPointEquivalence.get(icp));
						bmDecision.getInputBranches().add(bmInputBranch);
					}
					for (OutputBranch ob : d.getOutputBranches()) {
						BMOutputBranch bmOutputBranch = new BMOutputBranch(ob.getName());
						bmOutputBranch.setCondition(ob.getCondition());
						bmOutputBranch.setProbabilityPercentage(ob.getProbabilityPercentage());
						for (OutputContactPoint ocp : ob.getOutputContactPoints())
							bmOutputBranch.getOutputContactPoints().add(outputContactPointEquivalence.get(ocp));
						bmDecision.getOutputBranches().add(bmOutputBranch);
					}
					bmProcess.getDecisions().add(bmDecision);
				}

				// Creating Forks
				for (Fork f : p.getForks()) {
					BMFork bmFork = new BMFork(f.getName());

					// Creating contact points
					buildInputAndOutputContactPoints(inputContactPointEquivalence, outputContactPointEquivalence, dataCatalog, businessItemEquivalence, f.getInputContactPoints(), f.getOutputContactPoints(), bmFork);

					// Creating Branches
					for (InputBranch ib : f.getInputBranches()) {
						BMInputBranch bmInputBranch = new BMInputBranch(ib.getName());
						for (InputContactPoint icp : ib.getInputContactPoints())
							bmInputBranch.getInputContactPoints().add(inputContactPointEquivalence.get(icp));
						bmFork.getInputBranches().add(bmInputBranch);
					}
					for (OutputBranch ob : f.getOutputBranches()) {
						BMOutputBranch bmOutputBranch = new BMOutputBranch(ob.getName());
						bmOutputBranch.setCondition(ob.getCondition());
						bmOutputBranch.setProbabilityPercentage(ob.getProbabilityPercentage());
						for (OutputContactPoint ocp : ob.getOutputContactPoints())
							bmOutputBranch.getOutputContactPoints().add(outputContactPointEquivalence.get(ocp));
						bmFork.getOutputBranches().add(bmOutputBranch);
					}
					bmProcess.getForks().add(bmFork);
				}

				// Creating Merges
				for (Merge m : p.getMerges()) {
					BMMerge bmerge = new BMMerge(m.getName());

					// Creating contact points
					buildInputAndOutputContactPoints(inputContactPointEquivalence, outputContactPointEquivalence, dataCatalog, businessItemEquivalence, m.getInputContactPoints(), m.getOutputContactPoints(), bmerge);

					// Creating Branches
					for (InputBranch ib : m.getInputBranches()) {
						BMInputBranch bmInputBranch = new BMInputBranch(ib.getName());
						for (InputContactPoint icp : ib.getInputContactPoints())
							bmInputBranch.getInputContactPoints().add(inputContactPointEquivalence.get(icp));
						bmerge.getInputBranches().add(bmInputBranch);
					}
					for (OutputBranch ob : m.getOutputBranches()) {
						BMOutputBranch bmOutputBranch = new BMOutputBranch(ob.getName());
						bmOutputBranch.setCondition(ob.getCondition());
						bmOutputBranch.setProbabilityPercentage(ob.getProbabilityPercentage());
						for (OutputContactPoint ocp : ob.getOutputContactPoints())
							bmOutputBranch.getOutputContactPoints().add(outputContactPointEquivalence.get(ocp));
						bmerge.getOutputBranches().add(bmOutputBranch);
					}
					bmProcess.getMerges().add(bmerge);
				}

				// Creating Connections
				for (Connection c : p.getConnections())
					bmProcess.getConnections().add(new BMConnection(outputContactPointEquivalence.get(c.getOutput()), inputContactPointEquivalence.get(c.getInput())));

				return projectDefinition;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public br.ufsc.gsigma.catalog.services.model.Process convertToCatalogFormat(CatalogService catalogService, File xmlFile, boolean assignId, boolean processDataModel, ExecutionContext executionContext) throws ParserConfigurationException, SAXException, IOException {

		int[] globalId = new int[] { 0 };

		List<Node> componentsNodeBuffer = new ArrayList<Node>();
		List<Node> connectionsNodeBuffer = new ArrayList<Node>();
		Map<String, Participant> mapParticipantEquivalence = new HashMap<String, Participant>();
		Map<String, Document> mapDocumentEquivalence = new HashMap<String, Document>();
		Map<String, Process> mapProcessEquivalence = new HashMap<String, Process>();

		Map<String, ConnectableComponent> mapConnectableComponentEquivalence = new HashMap<String, ConnectableComponent>();

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = docBuilder.parse(xmlFile);

		doc.getDocumentElement().normalize();

		// Resource Model
		Node nodeResourceModel = doc.getElementsByTagName("wbim:resourceModel").item(0);
		if (nodeResourceModel != null) {
			for (int i = 0; i < nodeResourceModel.getChildNodes().getLength(); i++) {

				// Roles
				if (nodeResourceModel.getChildNodes().item(i).getNodeName().equals("wbim:roles")) {

					NodeList nodeListRoles = nodeResourceModel.getChildNodes().item(i).getChildNodes();

					for (int k = 0; k < nodeListRoles.getLength(); k++) {
						if (nodeListRoles.item(k).getNodeName().equals("wbim:role")) {
							String roleName = nodeListRoles.item(k).getAttributes().getNamedItem("name").getNodeValue();
							mapParticipantEquivalence.put(roleName, new Participant(normalizeName(roleName)));
						}
					}
				}
			}
		}

		// Data Model
		if (processDataModel) {
			Node nodeDataModel = doc.getElementsByTagName("wbim:dataModel").item(0);
			if (nodeDataModel != null) {
				for (int i = 0; i < nodeDataModel.getChildNodes().getLength(); i++) {

					// BusinessItems
					if (nodeDataModel.getChildNodes().item(i).getNodeName().equals("wbim:businessItems")) {

						NodeList nodeListRoles = nodeDataModel.getChildNodes().item(i).getChildNodes();

						for (int k = 0; k < nodeListRoles.getLength(); k++) {
							if (nodeListRoles.item(k).getNodeName().equals("wbim:businessItem")) {
								String businessItemName = nodeListRoles.item(k).getAttributes().getNamedItem("name").getNodeValue();
								Document document = catalogService.getCatalogDocumentById(new Document(normalizeName(businessItemName)).getId());
								mapDocumentEquivalence.put(businessItemName, document);
							}
						}
					}
				}
			}
		}

		// Process Model
		Node nodeProcessModel = doc.getElementsByTagName("wbim:processModel").item(0);
		if (nodeProcessModel != null) {
			for (int i = 0; i < nodeProcessModel.getChildNodes().getLength(); i++) {

				// Process
				if (nodeProcessModel.getChildNodes().item(i).getNodeName().equals("wbim:processes")) {

					NodeList nodeListProcess = nodeProcessModel.getChildNodes().item(i).getChildNodes();

					for (int k = 0; k < nodeListProcess.getLength(); k++) {
						if (nodeListProcess.item(k).getNodeName().equals("wbim:process")) {
							Node processNode = nodeListProcess.item(k);
							String processName = processNode.getAttributes().getNamedItem("name").getNodeValue();

							br.ufsc.gsigma.catalog.services.model.Process process = new br.ufsc.gsigma.catalog.services.model.Process(normalizeName(processName));

							ServicesInformation servicesInformation = new ServicesInformation();

							ITConfiguration itConfiguration = new ITConfiguration(servicesInformation);

							process.setItConfiguration(itConfiguration);

							mapProcessEquivalence.put(processName, process);

							// Flow Content
							NodeList processChildNodes = processNode.getChildNodes();

							for (int w = 0; w < processChildNodes.getLength(); w++) {

								if (processChildNodes.item(w).getNodeName().equals("wbim:description")) {

									BMProcessInformationExtension bmProcessInformationExtension = ModelExtensionUtils.getProcessInformationExtensionFromXML(processChildNodes.item(w).getTextContent());

									if (bmProcessInformationExtension != null) {
										servicesInformation.setQoSCriterions(bmProcessInformationExtension.getQoSCriterions());
										servicesInformation.setQoSWeights(bmProcessInformationExtension.getQoSWeights());
										servicesInformation.setGlobalQoSDelta(bmProcessInformationExtension.getGlobalQoSDelta());

										BMInfrastructureProvider bmInfrastructureProvider = bmProcessInformationExtension.getInfrastructureProvider();

										if (bmInfrastructureProvider != null) {

											InfrastructureProvider infrastructureProvider = new InfrastructureProvider();
											infrastructureProvider.setId(bmInfrastructureProvider.getId());
											infrastructureProvider.setName(bmInfrastructureProvider.getName());
											infrastructureProvider.setServerType(InfrastructureServerType.valueOf(bmInfrastructureProvider.getServerType()));

											if (!CollectionUtils.isEmpty(bmInfrastructureProvider.getDeploymentServers())) {
												for (BMDeploymentServer d : bmInfrastructureProvider.getDeploymentServers()) {
													infrastructureProvider.getDeploymentServers().add(new DeploymentServer(d.getName(), d.getAddress()));
												}
											}

											if (!CollectionUtils.isEmpty(bmInfrastructureProvider.getOrchestratorServers())) {
												for (BMDeploymentServer d : bmInfrastructureProvider.getOrchestratorServers()) {
													infrastructureProvider.getOrchestratorServers().add(new DeploymentServer(d.getName(), d.getAddress()));
												}
											}

											itConfiguration.setInfrastructureProvider(infrastructureProvider);
										}

										BMResilienceConfiguration bmResilienceConfiguration = bmProcessInformationExtension.getResilienceConfiguration();

										if (bmResilienceConfiguration != null) {
											ResilienceConfiguration resilienceConfiguration = new ResilienceConfiguration();

											resilienceConfiguration.setBindingServiceReplicas(bmResilienceConfiguration.getBindingServiceReplicas());
											resilienceConfiguration.setExecutionServiceReplicas(bmResilienceConfiguration.getExecutionServiceReplicas());
											resilienceConfiguration.setResilienceServiceReplicas(bmResilienceConfiguration.getResilienceServiceReplicas());

											resilienceConfiguration.getParams().putAll(bmResilienceConfiguration.getParams());
											resilienceConfiguration.getFlags().putAll(bmResilienceConfiguration.getFlags());

											if (executionContext != null) {

												for (Entry<String, String> e : bmResilienceConfiguration.getParams().entrySet()) {
													executionContext.addAttribute(e.getKey(), e.getValue());
												}

												executionContext.getFlags().putAll(bmResilienceConfiguration.getFlags());
											}

											itConfiguration.setResilienceConfiguration(resilienceConfiguration);
										}
									}

								} else if (processChildNodes.item(w).getNodeName().equals("wbim:flowContent")) {
									Node flowContentNode = processChildNodes.item(w);

									NodeList nodesInsideFlowContent = flowContentNode.getChildNodes();

									for (int z = 0; z < nodesInsideFlowContent.getLength(); z++) {

										// Tasks, StartNode, EndNode,
										if (nodesInsideFlowContent.item(z).getNodeName().equals("wbim:task")//
												|| nodesInsideFlowContent.item(z).getNodeName().equals("wbim:startNode")//
												|| nodesInsideFlowContent.item(z).getNodeName().equals("wbim:endNode") || nodesInsideFlowContent.item(z).getNodeName().equals("wbim:decision") || nodesInsideFlowContent.item(z).getNodeName().equals("wbim:fork")
												|| nodesInsideFlowContent.item(z).getNodeName().equals("wbim:merge"))//
											componentsNodeBuffer.add(nodesInsideFlowContent.item(z));

										if (nodesInsideFlowContent.item(z).getNodeName().equals("wbim:connection"))
											connectionsNodeBuffer.add(nodesInsideFlowContent.item(z));

									}
								}
							}

							for (Node node : componentsNodeBuffer) {
								parseStartNode(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, node, assignId, globalId);
								parseEndNode(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, node, assignId, globalId);
								parseTask(mapParticipantEquivalence, mapConnectableComponentEquivalence, mapDocumentEquivalence, process, node, assignId, globalId);
								parseDecision(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, node, assignId, globalId);
								parseFork(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, node, assignId, globalId);
								parseMerge(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, node, assignId, globalId);
							}

							for (Node node : connectionsNodeBuffer) {
								parseConnectionNode(mapConnectableComponentEquivalence, process, node, assignId, globalId);
							}

							return process;

						}
					}

				}
			}
		}
		return null;
	}

	private static void parseConnectionNode(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {
		if (node.getNodeName().equals("wbim:connection")) {
			// String connectionNodeName =
			// node.getAttributes().getNamedItem("name").getNodeValue();

			String sourceComponent = null;
			String sourceContactPoint = null;

			String targetComponent = null;
			String targetContactPoint = null;

			NodeList nodesInsideConnection = node.getChildNodes();

			for (int i = 0; i < nodesInsideConnection.getLength(); i++) {

				// Source
				if (nodesInsideConnection.item(i).getNodeName().equals("wbim:source")) {

					sourceComponent = nodesInsideConnection.item(i).getAttributes().getNamedItem("node").getNodeValue();
					sourceContactPoint = (nodesInsideConnection.item(i).getAttributes().getNamedItem("contactPoint") != null) ? nodesInsideConnection.item(i).getAttributes().getNamedItem("contactPoint").getNodeValue() : null;
				}
				// Target
				if (nodesInsideConnection.item(i).getNodeName().equals("wbim:target")) {

					targetComponent = nodesInsideConnection.item(i).getAttributes().getNamedItem("node").getNodeValue();
					targetContactPoint = (nodesInsideConnection.item(i).getAttributes().getNamedItem("contactPoint") != null) ? nodesInsideConnection.item(i).getAttributes().getNamedItem("contactPoint").getNodeValue() : null;
				}

			}

			OutputContactPoint outputContactPoint = (sourceContactPoint != null) ? mapConnectableComponentEquivalence.get(sourceComponent).getOutputContactPointByName(sourceContactPoint) : mapConnectableComponentEquivalence.get(sourceComponent).getOutputContactPoints().get(0);

			InputContactPoint inputContactPoint = (targetContactPoint != null) ? mapConnectableComponentEquivalence.get(targetComponent).getInputContactPointByName(targetContactPoint) : mapConnectableComponentEquivalence.get(targetComponent).getInputContactPoints().get(0);

			// If one contact point has a associated document, and the other
			// doesn't have
			if (outputContactPoint.getAssociatedDocument() != null && inputContactPoint.getAssociatedDocument() == null)
				inputContactPoint.setAssociatedDocument(outputContactPoint.getAssociatedDocument());

			if (inputContactPoint.getAssociatedDocument() != null && outputContactPoint.getAssociatedDocument() == null)
				outputContactPoint.setAssociatedDocument(inputContactPoint.getAssociatedDocument());

			Connection connection = new Connection(process, outputContactPoint, inputContactPoint);
			process.getConnections().add(connection);

			if (assignId)
				connection.setId(globalId[0]++);

		}
	}

	private static void parseStartNode(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {
		if (node.getNodeName().equals("wbim:startNode")) {
			String startNodeName = node.getAttributes().getNamedItem("name").getNodeValue();

			StartEvent startEvent = new StartEvent(process, startNodeName);

			if (assignId)
				startEvent.setId(globalId[0]++);

			process.getStartEvents().add(startEvent);

			mapConnectableComponentEquivalence.put(startNodeName, startEvent);

			NodeList nodesInsideStartNode = node.getChildNodes();

			for (int i = 0; i < nodesInsideStartNode.getLength(); i++) {

				// Inputs
				if (nodesInsideStartNode.item(i).getNodeName().equals("wbim:inputs"))
					parseInputContactPoints(startEvent, mapDocumentEquivalence, nodesInsideStartNode.item(i).getChildNodes(), assignId, globalId);

				// Outputs
				if (nodesInsideStartNode.item(i).getNodeName().equals("wbim:outputs"))
					parseOutputContactPoints(startEvent, mapDocumentEquivalence, nodesInsideStartNode.item(i).getChildNodes(), assignId, globalId);

			}

			if (startEvent.getInputContactPoints().size() == 0 && startEvent.getOutputContactPoints().size() == 0) {
				startEvent.buildDefaultContactPoints();

				if (assignId) {

					for (OutputContactPoint outputContactPoint : startEvent.getOutputContactPoints())
						outputContactPoint.setId(globalId[0]++);

					for (InputContactPoint inputContactPoint : startEvent.getInputContactPoints())
						inputContactPoint.setId(globalId[0]++);

				}
			}

		}
	}

	private static void parseEndNode(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {
		if (node.getNodeName().equals("wbim:endNode")) {
			String endNodeName = node.getAttributes().getNamedItem("name").getNodeValue();

			EndEvent endEvent = new EndEvent(process, endNodeName);

			if (assignId)
				endEvent.setId(globalId[0]++);

			process.getEndEvents().add(endEvent);

			mapConnectableComponentEquivalence.put(endNodeName, endEvent);

			NodeList nodesInsideEndNode = node.getChildNodes();

			for (int i = 0; i < nodesInsideEndNode.getLength(); i++) {

				// Inputs
				if (nodesInsideEndNode.item(i).getNodeName().equals("wbim:inputs"))
					parseInputContactPoints(endEvent, mapDocumentEquivalence, nodesInsideEndNode.item(i).getChildNodes(), assignId, globalId);

				// Outputs
				if (nodesInsideEndNode.item(i).getNodeName().equals("wbim:outputs"))
					parseOutputContactPoints(endEvent, mapDocumentEquivalence, nodesInsideEndNode.item(i).getChildNodes(), assignId, globalId);

			}

			if (endEvent.getInputContactPoints().size() == 0 && endEvent.getOutputContactPoints().size() == 0) {
				endEvent.buildDefaultContactPoints();

				if (assignId) {

					for (OutputContactPoint outputContactPoint : endEvent.getOutputContactPoints())
						outputContactPoint.setId(globalId[0]++);

					for (InputContactPoint inputContactPoint : endEvent.getInputContactPoints())
						inputContactPoint.setId(globalId[0]++);

				}

			}

		}
	}

	private static void parseTask(Map<String, Participant> mapParticipantEquivalence, Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {

		ServicesInformation servicesInformation = process.getItConfiguration().getServicesInformation();

		if (node.getNodeName().equals("wbim:task")) {
			String taskName = node.getAttributes().getNamedItem("name").getNodeValue();

			Task task = new Task(process, taskName);

			if (assignId)
				task.setId(globalId[0]++);

			process.getTasks().add(task);
			mapConnectableComponentEquivalence.put(taskName, task);

			NodeList nodesInsideTask = node.getChildNodes();

			for (int i = 0; i < nodesInsideTask.getLength(); i++) {

				// Description (vai ser usado para QoS e UBL)
				if (nodesInsideTask.item(i).getNodeName().equals("wbim:description")) {

					BMTaskInformationExtension bmTaskInformationExtension = ModelExtensionUtils.getTaskInformationExtensionFromXML(nodesInsideTask.item(i).getTextContent());

					String classification = bmTaskInformationExtension.getTaxonomyClassification();

					ServiceConfig serviceConfig = servicesInformation.createServiceConfig(classification);

					task.setTaxonomyClassification(classification);
					serviceConfig.setQoSCriterions(bmTaskInformationExtension.getQoSCriterions());
					serviceConfig.setManagedQoSCriterions(bmTaskInformationExtension.getManagedQoSCriterions());
					serviceConfig.setServiceAssociations(ServiceAssociationUtil.getServiceAssociations(bmTaskInformationExtension.getServiceAssociations()));
					serviceConfig.setMaxNumberOfServices(bmTaskInformationExtension.getMaxNumberOfServicesForDiscovery());

					BMTaskResilienceConfiguration resilienceConfiguration = bmTaskInformationExtension.getResilienceConfiguration();

					if (resilienceConfiguration != null) {

						ExecutionContext executionContext = null;

						if (!resilienceConfiguration.isEnableResilience()) {
							executionContext = executionContext == null ? new ExecutionContext() : executionContext;
							executionContext.getFlags().put(ExecutionFlags.FLAG_DISABLE_SERVICE_MONITORING, true);
						}

						if (!resilienceConfiguration.isEnableAdhocServicesReplication()) {
							executionContext = executionContext == null ? new ExecutionContext() : executionContext;
							executionContext.getFlags().put(ExecutionFlags.FLAG_DISABLE_ADHOC_SERVICE_DEPLOYMENT, true);
						} else {
							serviceConfig.setNumberOfReplicas(resilienceConfiguration.getServiceReplicas());
						}

						serviceConfig.setExecutionContext(executionContext);
					}

				}

				// Inputs
				if (nodesInsideTask.item(i).getNodeName().equals("wbim:inputs"))
					parseInputContactPoints(task, mapDocumentEquivalence, nodesInsideTask.item(i).getChildNodes(), assignId, globalId);

				// Outputs
				if (nodesInsideTask.item(i).getNodeName().equals("wbim:outputs"))
					parseOutputContactPoints(task, mapDocumentEquivalence, nodesInsideTask.item(i).getChildNodes(), assignId, globalId);

				// Resources
				if (nodesInsideTask.item(i).getNodeName().equals("wbim:resources")) {
					NodeList nodesInsideResources = nodesInsideTask.item(i).getChildNodes();
					for (int w = 0; w < nodesInsideResources.getLength(); w++) {
						if (nodesInsideResources.item(w).getNodeName().equals("wbim:roleRequirement")) {
							String roleRequimentName = nodesInsideResources.item(w).getAttributes().getNamedItem("name").getNodeValue();
							String roleName = nodesInsideResources.item(w).getAttributes().getNamedItem("role").getNodeValue();
							task.addParticipant(roleRequimentName, mapParticipantEquivalence.get(roleName));
						}
					}
				}

			}

			if (task.getInputContactPoints().size() == 0 && task.getOutputContactPoints().size() == 0) {
				task.buildDefaultContactPoints();

				if (assignId) {

					for (OutputContactPoint outputContactPoint : task.getOutputContactPoints())
						outputContactPoint.setId(globalId[0]++);

					for (InputContactPoint inputContactPoint : task.getInputContactPoints())
						inputContactPoint.setId(globalId[0]++);

				}

			}

		}
	}

	private static void parseDecision(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {

		if (node.getNodeName().equals("wbim:decision")) {
			String decisionName = node.getAttributes().getNamedItem("name").getNodeValue();

			Decision decision = new Decision(process, decisionName);

			if (assignId)
				decision.setId(globalId[0]++);

			process.getDecisions().add(decision);
			mapConnectableComponentEquivalence.put(decisionName, decision);

			parseFlowControlComponent(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, decision, node, assignId, globalId);
		}

	}

	private static void parseFork(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {

		if (node.getNodeName().equals("wbim:fork")) {
			String forkName = node.getAttributes().getNamedItem("name").getNodeValue();

			Fork fork = new Fork(process, forkName);

			if (assignId)
				fork.setId(globalId[0]++);

			process.getForks().add(fork);
			mapConnectableComponentEquivalence.put(forkName, fork);

			parseFlowControlComponent(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, fork, node, assignId, globalId);
		}
	}

	private static void parseMerge(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, Node node, boolean assignId, int[] globalId) {

		if (node.getNodeName().equals("wbim:merge")) {
			String mergeName = node.getAttributes().getNamedItem("name").getNodeValue();

			Merge merge = new Merge(process, mergeName);

			if (assignId)
				merge.setId(globalId[0]++);

			process.getMerges().add(merge);
			mapConnectableComponentEquivalence.put(mergeName, merge);

			parseFlowControlComponent(mapConnectableComponentEquivalence, mapDocumentEquivalence, process, merge, node, assignId, globalId);

		}
	}

	// TODO: colocar junction

	private static void parseFlowControlComponent(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Map<String, Document> mapDocumentEquivalence, Process process, FlowControlComponent flowControlComponent, Node node, boolean assignId, int[] globalId) {

		NodeList nodesInsideDecision = node.getChildNodes();

		for (int i = 0; i < nodesInsideDecision.getLength(); i++) {

			// Inputs
			if (nodesInsideDecision.item(i).getNodeName().equals("wbim:inputBranch")) {

				InputBranch inputBranch = new InputBranch(flowControlComponent, nodesInsideDecision.item(i).getAttributes().getNamedItem("name").getNodeValue());

				for (int k = 0; k < nodesInsideDecision.item(i).getChildNodes().getLength(); k++) {
					if (nodesInsideDecision.item(i).getChildNodes().item(k).getNodeName().equals("wbim:input")) {

						InputContactPoint inputContactPoint = generateInputContactPointFromNode(flowControlComponent, mapDocumentEquivalence, nodesInsideDecision.item(i).getChildNodes().item(k), assignId, globalId);

						inputBranch.getInputContactPoints().add(inputContactPoint);
						flowControlComponent.getInputContactPoints().add(inputContactPoint);
					}
				}

				flowControlComponent.getInputBranches().add(inputBranch);

			}

			// Outputs
			if (nodesInsideDecision.item(i).getNodeName().equals("wbim:outputBranch")) {

				OutputBranch outputBranch = new OutputBranch(flowControlComponent, nodesInsideDecision.item(i).getAttributes().getNamedItem("name").getNodeValue());

				String conditionName = null;
				Float probability = null;

				for (int k = 0; k < nodesInsideDecision.item(i).getChildNodes().getLength(); k++) {

					if (nodesInsideDecision.item(i).getChildNodes().item(k).getNodeName().equals("wbim:output")) {

						OutputContactPoint outputContactPoint = generateOutputContactPointFromNode(flowControlComponent, mapDocumentEquivalence, nodesInsideDecision.item(i).getChildNodes().item(k), assignId, globalId);

						flowControlComponent.getOutputContactPoints().add(outputContactPoint);
						outputBranch.getOutputContactPoints().add(outputContactPoint);

					}
					if (nodesInsideDecision.item(i).getChildNodes().item(k).getNodeName().equals("wbim:condition"))
						conditionName = nodesInsideDecision.item(i).getChildNodes().item(k).getAttributes().getNamedItem("name").getNodeValue();

					if (nodesInsideDecision.item(i).getChildNodes().item(k).getNodeName().equals("wbim:operationalData")) {
						NodeList nodesInsideOperationalData = nodesInsideDecision.item(i).getChildNodes().item(k).getChildNodes();

						for (int w = 0; w < nodesInsideOperationalData.getLength(); w++) {

							if (nodesInsideOperationalData.item(w).getNodeName().equals("wbim:probability")) {
								NodeList nodesInsideProbability = nodesInsideOperationalData.item(w).getChildNodes();

								for (int y = 0; y < nodesInsideProbability.getLength(); y++) {
									if (nodesInsideProbability.item(y).getNodeName().equals("wbim:literalValue")) {
										if (NumberUtils.isNumber(nodesInsideProbability.item(y).getTextContent())) {
											probability = new Float(nodesInsideProbability.item(y).getTextContent());
										}
									}
								}
							}
						}
					}
				}

				outputBranch.setCondition(conditionName);
				outputBranch.setProbabilityPercentage(probability);

				flowControlComponent.getOutputBranches().add(outputBranch);

			}
		}

		if (flowControlComponent.getInputContactPoints().size() == 0 && flowControlComponent.getOutputContactPoints().size() == 0)
			flowControlComponent.buildDefaultContactPoints();

	}

	private static void parseInputContactPoints(ConnectableComponent component, Map<String, Document> mapDocumentEquivalence, NodeList nodeListInputs, boolean assignId, int[] globalId) {
		// if (node.getNodeName().equals("wbim:inputs")) {
		// NodeList nodesInput = node.getChildNodes();
		for (int w = 0; w < nodeListInputs.getLength(); w++) {
			if (nodeListInputs.item(w).getNodeName().equals("wbim:input")) {
				// String contactName =
				// nodeListInputs.item(w).getAttributes().getNamedItem("name").getNodeValue();
				//
				// String associatedData =
				// (nodeListInputs.item(w).getAttributes().getNamedItem("associatedData")
				// != null) ? nodeListInputs
				// .item(w).getAttributes().getNamedItem("associatedData").getNodeValue()
				// : null;
				//
				// InputContactPoint inputContactPoint = new
				// InputContactPoint(component, contactName);
				//
				// if (assignId)
				// inputContactPoint.setId(globalId[0]++);
				//
				// if (associatedData != null)
				// inputContactPoint.setAssociatedDocument(mapDocumentEquivalence.get(associatedData));

				InputContactPoint inputContactPoint = generateInputContactPointFromNode(component, mapDocumentEquivalence, nodeListInputs.item(w), assignId, globalId);

				component.getInputContactPoints().add(inputContactPoint);
			}
		}
		// }
	}

	private static InputContactPoint generateInputContactPointFromNode(ConnectableComponent component, Map<String, Document> mapDocumentEquivalence, Node inputNode, boolean assignId, int[] globalId) {
		String contactName = inputNode.getAttributes().getNamedItem("name").getNodeValue();

		String associatedData = (inputNode.getAttributes().getNamedItem("associatedData") != null) ? inputNode.getAttributes().getNamedItem("associatedData").getNodeValue() : null;

		InputContactPoint inputContactPoint = new InputContactPoint(component, contactName);

		if (assignId)
			inputContactPoint.setId(globalId[0]++);

		if (associatedData != null)
			inputContactPoint.setAssociatedDocument((associatedData.indexOf("##") > -1) ? mapDocumentEquivalence.get(associatedData) : new Document(associatedData, true));

		return inputContactPoint;
	}

	private static void parseOutputContactPoints(ConnectableComponent component, Map<String, Document> mapDocumentEquivalence, NodeList nodeListOutputs, boolean assignId, int[] globalId) {
		// if (node.getNodeName().equals("wbim:outputs")) {
		// NodeList nodesOutput = node.getChildNodes();
		for (int w = 0; w < nodeListOutputs.getLength(); w++) {
			if (nodeListOutputs.item(w).getNodeName().equals("wbim:output")) {
				// String contactName =
				// nodeListOutputs.item(w).getAttributes().getNamedItem("name").getNodeValue();
				//
				// String associatedData =
				// (nodeListOutputs.item(w).getAttributes().getNamedItem("associatedData")
				// != null) ? nodeListOutputs
				// .item(w).getAttributes().getNamedItem("associatedData").getNodeValue()
				// : null;
				//
				// OutputContactPoint outputContactPoint = new
				// OutputContactPoint(component, contactName);
				//
				// if (assignId)
				// outputContactPoint.setId(globalId[0]++);
				//
				// if (associatedData != null)
				// outputContactPoint.setAssociatedDocument(mapDocumentEquivalence.get(associatedData));

				OutputContactPoint outputContactPoint = generateOutputContactPointFromNode(component, mapDocumentEquivalence, nodeListOutputs.item(w), assignId, globalId);

				component.getOutputContactPoints().add(outputContactPoint);
			}
		}
		// }
	}

	private static OutputContactPoint generateOutputContactPointFromNode(ConnectableComponent component, Map<String, Document> mapDocumentEquivalence, Node outputNode, boolean assignId, int[] globalId) {
		String contactName = outputNode.getAttributes().getNamedItem("name").getNodeValue();

		String associatedData = (outputNode.getAttributes().getNamedItem("associatedData") != null) ? outputNode.getAttributes().getNamedItem("associatedData").getNodeValue() : null;

		OutputContactPoint outputContactPoint = new OutputContactPoint(component, contactName);

		if (assignId)
			outputContactPoint.setId(globalId[0]++);

		if (associatedData != null)
			outputContactPoint.setAssociatedDocument((associatedData.indexOf("##") > -1) ? mapDocumentEquivalence.get(associatedData) : new Document(associatedData, true));

		return outputContactPoint;
	}

	private static String normalizeName(String s) {
		return s.split("##")[1];
	}

	public static void main(String[] args) throws IOException, URISyntaxException, TemplateException, ParserConfigurationException, SAXException {

		String fileName = "d:/orderingprocess.xml";

		ProcessCatalogConverter converter = new ProcessCatalogConverter();

		CatalogService service = CatalogServiceLocator.get();

		Process process = converter.convertToCatalogFormat(service, new File(fileName), true, true, null);

		process.setName("Ordering Process");

		service.addOrUpdateProcess(18, process);

		// System.out.println(process);

		// Process process =
		// WebServiceLocator.locateCatalogService("http://localhost:8081/CatalogService")
		// .getProcessFromProcessStandardSpecification("ubl/payment/paymentprocess");
		//

		// ProcessCatalogConverter converter = new ProcessCatalogConverter();
		//
		// System.out.println(process);
		//

		// Process process = service.getProcessById(25);
		//
		// ProjectDefinition projectDefinition =
		// converter.convertToEditorFormat(process);
		//
		// System.out.println(projectDefinition);
		//
		// converter.outputEditorXML(projectDefinition,
		// "d:/testwsbm2-converted.xml");

	}
}