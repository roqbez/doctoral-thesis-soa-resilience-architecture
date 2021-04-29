
package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProcessOrderAsyncCallback }
     * 
     */
    public ProcessOrderAsyncCallback createProcessOrderAsyncCallback() {
        return new ProcessOrderAsyncCallback();
    }

    /**
     * Create an instance of {@link Alive }
     * 
     */
    public Alive createAlive() {
        return new Alive();
    }

    /**
     * Create an instance of {@link ProcessOrderAsyncCallbackResponse }
     * 
     */
    public ProcessOrderAsyncCallbackResponse createProcessOrderAsyncCallbackResponse() {
        return new ProcessOrderAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link ProcessOrderAsync }
     * 
     */
    public ProcessOrderAsync createProcessOrderAsync() {
        return new ProcessOrderAsync();
    }

    /**
     * Create an instance of {@link ProcessOrder }
     * 
     */
    public ProcessOrder createProcessOrder() {
        return new ProcessOrder();
    }

    /**
     * Create an instance of {@link ProcessOrderResponse }
     * 
     */
    public ProcessOrderResponse createProcessOrderResponse() {
        return new ProcessOrderResponse();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

}
