package br.ufsc.gsigma.infrastructure.ws.uddi;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.juddi.api_v3.Publisher;
import org.apache.juddi.api_v3.SavePublisher;
import org.apache.juddi.v3.client.transport.Transport;
import org.uddi.api_v3.AuthToken;
import org.uddi.api_v3.BusinessDetail;
import org.uddi.api_v3.BusinessService;
import org.uddi.api_v3.CategoryBag;
import org.uddi.api_v3.GetAuthToken;
import org.uddi.api_v3.KeyedReference;
import org.uddi.api_v3.Name;
import org.uddi.api_v3.SaveTModel;
import org.uddi.api_v3.TModel;
import org.uddi.api_v3.TModelInstanceInfo;
import org.uddi.v3_service.UDDISecurityPortType;

import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public abstract class UBLUddiUtil {

	public static final String UBL_SERVICE_URL_PREFIX = getUBLServiceUrlPrefix(ServicesAddresses.UBL_SERVICES_HOSTNAME, ServicesAddresses.UBL_SERVICES_PORT);

	public static String getUBLServiceUrlPrefix(String hostname, int port) {
		return "http://" + hostname + ":" + port + "/services";
	}

	public static void createPublishers(Transport transport, int k) throws Exception {

		UDDISecurityPortType security = transport.getUDDISecurityService();

		GetAuthToken getAuthTokenRoot = new GetAuthToken();
		getAuthTokenRoot.setUserID("ubl");
		getAuthTokenRoot.setCred("");

		AuthToken authToken = security.getAuthToken(getAuthTokenRoot);

		for (int i = 1; i <= k; i++) {

			SavePublisher savePublisher = new SavePublisher();
			savePublisher.setAuthInfo(authToken.getAuthInfo());

			String authorizedName = "publisher" + i;

			Publisher publisher = new Publisher();
			publisher.setAuthorizedName("ubl1");
			publisher.setPublisherName("Company " + i);
			publisher.setIsAdmin(false);

			savePublisher.getPublisher().add(publisher);
			transport.getJUDDIApiService().savePublisher(savePublisher);

			getAuthTokenRoot = new GetAuthToken();
			getAuthTokenRoot.setUserID(authorizedName);
			getAuthTokenRoot.setCred("");

			// Keygen
			SaveTModel st = new SaveTModel();
			st.setAuthInfo(security.getAuthToken(getAuthTokenRoot).getAuthInfo());
			TModel tm = new TModel();
			tm.setName(new Name());
			tm.getName().setValue("Publisher " + i + " Keymodel generator");
			tm.getName().setLang("en");
			tm.setCategoryBag(new CategoryBag());
			KeyedReference kr = new KeyedReference();
			kr.setTModelKey("uddi:uddi.org:categorization:types");
			kr.setKeyName("uddi-org:keyGenerator");
			kr.setKeyValue("keyGenerator");
			tm.getCategoryBag().getKeyedReference().add(kr);
			tm.setTModelKey("uddi:" + authorizedName + ":keygenerator");
			st.getTModel().add(tm);

			transport.getUDDIPublishService().saveTModel(st);

		}

	}

	public static List<BusinessService> populateServices(Transport transport, String authLogin, String authPassword, String businessKey, String serviceKeyPrefix, String businessEntityName, String nodeName, Map<String, Integer> mapServiceKeyPrefixCount) throws Exception {

		if (businessKey == null) {
			BusinessDetail businessDetail = UddiRegister.createBusinessEntity(transport, authLogin, authPassword, businessEntityName);
			if (businessDetail.getBusinessEntity() != null && businessDetail.getBusinessEntity().size() > 0) {
				businessKey = businessDetail.getBusinessEntity().get(0).getBusinessKey();
			}
		}

		List<BusinessService> services = getRandomBusinessServices(businessKey, serviceKeyPrefix, nodeName, mapServiceKeyPrefixCount, null);

		return UddiRegister.publishBusinessServices(transport, authLogin, authPassword, services);

	}

	public static List<BusinessService> getRandomBusinessServices(String businessKey, String serviceKeyPrefix, String nodeName, Map<String, Integer> mapServiceKeyPrefixCount, List<String> servicesClassifications) {
		return getRandomBusinessServices(UBL_SERVICE_URL_PREFIX, businessKey, serviceKeyPrefix, nodeName, mapServiceKeyPrefixCount, servicesClassifications);
	}

	public static List<BusinessService> getRandomBusinessServices(String urlPrefix, String businessKey, String serviceKeyPrefix, String nodeName, Map<String, Integer> mapServiceKeyPrefixCount, List<String> servicesClassifications) {

		List<BusinessService> services = new LinkedList<BusinessService>();

		String[] protocols = new String[] { //
				"br.ufsc.gsigma.binding.converters.SoapServiceProtocolConverter", //
				"br.ufsc.gsigma.binding.converters.JsonServiceProtocolConverter" //
		};

		String serviceProtocolConverter = protocols[new Random().nextInt(protocols.length)];

		// Create Catalogue

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyRequestCatalogue", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Request Catalogue", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/requestCatalogue", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_requestcatalogue") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyRespondToRequest", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Respond To Request", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/respondToRequest", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_respondtorequest") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyProcessCatalogueRequest", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Process Catalogue Request", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/processCatalogueRequest", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_processcataloguerequest") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartySendRejection", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Send Rejection", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/sendRejection", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_sendrejection") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyReceiveRejection", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Receive Rejection", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/receiveRejection", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_receiverejection") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartySendAcceptanceResponse", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Send Acceptance Response", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/sendAcceptanceResponse", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_sendacceptanceresponse") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyPrepareCatalogueInformation", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Prepare Catalogue Information", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/prepareCatalogueInformation", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_preparecatalogueinformation") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyProduceCatalogue", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Produce Catalogue", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/produceCatalogue", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_producecatalogue") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyDistributeCatalogue", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Distribute Catalogue", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/distributeCatalogue", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_distributecatalogue") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyReceiveCatalogue", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Receive Catalogue", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/receiveCatalogue", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_receivecatalogue") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyReviewCatalogueContent", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Review Catalogue Content", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/reviewCatalogueContent", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_reviewcataloguecontent") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyAcknowledgeAcceptance", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Acknowledge Acceptance", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/acknowledgeAcceptance", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_acknowledgeacceptance") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyReceiveAcknowledgeAcceptance", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Receive Acknowledge Acceptance", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/receiveAcknowledgeAcceptance", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_receiveacknowledgeacceptance") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyAcceptCatalogue", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Accept Catalogue", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/acceptCatalogue", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_acceptcatalogue") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessReceiverPartyQueryCatalogueContent", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Receiver Party -> Query Catalogue Content", //
				urlPrefix + "/ubl/createcatalogueprocess/receiverParty/queryCatalogueContent", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_receiverparty_querycataloguecontent") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyDecideOnAction", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Decide On Action", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/decideOnAction", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_decideonaction") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyCancelTransaction", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Cancel Transaction", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/cancelTransaction", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_canceltransaction") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLCreateCatalogueProcessProviderPartyReviseContent", //
				"UBL Sourcing -> Catalogue Provision -> Create Catalogue Process -> Provider Party -> Revise Content", //
				urlPrefix + "/ubl/createcatalogueprocess/providerParty/reviseContent", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:sourcing_catalogueprovision_createcatalogueprocess_providerparty_revisecontent") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		// Ordering Process
		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessBuyerPartyPlaceOrder", //
				"UBL Payment -> Ordering Process -> Buyer Party -> Place Order", //
				urlPrefix + "/ubl/orderingprocess/buyerParty/placeOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_buyerparty_placeorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyReceiveOrder", //
				"UBL Payment -> Ordering Process -> Seller Party -> Receive Order", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/receiveOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_receiveorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyProcessOrder", //
				"UBL Payment -> Ordering Process -> Seller Party -> Process Order", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/processOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_processorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyAcceptOrder", //
				"UBL Payment -> Ordering Process -> Seller Party -> Accept Order", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/acceptOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_acceptorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyRejectOrder", //
				"UBL Payment -> Ordering Process -> Seller Party -> Reject Order", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/rejectOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_rejectorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyAddDetail", //
				"UBL Payment -> Ordering Process -> Seller Party -> Add Detail", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/addDetail", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_adddetail") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessBuyerPartyReceiveResponse", //
				"UBL Payment -> Ordering Process -> Buyer Party -> Receive Response", //
				urlPrefix + "/ubl/orderingprocess/buyerParty/receiveResponse", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_buyerparty_receiveresponse") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessBuyerPartyAcceptOrder", //
				"UBL Payment -> Ordering Process -> Buyer Party -> Accept Order", //
				urlPrefix + "/ubl/orderingprocess/buyerParty/acceptOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_buyerparty_acceptorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessBuyerPartyRejectOrder", //
				"UBL Payment -> Ordering Process -> Buyer Party -> Reject Order", //
				urlPrefix + "/ubl/orderingprocess/buyerParty/rejectOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_buyerparty_rejectorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessBuyerPartyChangeOrder", //
				"UBL Payment -> Ordering Process -> Buyer Party -> Change Order", //
				urlPrefix + "/ubl/orderingprocess/buyerParty/changeOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_buyerparty_changeorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessBuyerPartyCancelOrder", //
				"UBL Payment -> Ordering Process -> Buyer Party -> Cancel Order", //
				urlPrefix + "/ubl/orderingprocess/buyerParty/cancelOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_buyerparty_cancelorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyCancelOrder", //
				"UBL Payment -> Ordering Process -> Seller Party -> Cancel Order", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/cancelOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_cancelorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyChangeOrder", //
				"UBL Payment -> Ordering Process -> Seller Party -> Change Order", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/changeOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_changeorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLOrderingProcessSellerPartyProcessOrderChange", //
				"UBL Payment -> Ordering Process -> Seller Party -> Process Order Change", //
				urlPrefix + "/ubl/orderingprocess/sellerParty/processOrderChange", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:ordering_orderingprocess_sellerparty_processorderchange") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		// Payment Process
		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLPaymentProcessAccountingCustomerAuthorizePayment", //
				"UBL Payment -> Payment Process -> Accounting Customer -> Authorize Payment", //
				urlPrefix + "/ubl/paymentprocess/accountingCustomer/authorizePayment", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:payment_paymentprocess_accountingcustomer_authorizepayment") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLPaymentProcessAccountingCustomerNotifyOfPayment", //
				"UBL Payment -> Payment Process -> Accounting Customer -> Notify of Payment", //
				urlPrefix + "/ubl/paymentprocess/accountingCustomer/notifyOfPayment", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:payment_paymentprocess_accountingcustomer_notifyofpayment") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLPaymentProcessAccountingSupplierNotifyPayee", //
				"UBL Payment -> Payment Process -> Accounting Supplier -> Notify Payee", //
				urlPrefix + "/ubl/paymentprocess/accountingSupplier/notifyPayee", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:payment_paymentprocess_accountingsupplier_notifypayee") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLPaymentProcessAccountingSupplierReceiveAdvice", //
				"UBL Payment -> Payment Process -> Accounting Supplier -> Receive Advice", //
				urlPrefix + "/ubl/paymentprocess/accountingSupplier/receiveAdvice", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:payment_paymentprocess_accountingsupplier_receiveadvice") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLPaymentProcessPayeePartyReceiveAdvice", //
				"UBL Payment -> Payment Process -> Payee Party -> Receive Advice", //
				urlPrefix + "/ubl/paymentprocess/payeeParty/receiveAdvice", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:payment_paymentprocess_payeeparty_receiveadvice") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		// Fulfilment with Receipt Advice
		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItems", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Delivery Party -> Receive Order Items", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/deliveryParty/receiveOrderItems", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_deliveryparty_receiveorderitems") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItems", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Delivery Party -> Check Status of Items", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/deliveryParty/checkStatusOfItems", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_deliveryparty_checkstatusofitems") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceDeliveryPartyAdviseBuyerOfStatus", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Delivery Party -> Advise Buyer of Status", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/deliveryParty/adviseBuyerOfStatus", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_deliveryparty_advisebuyerofstatus") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Delivery Party -> Send Receipt Advice", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_deliveryparty_sendreceiptadvice") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceBuyerPartyDetermineAction", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Buyer Party -> Determine Action", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/buyerParty/determineAction", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_buyerparty_determineaction") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItems", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Buyer Party -> Accept Items", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/buyerParty/acceptItems", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_buyerparty_acceptitems") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustOrder", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Buyer Party -> Adjust Order", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_buyerparty_adjustorder") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceBuyerPartySendOrderCancellation", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Buyer Party -> Send Order Cancellation", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/buyerParty/sendOrderCancellation", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_buyerparty_sendordercancellation") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLFulfilmentWithReceiptAdviceSellerPartyAdjustSupplyStatus", //
				"UBL Fulfilment -> Fulfilment with Receipt Advice -> Seller Party -> Adjust Supply Status", //
				urlPrefix + "/ubl/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:fulfilment_fulfilmentwithreceiptadvice_sellerparty_adjustsupplystatus") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		// Billing with Credit Note
		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingCustomerAcceptCharges", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Customer -> Accept Charges", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingCustomer/acceptCharges", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingcustomer_acceptcharges") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingCustomerAcceptCredit", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Customer -> Accept Credit", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingCustomer/acceptCredit", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingcustomer_acceptcredit") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingCustomerReceiveCreditNote", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Customer -> Receive Credit Note", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingCustomer/receiveCreditNote", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingcustomer_receivecreditnote") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingCustomerReceiveInvoice", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Customer -> Receive Invoice", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingCustomer/receiveInvoice", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingcustomer_receiveinvoice") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingCustomerReconcileCharges", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Customer -> Reconcile Charges", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingCustomer/reconcileCharges", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingcustomer_reconcilecharges") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingCustomerSendAccountResponse", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Customer -> Send Account Response", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingCustomer/sendAccountResponse", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingcustomer_sendaccountresponse") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNote", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Supplier -> Rraise Credit Note", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingSupplier/raiseCreditNote", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingsupplier_raisecreditnote") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingSupplierRaiseInvoice", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Supplier -> Raise Invoice", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingSupplier/raiseInvoice", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingsupplier_raiseinvoice") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingSupplierReceiveAccountResponse", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Supplier -> Receive Account Response", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingSupplier/receiveAccountResponse", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingsupplier_receiveaccountresponse") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingSupplierReconcileCharges", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Supplier -> Reconcile Charges", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingSupplier/reconcileCharges", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingsupplier_reconcilecharges") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		services.add(UddiRegister.getBusinessServiceForService(businessKey, null, serviceKeyPrefix, //
				"UBLBillingWithCreditNoteAccountingSupplierValidateResponse", //
				"UBL Billing -> Traditional Billing -> Billing with Credit Note -> Accounting Supplier -> Validate Response", //
				urlPrefix + "/ubl/billingwithcreditnote/accountingSupplier/validateResponse", //
				"endpoint", serviceProtocolConverter, new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:ubl:services:billing_traditionalbilling_billingwithcreditnote_accountingsupplier_validateresponse") }, //
				buildRandomQos(), nodeName, mapServiceKeyPrefixCount));

		return services;
	}

	private static KeyedReference[] buildRandomQos() {

		Random random = new Random();

		int PERFORMANCE_RESPONSETIME_MIN = 150;
		int PERFORMANCE_RESPONSETIME_MAX = 550;

		double PERFORMANCE_RESPONSETIME = getRandomValue(random, PERFORMANCE_RESPONSETIME_MIN, PERFORMANCE_RESPONSETIME_MAX, 0);

		double AVAILABILITY_MIN = 0.9D;
		double AVAILABILITY_MAX = 0.9999D;
		double AVAILABILITY = getRandomValue(random, AVAILABILITY_MIN, AVAILABILITY_MAX, 4);

		int THROUGHPUT_MIN = 15;
		int THROUGHPUT_MAX = 100;
		double THROUGHPUT = getRandomValue(random, THROUGHPUT_MIN, THROUGHPUT_MAX, 0);

		double c1 = (PERFORMANCE_RESPONSETIME_MAX - PERFORMANCE_RESPONSETIME) / (PERFORMANCE_RESPONSETIME_MAX - PERFORMANCE_RESPONSETIME_MIN);
		double c2 = (AVAILABILITY - AVAILABILITY_MIN) / (AVAILABILITY_MAX - AVAILABILITY_MIN);
		double c3 = (THROUGHPUT - THROUGHPUT_MIN) / (THROUGHPUT_MAX - THROUGHPUT_MIN);

		int w1 = 1 + random.nextInt(3);
		int w2 = 1 + random.nextInt(3);
		int w3 = 1 + random.nextInt(3);

		double EXECUTION_COST = (c1 * w1 + c2 * w2 + c3 * w3) * (100 + random.nextInt(50));

		return new KeyedReference[] { //
				//

				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_PERFORMANCE_RESPONSETIME, "ResponseTime", PERFORMANCE_RESPONSETIME), //
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_AVAILABILITY_AVAILABILITY, "Availability", AVAILABILITY), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_PERFORMANCE_THROUGHPUT, "Throughput", THROUGHPUT), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_COST_EXECUTION_COST, "Execution Cost", EXECUTION_COST), // Money

				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_ACCURACY_GENERATEDERROR, "GeneratedError", 1 + random.nextInt(100)), // ErrorNumber
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_ACESSIBILITY_ACESSIBILITY, "Acessibility", getRandomPercentage(random)), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_CAPACITY_CAPACITY, "Capacity", 1 + random.nextInt(100)), // RequestNumber
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_EXCEPTIONHANDLING_FUNCTIONALITY, "Functionality", getRandomPercentage(random)), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_INTEGRITY_DATAINTEGRITY, "DataIntegrity", getRandomPercentage(random)), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_INTEGRITY_TRANSACTIONALINTEGRITY, "TransactionalIntegrity", getRandomPercentage(random)), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_INTEROPERABILITY_INTEROPERABILITY, "Interoperability", getRandomPercentage(random)), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_PERFORMANCE_LATENCY, "Latency", 1 + random.nextInt(100)), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_PERFORMANCE_EXECUTIONTIME, "ExecutionTime", 1 + random.nextInt(100)), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_PERFORMANCE_TRANSACTIONTIME, "TransactionTime", 1 + random.nextInt(100)), // Second

				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_RELIABILITY_MESSAGEWARRANTY, "MessageWarranty", 1 + random.nextInt(100)), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_RELIABILITY_MONTH, "Month", 1 + random.nextInt(100)), // FailureNumber
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_RELIABILITY_DAY, "Day", 1 + random.nextInt(100)), // FailureNumber
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_RELIABILITY_YEAR, "Year", 1 + random.nextInt(100)), // FailureNumber
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_ROBUSTNESS_ROBUSTNESS, "Robustness", getRandomPercentage(random)), // Percentage
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_SCALABILITY_LATENCY, "Latency", 1 + random.nextInt(100)), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_SCALABILITY_TRANSACTIONTIME, "TransactionTime", 1 + random.nextInt(100)), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_SCALABILITY_EXECUTIONTIME, "ExecutionTime", 1 + random.nextInt(100)), // ExecutionTime
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_SCALABILITY_THROUGHPUT, "Throughput", 1 + random.nextInt(100)), // Second
				UddiRegister.getKeyedReference(UddiKeys.UDDI_QOS_SCALABILITY_RESPONSETIME, "ResponseTime", 1 + random.nextInt(100)), // Second
		};

	}

	private static Double getRandomPercentage(Random random) {
		return getRandomValue(random, 0.1, 1.0, 2);
	}

	private static Double getRandomValue(final Random random, final double lowerBound, final double upperBound, final int decimalPlaces) {

		if (lowerBound < 0 || upperBound <= lowerBound || decimalPlaces < 0) {
			throw new IllegalArgumentException("Put error message here");
		}

		final double dbl = ((random == null ? new Random() : random).nextDouble() //
				* (upperBound - lowerBound)) + lowerBound;
		return Double.parseDouble(String.format(Locale.US, "%." + decimalPlaces + "f", dbl));

	}
}
