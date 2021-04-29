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

public class SpecificationXmlTest3 {

	public static void main(String[] args) throws Exception {

		ProcessStandardSpecification spec = null;

		try (InputStream in = SpecificationXmlTest3.class.getClassLoader().getResourceAsStream(CatalogSpecificationServiceImpl.PROCESSES_ROOT_PATH + "/ubl/specification.xml")) {
			spec = SimpleXMLSerializerUtil.read(ProcessStandardSpecification.class, in);
		}

		List<Document> docs = spec.getDocuments();

		ProcessStandard processStandard = docs.get(0).getProcessStandard();

		docs.add(new Document("UBL Invoice", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "InvoiceType", processStandard));
		docs.add(new Document("UBL Credit Note", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "CreditNoteType", processStandard));

		for (Document doc : docs) {
			System.out.println(doc);
		}

		System.out.println(spec);

		for (ProcessTaxonomy p : spec.getProcessTaxonomy().getChilds()) {
			if ("ubl/billing".equals(p.getTaxonomyClassification())) {
				for (ProcessTaxonomy p2 : p.getChilds()) {
					if ("ubl/billing/traditionalbilling".equals(p2.getTaxonomyClassification())) {

						for (ProcessTaxonomy p3 : p2.getChilds()) {

							if ("ubl/billing/traditionalbilling/billingwithcreditnote".equals(p3.getTaxonomyClassification())) {

								if (p3.getTasks().isEmpty()) {

									// Accounting Supplier
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Accounting Supplier Reconcile Charges");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/reconcileCharges");
										ptax.setParticipantName("Accounting Supplier");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Raise Invoice");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/raiseInvoice");
										ptax.setParticipantName("Accounting Supplier");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Raise Credit Note");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/raiseCreditNote");
										ptax.setParticipantName("Accounting Supplier");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Receive Account Response");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/receiveAccountResponse");
										ptax.setParticipantName("Accounting Supplier");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Validate Response");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/validateResponse");
										ptax.setParticipantName("Accounting Supplier");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}

									// Accounting Customer
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Receive Invoice");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/receiveInvoice");
										ptax.setParticipantName("Accounting Customer");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Receive Credit Note");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/receiveCreditNote");
										ptax.setParticipantName("Accounting Customer");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Accounting Customer Reconcile Charges");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/reconcileCharges");
										ptax.setParticipantName("Accounting Customer");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Accept Credit");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/acceptCredit");
										ptax.setParticipantName("Accounting Customer");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Accept Charges");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/acceptCharges");
										ptax.setParticipantName("Accounting Customer");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
									{
										ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();
										ptax.setName("Send Account Response");
										ptax.setTaxonomyClassification("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/sendAccountResponse");
										ptax.setParticipantName("Accounting Customer");
										ptax.setParent(p3);
										p3.getTasks().add(ptax);
									}
								}
							}
						}
					}
				}
			}
		}

		Map<String, String> wsdlLocations = new TreeMap<String, String>(spec.getWsdlLocations());

		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/acceptCharges", "billing/traditionalbilling/billingwithcreditnote/accountingCustomer/UBL_BillingWithCreditNote_AccountingCustomer_AcceptCharges.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/acceptCredit", "billing/traditionalbilling/billingwithcreditnote/accountingCustomer/UBL_BillingWithCreditNote_AccountingCustomer_AcceptCredit.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/receiveCreditNote", "billing/traditionalbilling/billingwithcreditnote/accountingCustomer/UBL_BillingWithCreditNote_AccountingCustomer_ReceiveCreditNote.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/receiveInvoice", "billing/traditionalbilling/billingwithcreditnote/accountingCustomer/UBL_BillingWithCreditNote_AccountingCustomer_ReceiveInvoice.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/reconcileCharges", "billing/traditionalbilling/billingwithcreditnote/accountingCustomer/UBL_BillingWithCreditNote_AccountingCustomer_ReconcileCharges.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingCustomer/sendAccountResponse", "billing/traditionalbilling/billingwithcreditnote/accountingCustomer/UBL_BillingWithCreditNote_AccountingCustomer_SendAccountResponse.wsdl");

		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/raiseCreditNote", "billing/traditionalbilling/billingwithcreditnote/accountingSupplier/UBL_BillingWithCreditNote_AccountingSupplier_RaiseCreditNote.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/raiseInvoice", "billing/traditionalbilling/billingwithcreditnote/accountingSupplier/UBL_BillingWithCreditNote_AccountingSupplier_RaiseInvoice.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/receiveAccountResponse", "billing/traditionalbilling/billingwithcreditnote/accountingSupplier/UBL_BillingWithCreditNote_AccountingSupplier_ReceiveAccountResponse.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/reconcileCharges", "billing/traditionalbilling/billingwithcreditnote/accountingSupplier/UBL_BillingWithCreditNote_AccountingSupplier_ReconcileCharges.wsdl");
		wsdlLocations.put("ubl/billing/traditionalbilling/billingwithcreditnote/accountingSupplier/validateResponse", "billing/traditionalbilling/billingwithcreditnote/accountingSupplier/UBL_BillingWithCreditNote_AccountingSupplier_ValidateResponse.wsdl");

		spec.setWsdlLocations(wsdlLocations);

		try (OutputStream out = new FileOutputStream("specification-ubl.xml")) {
			SimpleXMLSerializerUtil.write(spec, out);
		}

	}

}
