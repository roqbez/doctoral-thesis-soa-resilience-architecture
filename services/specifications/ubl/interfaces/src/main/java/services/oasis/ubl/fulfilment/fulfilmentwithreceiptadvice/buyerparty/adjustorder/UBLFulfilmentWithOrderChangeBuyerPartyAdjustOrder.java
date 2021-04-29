package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder;

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
 * Tue Aug 28 20:48:52 BRT 2018
 * Generated source version: 2.2.9
 * 
 */
 
@WebService(targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", name = "UBL_FulfilmentWithOrderChange_BuyerParty_AdjustOrder")
@XmlSeeAlso({ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.qualifieddatatypes_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ObjectFactory.class, un.unece.uncefact.codelist.specification._54217._2001.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonextensioncomponents_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.orderchange_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.order_2.ObjectFactory.class, br.ufsc.gsigma.processcontext.ObjectFactory.class, un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.ObjectFactory.class})
public interface UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder extends ServiceAccessContract {

    @WebMethod(action = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder/adjustOrder")
    @RequestWrapper(localName = "adjustOrder", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.AdjustOrder")
    @ResponseWrapper(localName = "adjustOrderResponse", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.AdjustOrderResponse")
    @WebResult(name = "output", targetNamespace = "")
    public oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType adjustOrder(
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType input
    );

    @WebMethod(action = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder/adjustOrderAsync")
    @Oneway
    @RequestWrapper(localName = "adjustOrderAsync", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.AdjustOrderAsync")
    public void adjustOrderAsync(
        @WebParam(name = "processContext", targetNamespace = "")
        br.ufsc.gsigma.processcontext.ProcessContext processContext,
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType input
    );

    @WebMethod(action = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder/alive")
    @RequestWrapper(localName = "alive", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.Alive")
    @ResponseWrapper(localName = "aliveResponse", targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", className = "services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder.AliveResponse")
    @WebResult(name = "output", targetNamespace = "")
    public java.lang.Boolean alive();
}
