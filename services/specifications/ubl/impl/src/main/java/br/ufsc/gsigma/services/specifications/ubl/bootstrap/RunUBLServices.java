package br.ufsc.gsigma.services.specifications.ubl.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.util.AppContext;
import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent;
import br.ufsc.gsigma.infrastructure.util.messaging.BootstrapCompleteEvent.ComponentName;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.cxf.MultipleServicesJAXBDataBindingInitializer;
import br.ufsc.gsigma.infrastructure.ws.cxf.ServiceAvailabilityFeature;
import br.ufsc.gsigma.services.specifications.ubl.cxf.JsonTransformInInterceptor;
import br.ufsc.gsigma.services.specifications.ubl.cxf.RequestBindingContextFeature;
import br.ufsc.gsigma.services.specifications.ubl.cxf.ServiceAccessInInterceptor;
import br.ufsc.gsigma.services.specifications.ubl.impl.UBLServicesAdminServiceImpl;
import br.ufsc.gsigma.services.specifications.ubl.interfaces.UBLServicesAdminService;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcharges.UBLBillingWithCreditNoteAccountingCustomerAcceptCharges;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcharges.UBLBillingWithCreditNoteAccountingCustomerAcceptChargesImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcredit.UBLBillingWithCreditNoteAccountingCustomerAcceptCredit;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcredit.UBLBillingWithCreditNoteAccountingCustomerAcceptCreditImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.receivecreditnote.UBLBillingWithCreditNoteAccountingCustomerReceiveCreditNote;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.receivecreditnote.UBLBillingWithCreditNoteAccountingCustomerReceiveCreditNoteImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.receiveinvoice.UBLBillingWithCreditNoteAccountingCustomerReceiveInvoice;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.receiveinvoice.UBLBillingWithCreditNoteAccountingCustomerReceiveInvoiceImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.reconcilecharges.UBLBillingWithCreditNoteAccountingCustomerReconcileCharges;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.reconcilecharges.UBLBillingWithCreditNoteAccountingCustomerReconcileChargesImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.sendaccountresponse.UBLBillingWithCreditNoteAccountingCustomerSendAccountResponse;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.sendaccountresponse.UBLBillingWithCreditNoteAccountingCustomerSendAccountResponseImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.raisecreditnote.UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNote;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.raisecreditnote.UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNoteImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.raiseinvoice.UBLBillingWithCreditNoteAccountingSupplierRaiseInvoice;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.raiseinvoice.UBLBillingWithCreditNoteAccountingSupplierRaiseInvoiceImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.receiveaccountresponse.UBLBillingWithCreditNoteAccountingSupplierReceiveAccountResponse;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.receiveaccountresponse.UBLBillingWithCreditNoteAccountingSupplierReceiveAccountResponseImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.reconcilecharges.UBLBillingWithCreditNoteAccountingSupplierReconcileCharges;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.reconcilecharges.UBLBillingWithCreditNoteAccountingSupplierReconcileChargesImpl;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.validateresponse.UBLBillingWithCreditNoteAccountingSupplierValidateResponse;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.validateresponse.UBLBillingWithCreditNoteAccountingSupplierValidateResponseImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.acceptitems.UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItems;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.acceptitems.UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItemsImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.determineaction.UBLFulfilmentWithReceiptAdviceBuyerPartyDetermineAction;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.determineaction.UBLFulfilmentWithReceiptAdviceBuyerPartyDetermineActionImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.sendordercancellation.UBLFulfilmentWithReceiptAdviceBuyerPartySendOrderCancellation;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.sendordercancellation.UBLFulfilmentWithReceiptAdviceBuyerPartySendOrderCancellationImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.advisebuyerofstatus.UBLFulfilmentWithReceiptAdviceDeliveryPartyAdviseBuyerOfStatus;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.advisebuyerofstatus.UBLFulfilmentWithReceiptAdviceDeliveryPartyAdviseBuyerOfStatusImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.checkstatusofitems.UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItems;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.checkstatusofitems.UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItemsImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.receiveorderitems.UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItems;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.receiveorderitems.UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItemsImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.sendreceiptadvice.UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.sendreceiptadvice.UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceImpl;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus;
import services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusImpl;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.acceptorder.UBLOrderingProcessBuyerPartyAcceptOrder;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.acceptorder.UBLOrderingProcessBuyerPartyAcceptOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.cancelorder.UBLOrderingProcessBuyerPartyCancelOrder;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.cancelorder.UBLOrderingProcessBuyerPartyCancelOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.changeorder.UBLOrderingProcessBuyerPartyChangeOrder;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.changeorder.UBLOrderingProcessBuyerPartyChangeOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder.UBLOrderingProcessBuyerPartyPlaceOrder;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder.UBLOrderingProcessBuyerPartyPlaceOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.receiveresponse.UBLOrderingProcessBuyerPartyReceiveResponse;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.receiveresponse.UBLOrderingProcessBuyerPartyReceiveResponseImpl;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.rejectorder.UBLOrderingProcessBuyerPartyRejectOrder;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.rejectorder.UBLOrderingProcessBuyerPartyRejectOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.acceptorder.UBLOrderingProcessSellerPartyAcceptOrder;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.acceptorder.UBLOrderingProcessSellerPartyAcceptOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.UBLOrderingProcessSellerPartyAddDetail;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.UBLOrderingProcessSellerPartyAddDetailImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.cancelorder.UBLOrderingProcessSellerPartyCancelOrder;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.cancelorder.UBLOrderingProcessSellerPartyCancelOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.changeorder.UBLOrderingProcessSellerPartyChangeOrder;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.changeorder.UBLOrderingProcessSellerPartyChangeOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.UBLOrderingProcessSellerPartyProcessOrder;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.UBLOrderingProcessSellerPartyProcessOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange.UBLOrderingProcessSellerPartyProcessOrderChange;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange.UBLOrderingProcessSellerPartyProcessOrderChangeImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.receiveorder.UBLOrderingProcessSellerPartyReceiveOrder;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.receiveorder.UBLOrderingProcessSellerPartyReceiveOrderImpl;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.rejectorder.UBLOrderingProcessSellerPartyRejectOrder;
import services.oasis.ubl.ordering.orderingprocess.sellerparty.rejectorder.UBLOrderingProcessSellerPartyRejectOrderImpl;
import services.oasis.ubl.payment.paymentprocess.accountingcustomer.authorizepayment.UBLPaymentProcessAccountingCustomerAuthorizePayment;
import services.oasis.ubl.payment.paymentprocess.accountingcustomer.authorizepayment.UBLPaymentProcessAccountingCustomerAuthorizePaymentImpl;
import services.oasis.ubl.payment.paymentprocess.accountingcustomer.notifyofpayment.UBLPaymentProcessAccountingCustomerNotifyOfPayment;
import services.oasis.ubl.payment.paymentprocess.accountingcustomer.notifyofpayment.UBLPaymentProcessAccountingCustomerNotifyOfPaymentImpl;
import services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee.UBLPaymentProcessAccountingSupplierNotifyPayee;
import services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee.UBLPaymentProcessAccountingSupplierNotifyPayeeImpl;
import services.oasis.ubl.payment.paymentprocess.accountingsupplier.receiveadvice.UBLPaymentProcessAccountingSupplierReceiveAdvice;
import services.oasis.ubl.payment.paymentprocess.accountingsupplier.receiveadvice.UBLPaymentProcessAccountingSupplierReceiveAdviceImpl;
import services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice.UBLPaymentProcessPayeePartyReceiveAdvice;
import services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice.UBLPaymentProcessPayeePartyReceiveAdviceImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.canceltransaction.UBLCreateCatalogueProcessProviderPartyCancelTransaction;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.canceltransaction.UBLCreateCatalogueProcessProviderPartyCancelTransactionImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.decideonaction.UBLCreateCatalogueProcessProviderPartyDecideOnAction;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.decideonaction.UBLCreateCatalogueProcessProviderPartyDecideOnActionImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.distributecatalogue.UBLCreateCatalogueProcessProviderPartyDistributeCatalogue;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.distributecatalogue.UBLCreateCatalogueProcessProviderPartyDistributeCatalogueImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.preparecatalogueinformation.UBLCreateCatalogueProcessProviderPartyPrepareCatalogueInformation;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.preparecatalogueinformation.UBLCreateCatalogueProcessProviderPartyPrepareCatalogueInformationImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.processcataloguerequest.UBLCreateCatalogueProcessProviderPartyProcessCatalogueRequest;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.processcataloguerequest.UBLCreateCatalogueProcessProviderPartyProcessCatalogueRequestImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.producecatalogue.UBLCreateCatalogueProcessProviderPartyProduceCatalogue;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.producecatalogue.UBLCreateCatalogueProcessProviderPartyProduceCatalogueImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.receiveacknowledgeacceptance.UBLCreateCatalogueProcessProviderPartyReceiveAcknowledgeAcceptance;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.receiveacknowledgeacceptance.UBLCreateCatalogueProcessProviderPartyReceiveAcknowledgeAcceptanceImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.respondtorequest.UBLCreateCatalogueProcessProviderPartyRespondToRequest;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.respondtorequest.UBLCreateCatalogueProcessProviderPartyRespondToRequestImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.revisecontent.UBLCreateCatalogueProcessProviderPartyReviseContent;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.revisecontent.UBLCreateCatalogueProcessProviderPartyReviseContentImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendacceptanceresponse.UBLCreateCatalogueProcessProviderPartySendAcceptanceResponse;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendacceptanceresponse.UBLCreateCatalogueProcessProviderPartySendAcceptanceResponseImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.UBLCreateCatalogueProcessProviderPartySendRejection;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.UBLCreateCatalogueProcessProviderPartySendRejectionImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.acceptcatalogue.UBLCreateCatalogueProcessReceiverPartyAcceptCatalogue;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.acceptcatalogue.UBLCreateCatalogueProcessReceiverPartyAcceptCatalogueImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.acknowledgeacceptance.UBLCreateCatalogueProcessReceiverPartyAcknowledgeAcceptance;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.acknowledgeacceptance.UBLCreateCatalogueProcessReceiverPartyAcknowledgeAcceptanceImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.querycataloguecontent.UBLCreateCatalogueProcessReceiverPartyQueryCatalogueContent;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.querycataloguecontent.UBLCreateCatalogueProcessReceiverPartyQueryCatalogueContentImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.receivecatalogue.UBLCreateCatalogueProcessReceiverPartyReceiveCatalogue;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.receivecatalogue.UBLCreateCatalogueProcessReceiverPartyReceiveCatalogueImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.receiverejection.UBLCreateCatalogueProcessReceiverPartyReceiveRejection;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.receiverejection.UBLCreateCatalogueProcessReceiverPartyReceiveRejectionImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.requestcatalogue.UBLCreateCatalogueProcessReceiverPartyRequestCatalogue;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.requestcatalogue.UBLCreateCatalogueProcessReceiverPartyRequestCatalogueImpl;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.reviewcataloguecontent.UBLCreateCatalogueProcessReceiverPartyReviewCatalogueContent;
import services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.reviewcataloguecontent.UBLCreateCatalogueProcessReceiverPartyReviewCatalogueContentImpl;

