
package services.oasis.ubl.ordering.orderingprocess.buyerparty.cancelorder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.ordercancellation_2.OrderCancellationType;


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
 *         &lt;element name="output" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderCancellation-2}OrderCancellationType"/>
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
@XmlRootElement(name = "cancelOrderResponse")
public class CancelOrderResponse {

    @XmlElement(required = true, nillable = true)
    protected OrderCancellationType output;

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link OrderCancellationType }
     *     
     */
    public OrderCancellationType getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderCancellationType }
     *     
     */
    public void setOutput(OrderCancellationType value) {
        this.output = value;
    }

}
