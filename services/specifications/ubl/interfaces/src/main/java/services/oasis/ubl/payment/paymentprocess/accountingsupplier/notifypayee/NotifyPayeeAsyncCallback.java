
package services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import br.ufsc.gsigma.processcontext.ProcessContext;
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
 *         &lt;element name="processContext" type="{http://gsigma.ufsc.br/processContext}ProcessContext"/>
 *         &lt;element name="output" type="{urn:oasis:names:specification:ubl:schema:xsd:RemittanceAdvice-2}RemittanceAdviceType"/>
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
    "output"
})
@XmlRootElement(name = "notifyPayeeAsyncCallback")
public class NotifyPayeeAsyncCallback {

    @XmlElement(required = true)
    protected ProcessContext processContext;
    @XmlElement(required = true, nillable = true)
    protected RemittanceAdviceType output;

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
     *     {@link RemittanceAdviceType }
     *     
     */
    public RemittanceAdviceType getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemittanceAdviceType }
     *     
     */
    public void setOutput(RemittanceAdviceType value) {
        this.output = value;
    }

}