public abstract class RunUBLServices {

	private final static Logger logger = LoggerFactory.getLogger(RunUBLServices.class);

	private static String hostPort;

	public static String getServiceUrlBase() {
		return hostPort;
	}

	public static void main(String[] args) throws Exception {

		try {

			long s = System.currentTimeMillis();

			String localHost = args.length > 0 ? args[0] : ServicesAddresses.ALL_HOSTS;
			String localPort = args.length > 1 ? args[1] : String.valueOf(ServicesAddresses.UBL_SERVICES_PORT);

			String publishHost = args.length > 2 ? args[2] : String.valueOf(ServicesAddresses.UBL_SERVICES_HOSTNAME);
			String publishPort = args.length > 3 ? args[3] : String.valueOf(ServicesAddresses.UBL_SERVICES_PORT);

			List<String> providedServicePaths = new ArrayList<String>();

			if (args.length > 4) {
				for (int i = 4; i < args.length; i++) {
					providedServicePaths.add(args[i]);
				}
			}

			hostPort = "http://" + publishHost + ":" + publishPort;

			final boolean interactive = args.length > 4 && "interactive".equalsIgnoreCase(args[4]);

			configureLog4jAppName(publishHost);

			logger.info("Using host " + publishHost + " and port " + publishPort);

			logger.info("Bootstraping Spring Framework");

			if (DockerContainerUtil.isRunningInContainer()) {
				System.setProperty("jms.clientid", DockerContainerUtil.getContainerClientId());
			} else {
				System.setProperty("jms.clientid", publishHost + ":" + publishPort);
			}

			AppContext.getApplicationContext("/applicationContext.xml");

			ServiceAvailabilityFeature serviceAvailabilityFeature = new ServiceAvailabilityFeature();
			RequestBindingContextFeature requestBindingContextFeature = new RequestBindingContextFeature();

			String localHostPort = "http://" + localHost + ":" + localPort;

			CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();

			org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(new InetSocketAddress(localHost, Integer.valueOf(localPort)));

			ServletContextHandler sch = new ServletContextHandler();
			sch.setContextPath("/");
			sch.addServlet(new ServletHolder(cxfServlet), "/*");
			// sch.addServlet(new ServletHolder(new ServiceRouterServlet(cxfServlet)), "/*");

			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] { sch });

