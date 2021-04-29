
package services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PlaceOrderAsync }
     * 
     */
    public PlaceOrderAsync createPlaceOrderAsync() {
        return new PlaceOrderAsync();
    }

    /**
     * Create an instance of {@link PlaceOrderAsyncCallback }
     * 
     */
    public PlaceOrderAsyncCallback createPlaceOrderAsyncCallback() {
        return new PlaceOrderAsyncCallback();
    }

    /**
     * Create an instance of {@link PlaceOrderAsyncCallbackResponse }
     * 
     */
    public PlaceOrderAsyncCallbackResponse createPlaceOrderAsyncCallbackResponse() {
        return new PlaceOrderAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link PlaceOrderResponse }
     * 
     */
    public PlaceOrderResponse createPlaceOrderResponse() {
        return new PlaceOrderResponse();
    }

    /**
     * Create an instance of {@link Alive }
     * 
     */
    public Alive createAlive() {
        return new Alive();
    }

    /**
     * Create an instance of {@link PlaceOrder }
     * 
     */
    public PlaceOrder createPlaceOrder() {
        return new PlaceOrder();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

}
