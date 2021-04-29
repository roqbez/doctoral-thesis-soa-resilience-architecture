package br.ufsc.gsigma.catalog.plugin.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ibm.btools.blm.ui.attributesview.model.ModelAccessor;
import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;
import com.ibm.btools.bom.model.processes.activities.Activity;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;

import br.ufsc.gsigma.catalog.plugin.modules.converter.ProcessConverterHelper;
import br.ufsc.gsigma.catalog.services.model.ITConfiguration;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.services.deployment.model.DeploymentRequest;

public abstract class DeploymentUtil {

	public static DeploymentRequest getDeploymentRequest(ModelAccessor ivModelAccessor) throws IOException, ParserConfigurationException, SAXException {

		final String processName = ivModelAccessor.getModel() instanceof StructuredActivityNode ? ((StructuredActivityNode) ivModelAccessor.getModel()).getName() : ((Activity) ivModelAccessor.getModel()).getName();

		final String currentProject = ProcessNodeHelper.getCurrentProjectSelection();

		final NavigationProcessNode navigationProcessNode = ProcessNodeHelper.getProcess(currentProject, processName);

		return getDeploymentRequest(navigationProcessNode);
	}

	public static DeploymentRequest getDeploymentRequest(final NavigationProcessNode navigationProcessNode) throws IOException, ParserConfigurationException, SAXException {

		final Process businessProcess = ProcessConverterHelper.getProcess(navigationProcessNode, true, true, null);

		ITConfiguration itConfiguration = businessProcess.getItConfiguration();

		DeploymentRequest deploymentRequest = new DeploymentRequest();
		deploymentRequest.setBusinessProcessName(businessProcess.getName());
		deploymentRequest.setBusinessProcess(businessProcess);

		deploymentRequest.setItConfiguration(itConfiguration);

		return deploymentRequest;
	}

}