			server.setHandler(contexts);

			server.start();

			localHostPort = "";

			// Admin

			UBLServicesAdminServiceImpl ublServicesAdminService = new UBLServicesAdminServiceImpl(serviceAvailabilityFeature);

			CxfUtil.createService(UBLServicesAdminService.class, localHostPort + "/services/UBLServicesAdminService", ublServicesAdminService);

			Collection<AbstractFeature> features = new LinkedList<AbstractFeature>();
			features.add(serviceAvailabilityFeature);
			features.add(requestBindingContextFeature);

			// Compound databinding (for fast loading)

			MultipleServicesJAXBDataBindingInitializer dataBindingInit = new MultipleServicesJAXBDataBindingInitializer();

			// Create Catalogue Process
			createService(UBLCreateCatalogueProcessReceiverPartyRequestCatalogue.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/requestCatalogue", //
					new UBLCreateCatalogueProcessReceiverPartyRequestCatalogueImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyRespondToRequest.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/respondToRequest", //
					new UBLCreateCatalogueProcessProviderPartyRespondToRequestImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyProcessCatalogueRequest.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/processCatalogueRequest", //
					new UBLCreateCatalogueProcessProviderPartyProcessCatalogueRequestImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartySendRejection.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/sendRejection", //
					new UBLCreateCatalogueProcessProviderPartySendRejectionImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessReceiverPartyReceiveRejection.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/receiveRejection", //
					new UBLCreateCatalogueProcessReceiverPartyReceiveRejectionImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartySendAcceptanceResponse.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/sendAcceptanceResponse", //
					new UBLCreateCatalogueProcessProviderPartySendAcceptanceResponseImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyPrepareCatalogueInformation.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/prepareCatalogueInformation", //
					new UBLCreateCatalogueProcessProviderPartyPrepareCatalogueInformationImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyProduceCatalogue.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/produceCatalogue", //
					new UBLCreateCatalogueProcessProviderPartyProduceCatalogueImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyDistributeCatalogue.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/distributeCatalogue", //
					new UBLCreateCatalogueProcessProviderPartyDistributeCatalogueImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessReceiverPartyReceiveCatalogue.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/receiveCatalogue", //
					new UBLCreateCatalogueProcessReceiverPartyReceiveCatalogueImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessReceiverPartyReviewCatalogueContent.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/reviewCatalogueContent", //
					new UBLCreateCatalogueProcessReceiverPartyReviewCatalogueContentImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessReceiverPartyAcknowledgeAcceptance.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/acknowledgeAcceptance", //
					new UBLCreateCatalogueProcessReceiverPartyAcknowledgeAcceptanceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyReceiveAcknowledgeAcceptance.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/receiveAcknowledgeAcceptance", //
					new UBLCreateCatalogueProcessProviderPartyReceiveAcknowledgeAcceptanceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessReceiverPartyAcceptCatalogue.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/acceptCatalogue", //
					new UBLCreateCatalogueProcessReceiverPartyAcceptCatalogueImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessReceiverPartyQueryCatalogueContent.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/receiverParty/queryCatalogueContent", //
					new UBLCreateCatalogueProcessReceiverPartyQueryCatalogueContentImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyDecideOnAction.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/decideOnAction", //
					new UBLCreateCatalogueProcessProviderPartyDecideOnActionImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyCancelTransaction.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/cancelTransaction", //
					new UBLCreateCatalogueProcessProviderPartyCancelTransactionImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLCreateCatalogueProcessProviderPartyReviseContent.class, localHostPort, providedServicePaths, "/services/ubl/createcatalogueprocess/providerParty/reviseContent", //
					new UBLCreateCatalogueProcessProviderPartyReviseContentImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			// Ordering Process

			createService(UBLOrderingProcessBuyerPartyPlaceOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/buyerParty/placeOrder", //
					new UBLOrderingProcessBuyerPartyPlaceOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyReceiveOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/receiveOrder", //
					new UBLOrderingProcessSellerPartyReceiveOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyProcessOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/processOrder", //
					new UBLOrderingProcessSellerPartyProcessOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyAcceptOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/acceptOrder", //
					new UBLOrderingProcessSellerPartyAcceptOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyRejectOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/rejectOrder", //
					new UBLOrderingProcessSellerPartyRejectOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyAddDetail.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/addDetail", //
					new UBLOrderingProcessSellerPartyAddDetailImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessBuyerPartyReceiveResponse.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/buyerParty/receiveResponse", //
					new UBLOrderingProcessBuyerPartyReceiveResponseImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessBuyerPartyAcceptOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/buyerParty/acceptOrder", //
					new UBLOrderingProcessBuyerPartyAcceptOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessBuyerPartyRejectOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/buyerParty/rejectOrder", //
					new UBLOrderingProcessBuyerPartyRejectOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessBuyerPartyChangeOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/buyerParty/changeOrder", //
					new UBLOrderingProcessBuyerPartyChangeOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessBuyerPartyCancelOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/buyerParty/cancelOrder", //
					new UBLOrderingProcessBuyerPartyCancelOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyCancelOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/cancelOrder", //
					new UBLOrderingProcessSellerPartyCancelOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyChangeOrder.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/changeOrder", //
					new UBLOrderingProcessSellerPartyChangeOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLOrderingProcessSellerPartyProcessOrderChange.class, localHostPort, providedServicePaths, "/services/ubl/orderingprocess/sellerParty/processOrderChange", //
					new UBLOrderingProcessSellerPartyProcessOrderChangeImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			// Payment Process
			createService(UBLPaymentProcessAccountingCustomerAuthorizePayment.class, localHostPort, providedServicePaths, "/services/ubl/paymentprocess/accountingCustomer/authorizePayment", //
					new UBLPaymentProcessAccountingCustomerAuthorizePaymentImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLPaymentProcessAccountingCustomerNotifyOfPayment.class, localHostPort, providedServicePaths, "/services/ubl/paymentprocess/accountingCustomer/notifyOfPayment", //
					new UBLPaymentProcessAccountingCustomerNotifyOfPaymentImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLPaymentProcessAccountingSupplierNotifyPayee.class, localHostPort, providedServicePaths, "/services/ubl/paymentprocess/accountingSupplier/notifyPayee", //
					new UBLPaymentProcessAccountingSupplierNotifyPayeeImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLPaymentProcessAccountingSupplierReceiveAdvice.class, localHostPort, providedServicePaths, "/services/ubl/paymentprocess/accountingSupplier/receiveAdvice", //
					new UBLPaymentProcessAccountingSupplierReceiveAdviceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLPaymentProcessPayeePartyReceiveAdvice.class, localHostPort, providedServicePaths, "/services/ubl/paymentprocess/payeeParty/receiveAdvice", //
					new UBLPaymentProcessPayeePartyReceiveAdviceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			// Fulfilment with Receipt Advice
			createService(UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItems.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/buyerParty/acceptItems", //
					new UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItemsImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", //
					new UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceBuyerPartyDetermineAction.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/buyerParty/determineAction", //
					new UBLFulfilmentWithReceiptAdviceBuyerPartyDetermineActionImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceBuyerPartySendOrderCancellation.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/buyerParty/sendOrderCancellation", //
					new UBLFulfilmentWithReceiptAdviceBuyerPartySendOrderCancellationImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceDeliveryPartyAdviseBuyerOfStatus.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/adviseBuyerOfStatus", //
					new UBLFulfilmentWithReceiptAdviceDeliveryPartyAdviseBuyerOfStatusImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItems.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/checkStatusOfItems", //
					new UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItemsImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItems.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/receiveOrderItems", //
					new UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItemsImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice", //
					new UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus.class, localHostPort, providedServicePaths, "/services/ubl/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", //
					new UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			// Billing with Credit Note

			createService(UBLBillingWithCreditNoteAccountingCustomerAcceptCharges.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingCustomer/acceptCharges", //
					new UBLBillingWithCreditNoteAccountingCustomerAcceptChargesImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingCustomerAcceptCredit.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingCustomer/acceptCredit", //
					new UBLBillingWithCreditNoteAccountingCustomerAcceptCreditImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingCustomerReceiveCreditNote.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingCustomer/receiveCreditNote", //
					new UBLBillingWithCreditNoteAccountingCustomerReceiveCreditNoteImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingCustomerReceiveInvoice.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingCustomer/receiveInvoice", //
					new UBLBillingWithCreditNoteAccountingCustomerReceiveInvoiceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingCustomerReconcileCharges.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingCustomer/reconcileCharges", //
					new UBLBillingWithCreditNoteAccountingCustomerReconcileChargesImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingCustomerSendAccountResponse.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingCustomer/sendAccountResponse", //
					new UBLBillingWithCreditNoteAccountingCustomerSendAccountResponseImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNote.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingSupplier/raiseCreditNote", //
					new UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNoteImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingSupplierRaiseInvoice.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingSupplier/raiseInvoice", //
					new UBLBillingWithCreditNoteAccountingSupplierRaiseInvoiceImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingSupplierReceiveAccountResponse.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingSupplier/receiveAccountResponse", //
					new UBLBillingWithCreditNoteAccountingSupplierReceiveAccountResponseImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingSupplierReconcileCharges.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingSupplier/reconcileCharges", //
					new UBLBillingWithCreditNoteAccountingSupplierReconcileChargesImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			createService(UBLBillingWithCreditNoteAccountingSupplierValidateResponse.class, localHostPort, providedServicePaths, "/services/ubl/billingwithcreditnote/accountingSupplier/validateResponse", //
					new UBLBillingWithCreditNoteAccountingSupplierValidateResponseImpl(interactive), features, dataBindingInit.createDataBinding(), ublServicesAdminService);

			dataBindingInit.initializeAll();

			long d = System.currentTimeMillis() - s;

			EventSender eventSender = EventSender.getInstance();
			eventSender.sendEvent(new BootstrapCompleteEvent(ComponentName.UBL_SERVICES, eventSender.getSenderId()));

			logger.info("Bootstrap of UBL services complete (" + d + " ms)");

		} catch (

		Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	private static boolean containsServicePath(List<String> providedServicePaths, String servicePath) {

		for (String providedServicePath : providedServicePaths) {
			if (servicePath.contains(providedServicePath)) {
				return true;
			}
		}
		return false;
	}

	private static Server createService(Class<?> serviceInterface, String hostPort, List<String> providedServicePaths, String servicePath, Object serviceImplementor, Collection<AbstractFeature> features, DataBinding dataBinding, UBLServicesAdminServiceImpl ublServicesAdminService) {

		if (providedServicePaths == null || providedServicePaths.isEmpty() || containsServicePath(providedServicePaths, servicePath)) {
			try {
				ublServicesAdminService.registerService(servicePath, serviceImplementor);

				List<Interceptor<? extends Message>> inInterceptors = new ArrayList<Interceptor<? extends Message>>();
				inInterceptors.add(JsonTransformInInterceptor.INSTANCE);
				inInterceptors.add(ServiceAccessInInterceptor.INSTANCE);

				// String serviceUrl = hostPort + servicePath.replaceAll("/services/", "/soap/");
				String serviceUrl = hostPort + servicePath;

				Server result = CxfUtil.createService(null, serviceInterface, serviceUrl, serviceImplementor, features, dataBinding, inInterceptors);
				try {
					ublServicesAdminService.setServiceAvailable(servicePath);
				} catch (RuntimeException e2) {
					logger.error(e2.getMessage(), e2);
				}
				return result;
			} catch (RuntimeException e) {
				logger.error(e.getMessage(), e);
				try {
					ublServicesAdminService.setServiceUnavailable(servicePath);
				} catch (RuntimeException e2) {
					logger.error(e2.getMessage(), e2);
				}
				throw e;
			}
		} else {
			return null;
		}
	}

	private static void configureLog4jAppName(String host) throws IOException {
		int idx = host.indexOf('.');

		if (idx > 0) {
			Properties log4jProps = new Properties();
			InputStream in = RunUBLServices.class.getResourceAsStream("/log4j.properties");
			if (in != null) {
				try (InputStream is = in) {
					log4jProps.load(is);
				} catch (IOException e) {
					throw e;
				}

				String appName = log4jProps.getProperty("log4j.appender.SYSLOG.appName");

				if (appName != null) {

					String sp = "services";

					int idx2 = host.indexOf(sp);

					if (idx2 > 0) {

						String n = host.substring(idx2 + sp.length(), idx);

						if (NumberUtils.isDigits(n)) {
							appName = appName + n;
							log4jProps.setProperty("log4j.appender.SYSLOG.appName", appName);
							PropertyConfigurator.configure(log4jProps);
						}
					}
				}
			}
		}
	}
}
