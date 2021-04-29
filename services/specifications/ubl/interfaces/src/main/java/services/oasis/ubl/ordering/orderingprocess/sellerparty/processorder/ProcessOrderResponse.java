
package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.order_2.OrderType;


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
 *         &lt;element name="outputDocument" type="{urn:oasis:names:specification:ubl:schema:xsd:Order-2}OrderType"/>
 *         &lt;element name="outputDecision" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "outputDocument",
    "outputDecision"
})
@XmlRootElement(name = "processOrderResponse")
public class ProcessOrderResponse {

    @XmlElement(required = true, nillable = true)
    protected OrderType outputDocument;
    @XmlElement(required = true, nillable = true)
    protected String outputDecision;

    /**
     * Gets the value of the outputDocument property.
     * 
     * @return
     *     possible object is
     *     {@link OrderType }
     *     
     */
    public OrderType getOutputDocument() {
        return outputDocument;
    }

    /**
     * Sets the value of the outputDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderType }
     *     
     */
    public void setOutputDocument(OrderType value) {
        this.outputDocument = value;
    }

    /**
     * Gets the value of the outputDecision property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputDecision() {
        return outputDecision;
    }

    /**
     * Sets the value of the outputDecision property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputDecision(String value) {
        this.outputDecision = value;
    }

}
