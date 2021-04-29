package br.ufsc.gsigma.catalog.plugin.mock;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.ProjectDefinition;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMConnection;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMEnd;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMInputContactPoint;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMOutputContactPoint;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessCatalog;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMRoleResourceRequisite;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMStart;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTask;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMResourceCatalog;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.resource.BMRole;

public class ProcessCatalogMock {

	private ProcessCatalogMock() {
	}

	public static ProjectDefinition getProjectDefinition() {

		ProjectDefinition projectDefinition = new ProjectDefinition();

		BMResourceCatalog resourceCatalog = projectDefinition.getResourceModel()
				.getResourceCatalogs().get(0);
		BMProcessCatalog processCatalog = projectDefinition.getProcessModel()
				.getProcessCatalogs().get(0);

		// Definição de Funções

		BMRole roleAccountingCustomer = resourceCatalog
				.addRole("Accounting Customer");
		BMRole roleAccountingSupplier = resourceCatalog
				.addRole("Accounting Supplier");
		BMRole rolePayeeParty = resourceCatalog.addRole("Payee Party");

		// Processo
		br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcess process = processCatalog
				.addProcess("Payment Process");

		// Início
		BMStart start = new BMStart("Start");
		process.getStartNodes().add(start);

		// Tarefas

		BMTask t1 = new BMTask("Authorize Payment");
		t1.getResourceRequisites().add(
				new BMRoleResourceRequisite("Executor", roleAccountingCustomer));
		process.getTasks().add(t1);

		BMTask t2 = new BMTask("Notify of Payment");
		t2.clearContactPoints();
		t2.getInputContactPoint().add(new BMInputContactPoint(t2, "Input"));
		t2.getOutputContactPoint().add(new BMOutputContactPoint(t2, "Output1"));
		t2.getOutputContactPoint().add(new BMOutputContactPoint(t2, "Output2"));
		t2.getResourceRequisites().add(
				new BMRoleResourceRequisite("Executor", roleAccountingCustomer));
		process.getTasks().add(t2);

		BMTask t3 = new BMTask("Receive Advice from Accounting Customer");
		t3.getResourceRequisites().add(
				new BMRoleResourceRequisite("Executor", roleAccountingSupplier));
		process.getTasks().add(t3);

		BMTask t4 = new BMTask("Notify Payee");
		t4.getResourceRequisites().add(
				new BMRoleResourceRequisite("Executor", roleAccountingSupplier));
		process.getTasks().add(t4);

		BMTask t5 = new BMTask("Receive Advice from Accounting Supplier");
		t5.clearContactPoints();
		t5.getInputContactPoint().add(new BMInputContactPoint(t5, "Input1"));
		t5.getInputContactPoint().add(new BMInputContactPoint(t5, "Input2"));
		t5.getOutputContactPoint().add(new BMOutputContactPoint(t5, "Output"));

		t5.getResourceRequisites().add(
				new BMRoleResourceRequisite("Executor", rolePayeeParty));
		process.getTasks().add(t5);

		// Fim
		BMEnd end = new BMEnd("End");
		process.getEndNodes().add(end);

		// Conexões
		process.getConnections().add(
				new BMConnection(start.getOutputContactPoint().get(0), t1
						.getInputContactPoint().get(0)));
		process.getConnections().add(
				new BMConnection(t1.getOutputContactPoint().get(0), t2
						.getInputContactPoint().get(0)));
		process.getConnections().add(
				new BMConnection(t2.getOutputContactPoint().get(0), t3
						.getInputContactPoint().get(0)));
		process.getConnections().add(
				new BMConnection(t2.getOutputContactPoint().get(1), t5
						.getInputContactPoint().get(0)));
		process.getConnections().add(
				new BMConnection(t3.getOutputContactPoint().get(0), t4
						.getInputContactPoint().get(0)));
		process.getConnections().add(
				new BMConnection(t4.getOutputContactPoint().get(0), t5
						.getInputContactPoint().get(1)));
		process.getConnections().add(
				new BMConnection(t5.getOutputContactPoint().get(0), end
						.getInputContactPoint().get(0)));

		return projectDefinition;

	}

}
