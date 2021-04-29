package br.ufsc.gsigma.catalog.plugin.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.ufsc.gsigma.catalog.services.model.ConnectableComponent;
import br.ufsc.gsigma.catalog.services.model.EndEvent;
import br.ufsc.gsigma.catalog.services.model.InputContactPoint;
import br.ufsc.gsigma.catalog.services.model.OutputContactPoint;
import br.ufsc.gsigma.catalog.services.model.Participant;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.StartEvent;
import br.ufsc.gsigma.catalog.services.model.Task;

import com.ibm.btools.blm.ui.navigation.model.NavigationProcessCatalogNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.blm.ui.navigation.model.NavigationProjectNode;
import com.ibm.btools.bom.model.artifacts.Element;
import com.ibm.btools.model.filemanager.FileMGR;
import com.ibm.btools.model.resourcemanager.ResourceMGR;
import com.ibm.btools.te.xml.MapperContext;
import com.ibm.btools.te.xml.export.helper.BomXMLUtils;
import com.ibm.btools.te.xml.model.DocumentRoot;
import com.ibm.btools.te.xml.model.ModelFactory;
import com.ibm.btools.te.xml.model.ModelType;
import com.ibm.btools.te.xml.model.util.ModelAlphabeticOrderSorter;
import com.ibm.btools.te.xml.model.util.ModelResourceFactoryImpl;

public class ExportTest {

	public static void main(String[] args) {

		List<Node> componentsNodeBuffer = new ArrayList<Node>();
		List<Node> connectionsNodeBuffer = new ArrayList<Node>();
		Map<String, Participant> mapParticipantEquivalence = new HashMap<String, Participant>();
		Map<String, Process> mapProcessEquivalence = new HashMap<String, Process>();

		Map<String, ConnectableComponent> mapConnectableComponentEquivalence = new HashMap<String, ConnectableComponent>();
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File("d:/Teste-Payment Process.xml"));

			doc.getDocumentElement().normalize();

