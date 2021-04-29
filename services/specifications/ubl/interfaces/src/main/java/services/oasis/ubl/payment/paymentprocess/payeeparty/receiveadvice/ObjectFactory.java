
package services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReceiveAdvice }
     * 
     */
    public ReceiveAdvice createReceiveAdvice() {
        return new ReceiveAdvice();
    }

    /**
     * Create an instance of {@link ReceiveAdviceAsyncCallback }
     * 
     */
    public ReceiveAdviceAsyncCallback createReceiveAdviceAsyncCallback() {
        return new ReceiveAdviceAsyncCallback();
    }

    /**
     * Create an instance of {@link ReceiveAdviceAsync }
     * 
     */
    public ReceiveAdviceAsync createReceiveAdviceAsync() {
        return new ReceiveAdviceAsync();
    }

    /**
     * Create an instance of {@link ReceiveAdviceResponse }
     * 
     */
    public ReceiveAdviceResponse createReceiveAdviceResponse() {
        return new ReceiveAdviceResponse();
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
     * Create an instance of {@link ReceiveAdviceAsyncCallbackResponse }
     * 
     */
    public ReceiveAdviceAsyncCallbackResponse createReceiveAdviceAsyncCallbackResponse() {
        return new ReceiveAdviceAsyncCallbackResponse();
    }

}
