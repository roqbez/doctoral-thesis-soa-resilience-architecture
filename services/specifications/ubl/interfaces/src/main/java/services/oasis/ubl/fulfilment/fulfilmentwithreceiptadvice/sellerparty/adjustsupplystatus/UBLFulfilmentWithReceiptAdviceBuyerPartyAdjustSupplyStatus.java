package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessContract;

/**
 * This class was generated by Apache CXF 2.2.9
 * Tue Aug 28 20:50:17 BRT 2018
 * Generated source version: 2.2.9
 * 
 */
 
@WebService(targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", name = "UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustSupplyStatus")
@XmlSeeAlso({ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.receiptadvice_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.qualifieddatatypes_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ObjectFactory.class, un.unece.uncefact.codelist.specification._54217._2001.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.ordercancellation_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonextensioncomponents_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.order_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.orderchange_2.ObjectFactory.class, br.ufsc.gsigma.processcontext.ObjectFactory.class, un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.ObjectFactory.class})
public interface UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus extends ServiceAccessContract {

    @WebMethod(action = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus/alive")
    @RequestWrapper(localName = "alive", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.Alive")
    @ResponseWrapper(localName = "aliveResponse", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.AliveResponse")
    @WebResult(name = "output", targetNamespace = "")
    public java.lang.Boolean alive();

    @WebMethod(action = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus/adjustSupplyStatus")
    @RequestWrapper(localName = "adjustSupplyStatus", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.AdjustSupplyStatus")
    @ResponseWrapper(localName = "adjustSupplyStatusResponse", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.AdjustSupplyStatusResponse")
    @WebResult(name = "output", targetNamespace = "")
    public java.lang.String adjustSupplyStatus(
        @WebParam(name = "inputReceiptAdvice", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.receiptadvice_2.ReceiptAdviceType inputReceiptAdvice,
        @WebParam(name = "inputOrder", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType inputOrder,
        @WebParam(name = "inputOrderChange", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType inputOrderChange,
        @WebParam(name = "inputOrderCancellation", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.ordercancellation_2.OrderCancellationType inputOrderCancellation
    );

    @WebMethod(action = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus/adjustSupplyStatusAsync")
    @Oneway
    @RequestWrapper(localName = "adjustSupplyStatusAsync", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus.AdjustSupplyStatusAsync")
    public void adjustSupplyStatusAsync(
        @WebParam(name = "processContext", targetNamespace = "")
        br.ufsc.gsigma.processcontext.ProcessContext processContext,
        @WebParam(name = "inputReceiptAdvice", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.receiptadvice_2.ReceiptAdviceType inputReceiptAdvice,
        @WebParam(name = "inputOrder", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType inputOrder,
        @WebParam(name = "inputOrderChange", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType inputOrderChange,
        @WebParam(name = "inputOrderCancellation", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.ordercancellation_2.OrderCancellationType inputOrderCancellation
    );
}