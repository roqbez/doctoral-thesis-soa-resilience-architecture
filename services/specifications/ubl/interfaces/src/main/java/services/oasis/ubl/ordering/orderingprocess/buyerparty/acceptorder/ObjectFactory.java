
package services.oasis.ubl.ordering.orderingprocess.buyerparty.acceptorder;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.ordering.orderingprocess.buyerparty.acceptorder package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.ordering.orderingprocess.buyerparty.acceptorder
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
     * Create an instance of {@link AcceptOrderAsync }
     * 
     */
    public AcceptOrderAsync createAcceptOrderAsync() {
        return new AcceptOrderAsync();
    }

    /**
     * Create an instance of {@link AcceptOrderAsyncCallback }
     * 
     */
    public AcceptOrderAsyncCallback createAcceptOrderAsyncCallback() {
        return new AcceptOrderAsyncCallback();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

    /**
     * Create an instance of {@link AcceptOrder }
     * 
     */
    public AcceptOrder createAcceptOrder() {
        return new AcceptOrder();
    }

    /**
     * Create an instance of {@link AcceptOrderAsyncCallbackResponse }
     * 
     */
    public AcceptOrderAsyncCallbackResponse createAcceptOrderAsyncCallbackResponse() {
        return new AcceptOrderAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link AcceptOrderResponse }
     * 
     */
    public AcceptOrderResponse createAcceptOrderResponse() {
        return new AcceptOrderResponse();
    }

}
