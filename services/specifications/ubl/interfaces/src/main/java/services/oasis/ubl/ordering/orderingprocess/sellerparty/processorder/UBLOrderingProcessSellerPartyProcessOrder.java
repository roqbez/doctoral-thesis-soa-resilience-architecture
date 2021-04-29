package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder;

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
 * Sat Oct 30 21:03:30 BRST 2010
 * Generated source version: 2.2.9
 * 
 */
 
@WebService(targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder", name = "UBL_OrderingProcess_SellerParty_ProcessOrder")
@XmlSeeAlso({ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonextensioncomponents_2.ObjectFactory.class, br.ufsc.gsigma.processcontext.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.order_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ObjectFactory.class, un.unece.uncefact.codelist.specification._54217._2001.ObjectFactory.class, un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.qualifieddatatypes_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.ObjectFactory.class})
public interface UBLOrderingProcessSellerPartyProcessOrder extends ServiceAccessContract {

    @Oneway
    @RequestWrapper(localName = "processOrderAsync", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.ProcessOrderAsync")
    @WebMethod(action = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder/processOrderAsync")
    public void processOrderAsync(
        @WebParam(name = "processContext", targetNamespace = "")
        br.ufsc.gsigma.processcontext.ProcessContext processContext,
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType input
    );

    @WebResult(name = "output", targetNamespace = "")
    @RequestWrapper(localName = "alive", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.Alive")
    @WebMethod(action = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder/alive")
    @ResponseWrapper(localName = "aliveResponse", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.AliveResponse")
    public java.lang.Boolean alive();

    @RequestWrapper(localName = "processOrder", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.ProcessOrder")
    @WebMethod(action = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder/processOrder")
    @ResponseWrapper(localName = "processOrderResponse", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/processOrder", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder.ProcessOrderResponse")
    public void processOrder(
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType input,
        @WebParam(mode = WebParam.Mode.OUT, name = "outputDocument", targetNamespace = "")
        javax.xml.ws.Holder<oasis.names.specification.ubl.schema.xsd.order_2.OrderType> outputDocument,
        @WebParam(mode = WebParam.Mode.OUT, name = "outputDecision", targetNamespace = "")
        javax.xml.ws.Holder<java.lang.String> outputDecision
    );
}
