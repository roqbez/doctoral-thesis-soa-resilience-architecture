package br.ufsc.gsigma.catalog.plugin.modules.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.xml.sax.SAXException;

import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
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

import br.ufsc.gsigma.catalog.services.locator.CatalogServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public abstract class ProcessConverterHelper {

	public static br.ufsc.gsigma.catalog.services.model.Process getProcess(NavigationProcessNode processNode, boolean assignId, boolean processDataModel, ExecutionContext executionContext) throws IOException, ParserConfigurationException, SAXException {

		List<Element> elements = new ArrayList<Element>();

		String projectName = processNode.getProjectNode().getLabel();
		String projectPath = FileMGR.getProjectPath(projectName);

		for (Object o1 : ResourceMGR.getResourceManger().getRootObjects(projectName, projectPath, (String) processNode.getEntityReferences().get(0)))
			elements.add((Element) o1);

		MapperContext context = new MapperContext();
		ModelType modelType = BomXMLUtils.getModel(context);
		modelType.setSchemaVersion("7.0.0");

		BomXMLUtils.setProjectNode(processNode.getProjectNode());

		if (!elements.isEmpty()) {
			for (Element element : elements)
				BomXMLUtils.mapBomSources(context, element, 1);
		}

		context.put("projectName", projectName);
		context.put("isPartialExport", new Boolean(BomXMLUtils.isEntireProjectExport(elements)));

		BomXMLUtils.clear();

		ModelAlphabeticOrderSorter.sortModel(modelType);

		DocumentRoot document = ModelFactory.eINSTANCE.createDocumentRoot();
		document.setModel(modelType);

		Calendar now = Calendar.getInstance();

		String tmpPath = new File(System.getProperty("java.io.tmpdir"), "bpmn-process-" + now.getTimeInMillis()).getAbsolutePath();

		String fileName = tmpPath + File.separator + "tmp" + ".xml";

		processResult(document, fileName);

		ProcessCatalogConverter converter = new ProcessCatalogConverter();

		File file = new File(fileName);
		try {
			br.ufsc.gsigma.catalog.services.model.Process process = converter.convertToCatalogFormat(CatalogServiceLocator.get(), file, assignId, processDataModel, executionContext);
			return process;
		} finally {
			file.delete();
		}

	}

	private static void processResult(DocumentRoot document, String fileName) throws IOException {
		XMLResource xmlResource = (XMLResource) getXMLResource(fileName);
		xmlResource.setEncoding("UTF-8");
		xmlResource.getContents().add(document);
		saveXMLResource(xmlResource);
	}

	private static Resource getXMLResource(String fileName) {
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new ModelResourceFactoryImpl());
		URI uri = URI.createFileURI(fileName);
		return resourceSet.createResource(uri);
	}

	private static void saveXMLResource(XMLResource xmlResource) throws IOException {
		Map<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("DECLARE_XML", Boolean.TRUE);
		localHashMap.put("ENCODING", "UTF-8");
		localHashMap.put("SCHEMA_LOCATION", Boolean.TRUE);
		localHashMap.put("USE_ENCODED_ATTRIBUTE_STYLE", Boolean.FALSE);
		xmlResource.save(localHashMap);
	}

}
