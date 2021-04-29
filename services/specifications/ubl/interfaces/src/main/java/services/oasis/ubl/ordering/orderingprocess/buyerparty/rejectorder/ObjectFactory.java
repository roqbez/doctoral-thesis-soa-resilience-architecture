
package services.oasis.ubl.ordering.orderingprocess.buyerparty.rejectorder;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.ordering.orderingprocess.buyerparty.rejectorder package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.ordering.orderingprocess.buyerparty.rejectorder
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Alive }
     * 
     */
    public Alive createAlive() {
        return new Alive();
    }

    /**
     * Create an instance of {@link RejectOrderAsync }
     * 
     */
    public RejectOrderAsync createRejectOrderAsync() {
        return new RejectOrderAsync();
    }

    /**
     * Create an instance of {@link RejectOrderAsyncCallback }
     * 
     */
    public RejectOrderAsyncCallback createRejectOrderAsyncCallback() {
        return new RejectOrderAsyncCallback();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

    /**
     * Create an instance of {@link RejectOrder }
     * 
     */
    public RejectOrder createRejectOrder() {
        return new RejectOrder();
    }

    /**
     * Create an instance of {@link RejectOrderAsyncCallbackResponse }
     * 
     */
    public RejectOrderAsyncCallbackResponse createRejectOrderAsyncCallbackResponse() {
        return new RejectOrderAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link RejectOrderResponse }
     * 
     */
    public RejectOrderResponse createRejectOrderResponse() {
        return new RejectOrderResponse();
    }

}
