
package org.xmlsoap.schemas.ws._2004._03.business_process;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.xmlsoap.schemas.ws._2004._03.business_process package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceRef_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/03/business-process/", "service-ref");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.xmlsoap.schemas.ws._2004._03.business_process
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServiceRefType }
     * 
     */
    public ServiceRefType createServiceRefType() {
        return new ServiceRefType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/03/business-process/", name = "service-ref")
    public JAXBElement<ServiceRefType> createServiceRef(ServiceRefType value) {
        return new JAXBElement<ServiceRefType>(_ServiceRef_QNAME, ServiceRefType.class, null, value);
    }

}
