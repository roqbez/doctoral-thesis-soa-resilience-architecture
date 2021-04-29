
package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.revisecontent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.catalogue_2.CatalogueType;


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
 *         &lt;element name="input" type="{urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2}CatalogueType"/>
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
    "input"
})
@XmlRootElement(name = "reviseContent")
public class ReviseContent {

    @XmlElement(required = true, nillable = true)
    protected CatalogueType input;

    /**
     * Gets the value of the input property.
     * 
     * @return
     *     possible object is
     *     {@link CatalogueType }
     *     
     */
    public CatalogueType getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     * 
     * @param value
     *     allowed object is
     *     {@link CatalogueType }
     *     
     */
    public void setInput(CatalogueType value) {
        this.input = value;
    }

}
