
/*
 * 
 */

package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.9
 * Tue Aug 28 20:50:17 BRT 2018
 * Generated source version: 2.2.9
 * 
 */


@WebServiceClient(name = "UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustSupplyStatusService", 
                  wsdlLocation = "file:UBL_FulfilmentWithReceiptAdvice_SellerParty_AdjustSupplyStatus.wsdl",
                  targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus") 
public class UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", "UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustSupplyStatusService");
    public final static QName UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusServicePort = new QName("http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/sellerParty/adjustSupplyStatus", "UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustSupplyStatusServicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:UBL_FulfilmentWithReceiptAdvice_SellerParty_AdjustSupplyStatus.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:UBL_FulfilmentWithReceiptAdvice_SellerParty_AdjustSupplyStatus.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }
    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus
     */
    @WebEndpoint(name = "UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustSupplyStatusServicePort")
    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus getUBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusServicePort() {
        return super.getPort(UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusServicePort, UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus
     */
    @WebEndpoint(name = "UBL_FulfilmentWithReceiptAdvice_BuyerParty_AdjustSupplyStatusServicePort")
    public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus getUBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusServicePort(WebServiceFeature... features) {
        return super.getPort(UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusServicePort, UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus.class, features);
    }

}
