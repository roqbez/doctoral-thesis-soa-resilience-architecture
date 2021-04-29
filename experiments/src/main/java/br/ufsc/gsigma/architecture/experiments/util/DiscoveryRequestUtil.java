package br.ufsc.gsigma.architecture.experiments.util;

import java.util.List;

import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequest;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequestItem;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryResult;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSValueComparisionType;

public abstract class DiscoveryRequestUtil {

	public static void main(String[] args) throws Exception {

		DiscoveryService discoveryService = ServiceClient.getClient(DiscoveryService.class, "http://discoveryservice.d-201603244.ufsc.br:8070/services/DiscoveryService", CxfUtil.getClientFeatures());

		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/acceptCatalogue", 180.0D, "publisher48", "0.6451486213091029");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/acknowledgeAcceptance", 545.0D, "publisher48", "0.8155214091083628");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/cancelTransaction", 179.0D, "publisher69", "1.0292034390821996");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/decideOnAction", 549.0D, "publisher69", "0.3029404214932624");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/distributeCatalogue", 548.0D, "publisher72", "0.7227316402382346");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/prepareCatalogueInformation", 548.0D, "publisher72", "0.2708721297205253");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/processCatalogueRequest", 546.0D, "publisher69", "0.6626042459462903");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/produceCatalogue", 547.0D, "publisher69", "0.45977864690367903");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/queryCatalogueContent", 525.0D, "publisher48", "1.1432720505050722");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/receiveAcknowledgeAcceptance", 179.0D, "publisher72", "0.35715459964621904");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/receiveCatalogue", 550.0D, "publisher48", "0.867283272701261");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/receiveRejection", 539.0D, "publisher48", "0.9992687047864619");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/requestCatalogue", 549.0D, "publisher48", "0.9509652880738387");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/respondToRequest", 550.0D, "publisher72", "0.1089724206063511");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/receiverParty/reviewCatalogueContent", 547.0D, "publisher48", "0.22538497183693862");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/reviseContent", 174.0D, "publisher72", "0.7380598734571338");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendAcceptanceResponse", 549.0D, "publisher69", "0.19639040127792756");//
		printXml(discoveryService, "ubl/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", 549.0D, "publisher72", "1.0558679379522258");//

	}

	private static void printXml(DiscoveryService discoveryService, String serviceClassification, double responseTimeConstraint, String serviceProvider, String utility) throws Exception {
		DiscoveryRequest req = new DiscoveryRequest();

		DiscoveryRequestItem item = new DiscoveryRequestItem();
		item.setMaxResults(5);

		item.setServiceClassification(serviceClassification);

		item.setServiceProvider(serviceProvider);

		QoSInformation qInfo = new QoSInformation();
		qInfo.addManagedQoSConstraint("Performance.ResponseTime", QoSValueComparisionType.LE, responseTimeConstraint);
		item.setQoSInformation(qInfo);

		req.getItens().add(item);

		DiscoveryResult result = discoveryService.discover(req);

		List<DiscoveredService> matchingServices = result.getMatchingServices();
		matchingServices.sort((DiscoveredService d1, DiscoveredService d2) -> d1.getServiceEndpointURL().compareTo(d2.getServiceEndpointURL()));

		for (DiscoveredService s : matchingServices) {
			String xml = "<serviceAssociation " //
					+ "serviceProviderName=\"" + s.getServiceProvider().getIdentification() + "\" " //
					+ "serviceEndpoint=\"" + s.getServiceEndpointURL() + "\" " //
					+ "serviceUtility=\"" + utility + "\" " //
					+ "serviceKey=\"" + s.getServiceKey() + "\" " //
					+ "serviceName=\"" + s.getServiceName() + "\" " //
					+ "bindingTemplateKey=\"" + s.getBindingTemplateKey() + "\" " //
					+ "/>";

			System.out.println(xml);
		}
		System.out.println("\n");
	}

}
