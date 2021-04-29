package br.ufsc.gsigma.catalog.plugin.attributes;

import org.apache.commons.lang.ArrayUtils;

import br.ufsc.gsigma.catalog.plugin.modules.deployment.DeploymentTab;
import br.ufsc.gsigma.catalog.plugin.modules.qos.process.ProcessQoSTab;
import br.ufsc.gsigma.catalog.plugin.modules.qos.task.TaskQoSTab;
import br.ufsc.gsigma.catalog.plugin.modules.resilience.process.ProcessResilienceTab;
import br.ufsc.gsigma.catalog.plugin.modules.resilience.task.TaskResilienceTab;
import br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.process.ProcessServiceAssociationTab;
import br.ufsc.gsigma.catalog.plugin.modules.serviceassociation.task.TaskServiceAssociationTab;

import com.ibm.btools.blm.ui.attributesview.ContentPageFactory;
import com.ibm.btools.ui.attributesview.IContentPage;

public class CatalogContentPageFactory extends ContentPageFactory {

	@Override
	public IContentPage[] createContentPages(@SuppressWarnings("rawtypes") Class clazz) {

		IContentPage[] tabs = super.createContentPages(clazz);

		if (clazz.getName().equals("com.ibm.btools.blm.ui.attributesview.ContentPageFactory$TopLevelProcess")) {

			ProcessServiceAssociationTab processServiceAssociationTab = new ProcessServiceAssociationTab();
			ProcessQoSTab processQoSTab = new ProcessQoSTab(processServiceAssociationTab);
			ProcessResilienceTab processResilienceTab = new ProcessResilienceTab();
			DeploymentTab deploymentTab = new DeploymentTab();

			tabs = (IContentPage[]) ArrayUtils.addAll(tabs, new Object[] { processQoSTab, processServiceAssociationTab, processResilienceTab, deploymentTab });

		} else if (clazz.getName().equals("com.ibm.btools.blm.ui.attributesview.ContentPageFactory$LocalTask")) {

			TaskQoSTab taskQoSTab = new TaskQoSTab();
			TaskServiceAssociationTab taskServiceAssociationTab = new TaskServiceAssociationTab();
			TaskResilienceTab taskResilienceTab = new TaskResilienceTab();

			tabs = (IContentPage[]) ArrayUtils.addAll(tabs, new Object[] { taskQoSTab, taskServiceAssociationTab, taskResilienceTab });
		}

		return tabs;
	}

}
