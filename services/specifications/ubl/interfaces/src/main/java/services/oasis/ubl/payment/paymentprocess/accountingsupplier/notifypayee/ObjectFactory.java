
package services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee
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
     * Create an instance of {@link NotifyPayeeAsyncCallbackResponse }
     * 
     */
    public NotifyPayeeAsyncCallbackResponse createNotifyPayeeAsyncCallbackResponse() {
        return new NotifyPayeeAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link NotifyPayeeAsync }
     * 
     */
    public NotifyPayeeAsync createNotifyPayeeAsync() {
        return new NotifyPayeeAsync();
    }

    /**
     * Create an instance of {@link NotifyPayeeResponse }
     * 
     */
    public NotifyPayeeResponse createNotifyPayeeResponse() {
        return new NotifyPayeeResponse();
    }

    /**
     * Create an instance of {@link NotifyPayee }
     * 
     */
    public NotifyPayee createNotifyPayee() {
        return new NotifyPayee();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

    /**
     * Create an instance of {@link NotifyPayeeAsyncCallback }
     * 
     */
    public NotifyPayeeAsyncCallback createNotifyPayeeAsyncCallback() {
        return new NotifyPayeeAsyncCallback();
    }

}
