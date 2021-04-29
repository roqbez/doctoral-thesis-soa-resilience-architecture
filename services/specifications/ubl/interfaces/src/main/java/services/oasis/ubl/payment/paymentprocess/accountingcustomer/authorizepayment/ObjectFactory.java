
package services.oasis.ubl.payment.paymentprocess.accountingcustomer.authorizepayment;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.payment.paymentprocess.accountingcustomer.authorizepayment package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.payment.paymentprocess.accountingcustomer.authorizepayment
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
     * Create an instance of {@link AuthorizePayment }
     * 
     */
    public AuthorizePayment createAuthorizePayment() {
        return new AuthorizePayment();
    }

    /**
     * Create an instance of {@link AuthorizePaymentAsyncCallbackResponse }
     * 
     */
    public AuthorizePaymentAsyncCallbackResponse createAuthorizePaymentAsyncCallbackResponse() {
        return new AuthorizePaymentAsyncCallbackResponse();
    }

    /**
     * Create an instance of {@link AliveResponse }
     * 
     */
    public AliveResponse createAliveResponse() {
        return new AliveResponse();
    }

    /**
     * Create an instance of {@link AuthorizePaymentAsync }
     * 
     */
    public AuthorizePaymentAsync createAuthorizePaymentAsync() {
        return new AuthorizePaymentAsync();
    }

    /**
     * Create an instance of {@link AuthorizePaymentAsyncCallback }
     * 
     */
    public AuthorizePaymentAsyncCallback createAuthorizePaymentAsyncCallback() {
        return new AuthorizePaymentAsyncCallback();
    }

    /**
     * Create an instance of {@link AuthorizePaymentResponse }
     * 
     */
    public AuthorizePaymentResponse createAuthorizePaymentResponse() {
        return new AuthorizePaymentResponse();
    }

}
