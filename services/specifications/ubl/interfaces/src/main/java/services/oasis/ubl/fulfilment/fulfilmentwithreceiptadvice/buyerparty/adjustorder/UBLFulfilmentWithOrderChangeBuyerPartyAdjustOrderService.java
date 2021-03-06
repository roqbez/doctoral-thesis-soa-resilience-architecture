
/*
 * 
 */

package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.adjustorder;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.9
 * Tue Aug 28 20:48:52 BRT 2018
 * Generated source version: 2.2.9
 * 
 */


@WebServiceClient(name = "UBL_FulfilmentWithOrderChange_BuyerParty_AdjustOrderService", 
                  wsdlLocation = "file:UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustOrder.wsdl",
                  targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder") 
public class UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", "UBL_FulfilmentWithOrderChange_BuyerParty_AdjustOrderService");
    public final static QName UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderServicePort = new QName("http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/buyerParty/adjustOrder", "UBL_FulfilmentWithOrderChange_BuyerParty_AdjustOrderServicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustOrder.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustOrder.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }
    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder
     */
    @WebEndpoint(name = "UBL_FulfilmentWithOrderChange_BuyerParty_AdjustOrderServicePort")
    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder getUBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderServicePort() {
        return super.getPort(UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderServicePort, UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder
     */
    @WebEndpoint(name = "UBL_FulfilmentWithOrderChange_BuyerParty_AdjustOrderServicePort")
    public UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder getUBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderServicePort(WebServiceFeature... features) {
        return super.getPort(UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrderServicePort, UBLFulfilmentWithOrderChangeBuyerPartyAdjustOrder.class, features);
    }

}
