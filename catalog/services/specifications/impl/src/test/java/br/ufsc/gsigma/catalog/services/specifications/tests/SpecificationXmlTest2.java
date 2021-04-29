package br.ufsc.gsigma.catalog.services.specifications.tests;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.ufsc.gsigma.catalog.services.model.Document;
import br.ufsc.gsigma.catalog.services.model.ProcessStandard;
import br.ufsc.gsigma.catalog.services.specifications.impl.CatalogSpecificationServiceImpl;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessStandardSpecification;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaskTaxonomy;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy;
import br.ufsc.gsigma.infrastructure.util.xml.SimpleXMLSerializerUtil;

public class SpecificationXmlTest2 {

	public static void main(String[] args) throws Exception {

		ProcessStandardSpecification spec = null;

		try (InputStream in = SpecificationXmlTest2.class.getClassLoader().getResourceAsStream(CatalogSpecificationServiceImpl.PROCESSES_ROOT_PATH + "/ubl/specification.xml")) {
			spec = SimpleXMLSerializerUtil.read(ProcessStandardSpecification.class, in);
		}

		List<Document> docs = spec.getDocuments();

		ProcessStandard processStandard = docs.get(0).getProcessStandard();

		// id="UBL_REMITTANCE_ADVICE" name="UBL Remittance Advice" primitive="false" xmlNamespace="urn:oasis:names:specification:ubl:schema:xsd:RemittanceAdvice-2" xmlName="RemittanceAdviceType"

		docs.add(new Document("UBL Receipt Advice", "urn:oasis:names:specification:ubl:schema:xsd:ReceiptAdvice-2", "ReceiptAdviceType", processStandard));

		for (Document doc : docs) {
			System.out.println(doc);
		}

		System.out.println(spec);

		for (ProcessTaxonomy p : spec.getProcessTaxonomy().getChilds()) {
			if ("ubl/fulfilment".equals(p.getTaxonomyClassification())) {
				for (ProcessTaxonomy p2 : p.getChilds()) {
					if ("ubl/fulfilment/fulfilmentwithreceiptadvice".equals(p2.getTaxonomyClassification())) {
						if (p2.getTasks().isEmpty()) {

							// Delivery Party
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Receive Order Items");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/receiveOrderItems");
								ptax.setParticipantName("Delivery Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Check Status of Items");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/checkStatusOfItems");
								ptax.setParticipantName("Delivery Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Advise Buyer of Status");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/adviseBuyerOfStatus");
								ptax.setParticipantName("Delivery Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Send Receipt Advice");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice");
								ptax.setParticipantName("Delivery Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}

							// UBL Buyer Party
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Determine Action");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/determineAction");
								ptax.setParticipantName("Buyer Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Accept Items");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/acceptItems");
								ptax.setParticipantName("Buyer Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Adjust Order");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder");
								ptax.setParticipantName("Buyer Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Send Order Cancellation");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/sendOrderCancellation");
								ptax.setParticipantName("Buyer Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}

							// UBL Seller Party
							{
								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
								ptax.setName("Adjust Supply Status");
								ptax.setTaxonomyClassification("ubl/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus");
								ptax.setParticipantName("Seller Party");
								ptax.setParent(p2);
								p2.getTasks().add(ptax);
							}
						}
					}
				}
			}
		}

		Map<String, String> wsdlLocations = new TreeMap<String, String>(spec.getWsdlLocations());

		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/receiveOrderItems", "fulfilment/fulfilmentwithreceiptadvice/deliveryParty/UBL_FulfilmentWithReceiptAdvice_DeliveryParty_ReceiveOrderItems.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/checkStatusOfItems", "fulfilment/fulfilmentwithreceiptadvice/deliveryParty/UBL_FulfilmentWithReceiptAdvice_DeliveryParty_CheckStatusOfItems.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/adviseBuyerOfStatus", "fulfilment/fulfilmentwithreceiptadvice/deliveryParty/UBL_FulfilmentWithReceiptAdvice_DeliveryParty_AdviseBuyerOfStatus.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice", "fulfilment/fulfilmentwithreceiptadvice/deliveryParty/UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdvice.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/determineAction", "fulfilment/fulfilmentwithreceiptadvice/buyerParty/UBL_FulfilmentWithReceiptAdvice_BuyerParty_DetermineAction.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/acceptItems", "fulfilment/fulfilmentwithreceiptadvice/buyerParty/UBL_FulfilmentWithReceiptAdvice_BuyerParty_AcceptItems.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", "fulfilment/fulfilmentwithreceiptadvice/buyerParty/UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustOrder.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/buyerParty/sendOrderCancellation", "fulfilment/fulfilmentwithreceiptadvice/buyerParty/UBL_FulfilmentWithReceiptAdvice_BuyerParty_SendOrderCancellation.wsdl");
		wsdlLocations.put("ubl/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", "fulfilment/fulfilmentwithreceiptadvice/sellerParty/UBL_FulfilmentWithReceiptAdvice_SellerParty_AdjustSupplyStatus.wsdl");

		spec.setWsdlLocations(wsdlLocations);

		try (OutputStream out = new FileOutputStream("specification-ubl.xml")) {
			SimpleXMLSerializerUtil.write(spec, out);
		}

	}

}
