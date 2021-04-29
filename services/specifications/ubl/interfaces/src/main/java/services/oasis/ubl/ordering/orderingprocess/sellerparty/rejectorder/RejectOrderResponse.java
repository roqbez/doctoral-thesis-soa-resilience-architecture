
package services.oasis.ubl.ordering.orderingprocess.sellerparty.rejectorder;

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
 *         &lt;element name="outputOrderResponse" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2}OrderResponseType"/>
 *         &lt;element name="outputOrderResponseSimple" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderResponseSimple-2}OrderResponseSimpleType"/>
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
    "outputOrderResponse",
    "outputOrderResponseSimple"
})
@XmlRootElement(name = "rejectOrderResponse")
public class RejectOrderResponse {

    @XmlElement(required = true, nillable = true)
    protected OrderResponseType outputOrderResponse;
    @XmlElement(required = true, nillable = true)
    protected OrderResponseSimpleType outputOrderResponseSimple;

    /**
     * Gets the value of the outputOrderResponse property.
     * 
     * @return
     *     possible object is
     *     {@link OrderResponseType }
     *     
     */
    public OrderResponseType getOutputOrderResponse() {
        return outputOrderResponse;
    }

    /**
     * Sets the value of the outputOrderResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderResponseType }
     *     
     */
    public void setOutputOrderResponse(OrderResponseType value) {
        this.outputOrderResponse = value;
    }

    /**
     * Gets the value of the outputOrderResponseSimple property.
     * 
     * @return
     *     possible object is
     *     {@link OrderResponseSimpleType }
     *     
     */
    public OrderResponseSimpleType getOutputOrderResponseSimple() {
        return outputOrderResponseSimple;
    }

    /**
     * Sets the value of the outputOrderResponseSimple property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderResponseSimpleType }
     *     
     */
    public void setOutputOrderResponseSimple(OrderResponseSimpleType value) {
        this.outputOrderResponseSimple = value;
    }

}
