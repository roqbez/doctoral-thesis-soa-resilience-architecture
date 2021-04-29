
package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.sendordercancellation;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.sendordercancellation package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.sendordercancellation
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
     * Create an instance of {@link SendOrderCancellation }
     * 
     */
    public SendOrderCancellation createSendOrderCancellation() {
        return new SendOrderCancellation();
    }

    /**
     * Create an instance of {@link SendOrderCancellationAsyncCallbackResponse }
     * 
     */
    public SendOrderCancellationAsyncCallbackResponse createSendOrderCancellationAsyncCallbackResponse() {
        return new SendOrderCancellationAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link SendOrderCancellationAsyncCallback }
     * 
     */
    public SendOrderCancellationAsyncCallback createSendOrderCancellationAsyncCallback() {
        return new SendOrderCancellationAsyncCallback();
    }

    /**
     * Create an instance of {@link SendOrderCancellationAsync }
     * 
     */
    public SendOrderCancellationAsync createSendOrderCancellationAsync() {
        return new SendOrderCancellationAsync();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

    /**
     * Create an instance of {@link SendOrderCancellationResponse }
     * 
     */
    public SendOrderCancellationResponse createSendOrderCancellationResponse() {
        return new SendOrderCancellationResponse();
    }

}
