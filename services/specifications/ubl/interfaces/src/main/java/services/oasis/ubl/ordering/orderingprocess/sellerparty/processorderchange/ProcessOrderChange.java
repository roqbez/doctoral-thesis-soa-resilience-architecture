
package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType;


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
 *         &lt;element name="input" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderChange-2}OrderChangeType"/>
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
@XmlRootElement(name = "processOrderChange")
public class ProcessOrderChange {

    @XmlElement(required = true, nillable = true)
    protected OrderChangeType input;

    /**
     * Gets the value of the input property.
     * 
     * @return
     *     possible object is
     *     {@link OrderChangeType }
     *     
     */
    public OrderChangeType getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderChangeType }
     *     
     */
    public void setInput(OrderChangeType value) {
        this.input = value;
    }

}
