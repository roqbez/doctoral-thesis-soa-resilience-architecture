
/*
 * 
 */

package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.sendreceiptadvice;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.9
 * Tue Aug 28 20:49:55 BRT 2018
 * Generated source version: 2.2.9
 * 
 */


@WebServiceClient(name = "UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdviceService", 
                  wsdlLocation = "file:UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdvice.wsdl",
                  targetNamespace = "http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice") 
public class UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice", "UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdviceService");
    public final static QName UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceServicePort = new QName("http://ubl.oasis.services/fulfilment/fulfilmentwithreceiptadvice/deliveryParty/sendReceiptAdvice", "UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdviceServicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdvice.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdvice.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }
    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice
     */
    @WebEndpoint(name = "UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdviceServicePort")
    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice getUBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceServicePort() {
        return super.getPort(UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceServicePort, UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice
     */
    @WebEndpoint(name = "UBL_FulfilmentWithReceiptAdvice_DeliveryParty_SendReceiptAdviceServicePort")
    public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice getUBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceServicePort(WebServiceFeature... features) {
        return super.getPort(UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceServicePort, UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice.class, features);
    }

}
