
package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.sendreceiptadvice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.receiptadvice_2.ReceiptAdviceType;


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
 *         &lt;element name="output" type="{urn:oasis:names:specification:ubl:schema:xsd:ReceiptAdvice-2}ReceiptAdviceType"/>
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
    "output"
})
@XmlRootElement(name = "sendReceiptAdviceResponse")
public class SendReceiptAdviceResponse {

    @XmlElement(required = true, nillable = true)
    protected ReceiptAdviceType output;

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link ReceiptAdviceType }
     *     
     */
    public ReceiptAdviceType getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReceiptAdviceType }
     *     
     */
    public void setOutput(ReceiptAdviceType value) {
        this.output = value;
    }

}
