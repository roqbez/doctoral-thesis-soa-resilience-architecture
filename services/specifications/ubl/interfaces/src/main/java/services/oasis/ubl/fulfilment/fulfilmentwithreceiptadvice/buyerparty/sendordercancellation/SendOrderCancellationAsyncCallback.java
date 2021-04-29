
package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.sendordercancellation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import br.ufsc.gsigma.processcontext.ProcessContext;
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
 *         &lt;element name="processContext" type="{http://gsigma.ufsc.br/processContext}ProcessContext"/>
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
    "processContext",
    "outputOrderCancellation"
})
@XmlRootElement(name = "sendOrderCancellationAsyncCallback")
public class SendOrderCancellationAsyncCallback {

    @XmlElement(required = true)
    protected ProcessContext processContext;
    @XmlElement(required = true, nillable = true)
    protected OrderCancellationType outputOrderCancellation;

    /**
     * Gets the value of the processContext property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessContext }
     *     
     */
    public ProcessContext getProcessContext() {
        return processContext;
    }

    /**
     * Sets the value of the processContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessContext }
     *     
     */
    public void setProcessContext(ProcessContext value) {
        this.processContext = value;
    }

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link OrderCancellationType }
     *     
     */
    public OrderCancellationType getOutputOrderCancellation() {
        return outputOrderCancellation;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderCancellationType }
     *     
     */
    public void setOutputOrderCancellation(OrderCancellationType value) {
        this.outputOrderCancellation = value;
    }

}
