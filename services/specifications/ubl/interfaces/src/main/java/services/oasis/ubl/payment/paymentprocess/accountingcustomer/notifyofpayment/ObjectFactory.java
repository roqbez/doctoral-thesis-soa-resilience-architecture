
package services.oasis.ubl.payment.paymentprocess.accountingcustomer.notifyofpayment;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.oasis.ubl.payment.paymentprocess.accountingcustomer.notifyofpayment package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.oasis.ubl.payment.paymentprocess.accountingcustomer.notifyofpayment
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NotifyOfPaymentResponse }
     * 
     */
    public NotifyOfPaymentResponse createNotifyOfPaymentResponse() {
        return new NotifyOfPaymentResponse();
    }

    /**
     * Create an instance of {@link NotifyOfPayment }
     * 
     */
    public NotifyOfPayment createNotifyOfPayment() {
        return new NotifyOfPayment();
    }

    /**
     * Create an instance of {@link NotifyOfPaymentAsyncCallbackResponse }
     * 
     */
    public NotifyOfPaymentAsyncCallbackResponse createNotifyOfPaymentAsyncCallbackResponse() {
        return new NotifyOfPaymentAsyncCallbackResponse();
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
     * Create an instance of {@link NotifyOfPaymentAsyncCallback }
     * 
     */
    public NotifyOfPaymentAsyncCallback createNotifyOfPaymentAsyncCallback() {
        return new NotifyOfPaymentAsyncCallback();
    }

    /**
     * Create an instance of {@link NotifyOfPaymentAsync }
     * 
     */
    public NotifyOfPaymentAsync createNotifyOfPaymentAsync() {
        return new NotifyOfPaymentAsync();
    }

}
