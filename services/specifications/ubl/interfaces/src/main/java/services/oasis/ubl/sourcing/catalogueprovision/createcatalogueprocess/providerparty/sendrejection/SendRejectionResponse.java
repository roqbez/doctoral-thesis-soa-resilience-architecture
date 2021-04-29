
package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.applicationresponse_2.ApplicationResponseType;


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
 *         &lt;element name="output" type="{urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2}ApplicationResponseType"/>
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
    "output"
})
@XmlRootElement(name = "sendRejectionResponse")
public class SendRejectionResponse {

    @XmlElement(required = true, nillable = true)
    protected ApplicationResponseType output;

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationResponseType }
     *     
     */
    public ApplicationResponseType getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationResponseType }
     *     
     */
    public void setOutput(ApplicationResponseType value) {
        this.output = value;
    }

}