			System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());

			// Resource Model
			Node nodeResourceModel = doc.getElementsByTagName("wbim:resourceModel").item(0);
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

			// Process Model
			Node nodeProcessModel = doc.getElementsByTagName("wbim:processModel").item(0);
			for (int i = 0; i < nodeProcessModel.getChildNodes().getLength(); i++) {

				// Process
				if (nodeProcessModel.getChildNodes().item(i).getNodeName().equals("wbim:processes")) {

					NodeList nodeListProcess = nodeProcessModel.getChildNodes().item(i).getChildNodes();

					for (int k = 0; k < nodeListProcess.getLength(); k++) {
						if (nodeListProcess.item(k).getNodeName().equals("wbim:process")) {
							String processName = nodeListProcess.item(k).getAttributes().getNamedItem("name").getNodeValue();
							System.out.println(processName);
							Process process = new Process(normalizeName(processName));
							mapProcessEquivalence.put(processName, process);

							// Flow Content
							NodeList nodeFlowContent = nodeListProcess.item(k).getChildNodes();
							for (int w = 0; w < nodeFlowContent.getLength(); w++) {
								if (nodeFlowContent.item(w).getNodeName().equals("wbim:flowContent")) {

									NodeList nodesInsideFlowContent = nodeFlowContent.item(w).getChildNodes();

									for (int z = 0; z < nodesInsideFlowContent.getLength(); z++) {

										// Tasks
										if (nodesInsideFlowContent.item(z).getNodeName().equals("wbim:task")//
												|| nodesInsideFlowContent.item(z).getNodeName().equals("wbim:startNode")//
												|| nodesInsideFlowContent.item(z).getNodeName().equals("wbim:endNode"))//
											componentsNodeBuffer.add(nodesInsideFlowContent.item(z));

										if (nodesInsideFlowContent.item(z).getNodeName().equals("wbim:connection"))
											connectionsNodeBuffer.add(nodesInsideFlowContent.item(z));

									}
								}
							}

							for (Node node : componentsNodeBuffer) {
								parseStartNode(mapConnectableComponentEquivalence, process, node);
								parseEndNode(mapConnectableComponentEquivalence, process, node);
								parseTask(mapParticipantEquivalence, mapConnectableComponentEquivalence, process, node);
							}
							for (Node node : connectionsNodeBuffer) {
								parseConnectionNode(mapConnectableComponentEquivalence, process, node);
							}

						}
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void parseConnectionNode(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Process process, Node node) {
		if (node.getNodeName().equals("wbim:connection")) {
//			String connectionNodeName = node.getAttributes().getNamedItem("name").getNodeValue();

			String sourceComponent = null;
			String sourceContactPoint = null;

			String targetComponent = null;
			String targetContactPoint = null;

			NodeList nodesInsideConnection = node.getChildNodes();

			for (int i = 0; i < nodesInsideConnection.getLength(); i++) {

				// Source
				if (nodesInsideConnection.item(i).getNodeName().equals("wbim:source")) {

					sourceComponent = nodesInsideConnection.item(i).getAttributes().getNamedItem("node").getNodeValue();
					sourceContactPoint = (nodesInsideConnection.item(i).getAttributes().getNamedItem("contactPoint") != null) ? nodesInsideConnection
							.item(i).getAttributes().getNamedItem("contactPoint").getNodeValue()
							: null;
				}
				// Target
				if (nodesInsideConnection.item(i).getNodeName().equals("wbim:target")) {

					targetComponent = nodesInsideConnection.item(i).getAttributes().getNamedItem("node").getNodeValue();
					targetContactPoint = (nodesInsideConnection.item(i).getAttributes().getNamedItem("contactPoint") != null) ? nodesInsideConnection
							.item(i).getAttributes().getNamedItem("contactPoint").getNodeValue()
							: null;
				}

			}

			OutputContactPoint outputContactPoint = (sourceContactPoint != null) ? mapConnectableComponentEquivalence.get(sourceComponent)
					.getOutputContactPointByName(sourceContactPoint) : mapConnectableComponentEquivalence.get(sourceComponent)
					.getOutputContactPoints().get(0);

			InputContactPoint inputContactPoint = (sourceContactPoint != null) ? mapConnectableComponentEquivalence.get(targetComponent)
					.getInputContactPointByName(targetContactPoint) : mapConnectableComponentEquivalence.get(targetComponent)
					.getInputContactPoints().get(0);

			process.addConnection(outputContactPoint, inputContactPoint);

		}
	}

	private static void parseStartNode(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Process process, Node node) {
		if (node.getNodeName().equals("wbim:startNode")) {
			String startNodeName = node.getAttributes().getNamedItem("name").getNodeValue();

			StartEvent startEvent = new StartEvent(process, startNodeName);
			mapConnectableComponentEquivalence.put(startNodeName, startEvent);

			NodeList nodesInsideStartNode = node.getChildNodes();

			for (int i = 0; i < nodesInsideStartNode.getLength(); i++) {

				// Inputs
				parseInputContactPoints(startEvent, nodesInsideStartNode.item(i));

				// Outputs
				parseOutputContactPoints(startEvent, nodesInsideStartNode.item(i));

			}

		}
	}

	private static void parseEndNode(Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Process process, Node node) {
		if (node.getNodeName().equals("wbim:endNode")) {
			String endNodeName = node.getAttributes().getNamedItem("name").getNodeValue();

			EndEvent endEvent = new EndEvent(process, endNodeName);
			mapConnectableComponentEquivalence.put(endNodeName, endEvent);

			NodeList nodesInsideEndNode = node.getChildNodes();

			for (int i = 0; i < nodesInsideEndNode.getLength(); i++) {

				// Inputs
				parseInputContactPoints(endEvent, nodesInsideEndNode.item(i));

				// Outputs
				parseOutputContactPoints(endEvent, nodesInsideEndNode.item(i));

			}

		}
	}

	private static void parseTask(Map<String, Participant> mapParticipantEquivalence,
			Map<String, ConnectableComponent> mapConnectableComponentEquivalence, Process process, Node node) {
		if (node.getNodeName().equals("wbim:task")) {
			String taskName = node.getAttributes().getNamedItem("name").getNodeValue();

			System.out.println("\t" + taskName);
			Task task = new Task(process, taskName);
			mapConnectableComponentEquivalence.put(taskName, task);

			NodeList nodesInsideTask = node.getChildNodes();

			for (int i = 0; i < nodesInsideTask.getLength(); i++) {

				// Description (vai ser usado para Qos e UBL)
				if (nodesInsideTask.item(i).getNodeName().equals("wbim:description")) {

				}

				// Inputs
				parseInputContactPoints(task, nodesInsideTask.item(i));

				// Outputs
				parseOutputContactPoints(task, nodesInsideTask.item(i));

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

		}
	}

	private static void parseInputContactPoints(ConnectableComponent component, Node node) {
		if (node.getNodeName().equals("wbim:inputs")) {
			NodeList nodesInput = node.getChildNodes();
			for (int w = 0; w < nodesInput.getLength(); w++) {
				if (nodesInput.item(w).getNodeName().equals("wbim:input")) {
					String contactName = nodesInput.item(w).getAttributes().getNamedItem("name").getNodeValue();
					component.getInputContactPoints().add(new InputContactPoint(component, contactName));
				}
			}
		}
	}

	private static void parseOutputContactPoints(ConnectableComponent component, Node node) {
		if (node.getNodeName().equals("wbim:outputs")) {
			NodeList nodesOutput = node.getChildNodes();
			for (int w = 0; w < nodesOutput.getLength(); w++) {
				if (nodesOutput.item(w).getNodeName().equals("wbim:output")) {
					String contactName = nodesOutput.item(w).getAttributes().getNamedItem("name").getNodeValue();
					component.getOutputContactPoints().add(new OutputContactPoint(component, contactName));
				}
			}
		}
	}

	private static String normalizeName(String s) {
		return s.split("##")[1];
	}

	@SuppressWarnings({ "unused", "null" })
	private void export() {
		NavigationProjectNode nodeProject = null;

		try {

			// NavigationProjectNode projectNodes = getExistingProjectNodes();

			NavigationProjectNode[] projectNodes = null;
			List<Element> elements = null;

			for (Object o : projectNodes) {
				NavigationProjectNode node = (NavigationProjectNode) o;

				nodeProject = node;

				for (NavigationProcessCatalogNode n : node.getLibraryNode().getProcessCatalogsNodes().getProcessCatalogNodes()) {

					System.out.println(n.getLabel() + " - ");

					String path = FileMGR.getProjectPath("Teste");

					for (NavigationProcessNode n1 : n.getProcessesNode().getProcessNodes()) {

						// Processos

						for (Object o1 : ResourceMGR.getResourceManger().getRootObjects("Teste", path,
								(String) n1.getEntityReferences().get(0)))
							elements.add((Element) o1);

						System.out.println("\t" + n1.getLabel());
					}

				}

			}

			System.out.println("elements=" + elements);

			MapperContext ivContext = new MapperContext();
			ModelType localModelType = BomXMLUtils.getModel(ivContext);
			localModelType.setSchemaVersion("7.0.0");

			BomXMLUtils.setProjectNode(nodeProject);

			if (!elements.isEmpty()) {

				for (Element element : elements) {
					ivContext.put("projectName", "Teste");
					ivContext.put("isPartialExport", new Boolean(BomXMLUtils.isEntireProjectExport(elements)));
					BomXMLUtils.mapBomSources(ivContext, element, 1);
				}
			}
			BomXMLUtils.clear();

			processResult(ivContext, localModelType);

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private void processResult(MapperContext paramMapperContext, ModelType modelType) {

		String fileName = "d:/testeXml.xml";

		ModelAlphabeticOrderSorter.sortModel(modelType);

		try {
			XMLResource xmlResource = (XMLResource) getXMLResource(fileName);
			xmlResource.setEncoding("UTF-8");

			DocumentRoot document = ModelFactory.eINSTANCE.createDocumentRoot();
			document.setModel(modelType);

			xmlResource.getContents().add(document);

			saveXMLResource(xmlResource);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private void saveXMLResource(XMLResource xmlResource) {
		Map<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("DECLARE_XML", Boolean.TRUE);
		localHashMap.put("ENCODING", "UTF-8");
		localHashMap.put("SCHEMA_LOCATION", Boolean.TRUE);
		localHashMap.put("USE_ENCODED_ATTRIBUTE_STYLE", Boolean.FALSE);
		try {
			xmlResource.save(localHashMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Resource getXMLResource(String fileName) {
		ResourceSetImpl resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new ModelResourceFactoryImpl());
		URI uri = URI.createFileURI(fileName);
		return resourceSet.createResource(uri);
	}

}
