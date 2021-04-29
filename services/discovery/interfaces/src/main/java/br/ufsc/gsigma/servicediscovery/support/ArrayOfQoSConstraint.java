
package br.ufsc.gsigma.servicediscovery.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;


/**
 * <p>Java class for ArrayOfQoSConstraint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfQoSConstraint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="qoSConstraint" type="{http://gsigma.ufsc.br/serviceDiscovery}QoSConstraint" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfQoSConstraint", propOrder = {
    "qoSConstraint"
})
public class ArrayOfQoSConstraint {

    @XmlElement(nillable = true)
    protected List<QoSConstraint> qoSConstraint;

    /**
     * Gets the value of the qoSConstraint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qoSConstraint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQoSConstraint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QoSConstraint }
     * 
     * 
     */
    public List<QoSConstraint> getQoSConstraint() {
        if (qoSConstraint == null) {
            qoSConstraint = new ArrayList<QoSConstraint>();
        }
        return this.qoSConstraint;
    }

}
