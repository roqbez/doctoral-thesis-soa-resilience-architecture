
package br.ufsc.gsigma.servicediscovery.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceDiscoveryInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceDiscoveryInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="candidateServices" type="{http://gsigma.ufsc.br/serviceDiscovery}ArrayOfCandidateService"/>
 *         &lt;element name="serviceClassification" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="qoSConstraints" type="{http://gsigma.ufsc.br/serviceDiscovery}ArrayOfQoSConstraint"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceDiscoveryInfo", propOrder = {
    "candidateServices",
    "serviceClassification",
    "qoSConstraints"
})
public class ServiceDiscoveryInfo {

    @XmlElement(required = true)
    protected ArrayOfCandidateService candidateServices;
    protected String serviceClassification;
    @XmlElement(required = true)
    protected ArrayOfQoSConstraint qoSConstraints;

    /**
     * Gets the value of the candidateServices property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfCandidateService }
     *     
     */
    public ArrayOfCandidateService getCandidateServices() {
        return candidateServices;
    }

    /**
     * Sets the value of the candidateServices property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfCandidateService }
     *     
     */
    public void setCandidateServices(ArrayOfCandidateService value) {
        this.candidateServices = value;
    }

    /**
     * Gets the value of the serviceClassification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceClassification() {
        return serviceClassification;
    }

    /**
     * Sets the value of the serviceClassification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceClassification(String value) {
        this.serviceClassification = value;
    }

    /**
     * Gets the value of the qoSConstraints property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfQoSConstraint }
     *     
     */
    public ArrayOfQoSConstraint getQoSConstraints() {
        return qoSConstraints;
    }

    /**
     * Sets the value of the qoSConstraints property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfQoSConstraint }
     *     
     */
    public void setQoSConstraints(ArrayOfQoSConstraint value) {
        this.qoSConstraints = value;
    }

}
