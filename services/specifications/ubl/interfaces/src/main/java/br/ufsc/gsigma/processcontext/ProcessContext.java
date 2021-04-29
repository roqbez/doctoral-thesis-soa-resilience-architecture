
package br.ufsc.gsigma.processcontext;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessContext complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessContext">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="callbackEndpoint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="callbackSOAPAction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessContext", propOrder = {
    "processId",
    "callbackEndpoint",
    "callbackSOAPAction"
})
public class ProcessContext {

    @XmlElement(required = true)
    protected String processId;
    @XmlElement(required = true)
    protected String callbackEndpoint;
    @XmlElement(required = true)
    protected String callbackSOAPAction;

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessId(String value) {
        this.processId = value;
    }

    /**
     * Gets the value of the callbackEndpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallbackEndpoint() {
        return callbackEndpoint;
    }

    /**
     * Sets the value of the callbackEndpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallbackEndpoint(String value) {
        this.callbackEndpoint = value;
    }

    /**
     * Gets the value of the callbackSOAPAction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallbackSOAPAction() {
        return callbackSOAPAction;
    }

    /**
     * Sets the value of the callbackSOAPAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallbackSOAPAction(String value) {
        this.callbackSOAPAction = value;
    }

}
