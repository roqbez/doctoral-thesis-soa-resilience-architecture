
package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import br.ufsc.gsigma.processcontext.ProcessContext;
import oasis.names.specification.ubl.schema.xsd.order_2.OrderType;
import oasis.names.specification.ubl.schema.xsd.ordercancellation_2.OrderCancellationType;
import oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType;
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
 *         &lt;element name="processContext" type="{http://gsigma.ufsc.br/processContext}ProcessContext"/>
 *         &lt;element name="inputReceiptAdvice" type="{urn:oasis:names:specification:ubl:schema:xsd:ReceiptAdvice-2}ReceiptAdviceType"/>
 *         &lt;element name="inputOrder" type="{urn:oasis:names:specification:ubl:schema:xsd:Order-2}OrderType"/>
 *         &lt;element name="inputOrderChange" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderChange-2}OrderChangeType"/>
 *         &lt;element name="inputOrderCancellation" type="{urn:oasis:names:specification:ubl:schema:xsd:OrderCancellation-2}OrderCancellationType"/>
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
    "inputReceiptAdvice",
    "inputOrder",
    "inputOrderChange",
    "inputOrderCancellation"
})
@XmlRootElement(name = "adjustSupplyStatusAsync")
public class AdjustSupplyStatusAsync {

    @XmlElement(required = true)
    protected ProcessContext processContext;
    @XmlElement(required = true, nillable = true)
    protected ReceiptAdviceType inputReceiptAdvice;
    @XmlElement(required = true, nillable = true)
    protected OrderType inputOrder;
    @XmlElement(required = true, nillable = true)
    protected OrderChangeType inputOrderChange;
    @XmlElement(required = true, nillable = true)
    protected OrderCancellationType inputOrderCancellation;

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
     * Gets the value of the inputReceiptAdvice property.
     * 
     * @return
     *     possible object is
     *     {@link ReceiptAdviceType }
     *     
     */
    public ReceiptAdviceType getInputReceiptAdvice() {
        return inputReceiptAdvice;
    }

    /**
     * Sets the value of the inputReceiptAdvice property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReceiptAdviceType }
     *     
     */
    public void setInputReceiptAdvice(ReceiptAdviceType value) {
        this.inputReceiptAdvice = value;
    }

    /**
     * Gets the value of the inputOrder property.
     * 
     * @return
     *     possible object is
     *     {@link OrderType }
     *     
     */
    public OrderType getInputOrder() {
        return inputOrder;
    }

    /**
     * Sets the value of the inputOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderType }
     *     
     */
    public void setInputOrder(OrderType value) {
        this.inputOrder = value;
    }

    /**
     * Gets the value of the inputOrderChange property.
     * 
     * @return
     *     possible object is
     *     {@link OrderChangeType }
     *     
     */
    public OrderChangeType getInputOrderChange() {
        return inputOrderChange;
    }

    /**
     * Sets the value of the inputOrderChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderChangeType }
     *     
     */
    public void setInputOrderChange(OrderChangeType value) {
        this.inputOrderChange = value;
    }

    /**
     * Gets the value of the inputOrderCancellation property.
     * 
     * @return
     *     possible object is
     *     {@link OrderCancellationType }
     *     
     */
    public OrderCancellationType getInputOrderCancellation() {
        return inputOrderCancellation;
    }

    /**
     * Sets the value of the inputOrderCancellation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderCancellationType }
     *     
     */
    public void setInputOrderCancellation(OrderCancellationType value) {
        this.inputOrderCancellation = value;
    }

}
