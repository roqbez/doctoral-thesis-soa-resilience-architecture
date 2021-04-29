
package services.oasis.ubl.ordering.orderingprocess.buyerparty.acceptorder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.orderresponse_2.OrderResponseType;
import oasis.names.specification.ubl.schema.xsd.orderresponsesimple_2.OrderResponseSimpleType;


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
 *         &lt;element name="inputOrderResponseSimple" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderResponseSimple-2}OrderResponseSimpleType"/>
 *         &lt;element name="inputOrderResponse" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2}OrderResponseType"/>
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
    "inputOrderResponseSimple",
    "inputOrderResponse"
})
@XmlRootElement(name = "acceptOrder")
public class AcceptOrder {

    @XmlElement(required = true, nillable = true)
    protected OrderResponseSimpleType inputOrderResponseSimple;
    @XmlElement(required = true, nillable = true)
    protected OrderResponseType inputOrderResponse;

    /**
     * Gets the value of the inputOrderResponseSimple property.
     * 
     * @return
     *     possible object is
     *     {@link OrderResponseSimpleType }
     *     
     */
    public OrderResponseSimpleType getInputOrderResponseSimple() {
        return inputOrderResponseSimple;
    }

    /**
     * Sets the value of the inputOrderResponseSimple property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderResponseSimpleType }
     *     
     */
    public void setInputOrderResponseSimple(OrderResponseSimpleType value) {
        this.inputOrderResponseSimple = value;
    }

    /**
     * Gets the value of the inputOrderResponse property.
     * 
     * @return
     *     possible object is
     *     {@link OrderResponseType }
     *     
     */
    public OrderResponseType getInputOrderResponse() {
        return inputOrderResponse;
    }

    /**
     * Sets the value of the inputOrderResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderResponseType }
     *     
     */
    public void setInputOrderResponse(OrderResponseType value) {
        this.inputOrderResponse = value;
    }

}
