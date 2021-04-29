package services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail;

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
 * Sat Oct 30 21:02:47 BRST 2010
 * Generated source version: 2.2.9
 * 
 */
 
@WebService(targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail", name = "UBL_OrderingProcess_SellerParty_AddDetail")
@XmlSeeAlso({ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.orderresponse_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonextensioncomponents_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.order_2.ObjectFactory.class, br.ufsc.gsigma.processcontext.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.orderresponsesimple_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ObjectFactory.class, un.unece.uncefact.codelist.specification._54217._2001.ObjectFactory.class, un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.qualifieddatatypes_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.ObjectFactory.class})
public interface UBLOrderingProcessSellerPartyAddDetail extends ServiceAccessContract {

    @Oneway
    @RequestWrapper(localName = "addDetailAsync", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.AddDetailAsync")
    @WebMethod(action = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail/addDetailAsync")
    public void addDetailAsync(
        @WebParam(name = "processContext", targetNamespace = "")
        br.ufsc.gsigma.processcontext.ProcessContext processContext,
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType input
    );

    @WebResult(name = "output", targetNamespace = "")
    @RequestWrapper(localName = "alive", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.Alive")
    @WebMethod(action = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail/alive")
    @ResponseWrapper(localName = "aliveResponse", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.AliveResponse")
    public java.lang.Boolean alive();

    @RequestWrapper(localName = "addDetail", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.AddDetail")
    @WebMethod(action = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail/addDetail")
    @ResponseWrapper(localName = "addDetailResponse", targetNamespace = "http://ubl.oasis.services/ordering/orderingprocess/sellerParty/addDetail", className = "services.oasis.ubl.ordering.orderingprocess.sellerparty.adddetail.AddDetailResponse")
    public void addDetail(
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.order_2.OrderType input,
        @WebParam(mode = WebParam.Mode.OUT, name = "outputOrderResponse", targetNamespace = "")
        javax.xml.ws.Holder<oasis.names.specification.ubl.schema.xsd.orderresponse_2.OrderResponseType> outputOrderResponse,
        @WebParam(mode = WebParam.Mode.OUT, name = "outputOrderResponseSimple", targetNamespace = "")
        javax.xml.ws.Holder<oasis.names.specification.ubl.schema.xsd.orderresponsesimple_2.OrderResponseSimpleType> outputOrderResponseSimple
    );
}
