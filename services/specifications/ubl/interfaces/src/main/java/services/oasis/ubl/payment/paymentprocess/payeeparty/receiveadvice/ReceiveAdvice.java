
package services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.remittanceadvice_2.RemittanceAdviceType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="inputFromAccountingCustomer" type="{urn:oasis:names:specification:ubl:schema:xsd:RemittanceAdvice-2}RemittanceAdviceType"/>
 *         &lt;element name="inputFromAccountingSupplier" type="{urn:oasis:names:specification:ubl:schema:xsd:RemittanceAdvice-2}RemittanceAdviceType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "inputFromAccountingCustomer",
    "inputFromAccountingSupplier"
})
@XmlRootElement(name = "receiveAdvice")
public class ReceiveAdvice {

    @XmlElement(required = true, nillable = true)
    protected RemittanceAdviceType inputFromAccountingCustomer;
    @XmlElement(required = true, nillable = true)
    protected RemittanceAdviceType inputFromAccountingSupplier;

    /**
     * Gets the value of the inputFromAccountingCustomer property.
     * 
     * @return
     *     possible object is
     *     {@link RemittanceAdviceType }
     *     
     */
    public RemittanceAdviceType getInputFromAccountingCustomer() {
        return inputFromAccountingCustomer;
    }

    /**
     * Sets the value of the inputFromAccountingCustomer property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemittanceAdviceType }
     *     
     */
    public void setInputFromAccountingCustomer(RemittanceAdviceType value) {
        this.inputFromAccountingCustomer = value;
    }

    /**
     * Gets the value of the inputFromAccountingSupplier property.
     * 
     * @return
     *     possible object is
     *     {@link RemittanceAdviceType }
     *     
     */
    public RemittanceAdviceType getInputFromAccountingSupplier() {
        return inputFromAccountingSupplier;
    }

    /**
     * Sets the value of the inputFromAccountingSupplier property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemittanceAdviceType }
     *     
     */
    public void setInputFromAccountingSupplier(RemittanceAdviceType value) {
        this.inputFromAccountingSupplier = value;
    }

}
