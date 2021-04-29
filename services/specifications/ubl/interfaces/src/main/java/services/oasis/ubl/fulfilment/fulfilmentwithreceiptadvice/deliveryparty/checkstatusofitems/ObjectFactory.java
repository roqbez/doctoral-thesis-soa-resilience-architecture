
package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.checkstatusofitems;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.checkstatusofitems package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.checkstatusofitems
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CheckStatusOfItemsAsync }
     * 
     */
    public CheckStatusOfItemsAsync createCheckStatusOfItemsAsync() {
        return new CheckStatusOfItemsAsync();
    }

    /**
     * Create an instance of {@link CheckStatusOfItemsAsyncCallback }
     * 
     */
    public CheckStatusOfItemsAsyncCallback createCheckStatusOfItemsAsyncCallback() {
        return new CheckStatusOfItemsAsyncCallback();
    }

    /**
     * Create an instance of {@link Alive }
     * 
     */
    public Alive createAlive() {
        return new Alive();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

    /**
     * Create an instance of {@link CheckStatusOfItemsResponse }
     * 
     */
    public CheckStatusOfItemsResponse createCheckStatusOfItemsResponse() {
        return new CheckStatusOfItemsResponse();
    }

    /**
     * Create an instance of {@link CheckStatusOfItems }
     * 
     */
    public CheckStatusOfItems createCheckStatusOfItems() {
        return new CheckStatusOfItems();
    }

    /**
     * Create an instance of {@link CheckStatusOfItemsAsyncCallbackResponse }
     * 
     */
    public CheckStatusOfItemsAsyncCallbackResponse createCheckStatusOfItemsAsyncCallbackResponse() {
        return new CheckStatusOfItemsAsyncCallbackResponse();
    }

}
