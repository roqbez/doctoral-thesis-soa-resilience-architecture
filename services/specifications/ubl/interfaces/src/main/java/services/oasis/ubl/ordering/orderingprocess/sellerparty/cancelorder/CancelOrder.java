
package services.oasis.ubl.ordering.orderingprocess.sellerparty.cancelorder;

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
 *         &lt;element name="input" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderCancellation-2}OrderCancellationType"/>
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
    "input"
})
@XmlRootElement(name = "cancelOrder")
public class CancelOrder {

    @XmlElement(required = true, nillable = true)
    protected OrderCancellationType input;

    /**
     * Gets the value of the input property.
     * 
     * @return
     *     possible object is
     *     {@link OrderCancellationType }
     *     
     */
    public OrderCancellationType getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderCancellationType }
     *     
     */
    public void setInput(OrderCancellationType value) {
        this.input = value;
    }

}
