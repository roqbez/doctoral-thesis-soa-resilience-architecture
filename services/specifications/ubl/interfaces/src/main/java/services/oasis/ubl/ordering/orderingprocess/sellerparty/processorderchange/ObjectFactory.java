
package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProcessOrderChangeAsyncCallback }
     * 
     */
    public ProcessOrderChangeAsyncCallback createProcessOrderChangeAsyncCallback() {
        return new ProcessOrderChangeAsyncCallback();
    }

    /**
     * Create an instance of {@link ProcessOrderChangeAsyncCallbackResponse }
     * 
     */
    public ProcessOrderChangeAsyncCallbackResponse createProcessOrderChangeAsyncCallbackResponse() {
        return new ProcessOrderChangeAsyncCallbackResponse();
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
     * Create an instance of {@link ProcessOrderChangeResponse }
     * 
     */
    public ProcessOrderChangeResponse createProcessOrderChangeResponse() {
        return new ProcessOrderChangeResponse();
    }

    /**
     * Create an instance of {@link ProcessOrderChangeAsync }
     * 
     */
    public ProcessOrderChangeAsync createProcessOrderChangeAsync() {
        return new ProcessOrderChangeAsync();
    }

    /**
     * Create an instance of {@link ProcessOrderChange }
     * 
     */
    public ProcessOrderChange createProcessOrderChange() {
        return new ProcessOrderChange();
    }

}
