package br.ufsc.gsigma.servicediscovery.support;

import javax.xml.bind.annotation.XmlRegistry;

import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the br.ufsc.gsigma.servicediscovery package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: br.ufsc.gsigma.servicediscovery
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link QoSConstraint }
	 * 
	 */
	public QoSConstraint createQoSConstraint() {
		return new QoSConstraint();
	}

	/**
	 * Create an instance of {@link ServiceDiscoveryInfo }
	 * 
	 */
	public ServiceDiscoveryInfo createServiceDiscoveryInfo() {
		return new ServiceDiscoveryInfo();
	}

	/**
	 * Create an instance of {@link ArrayOfQoSConstraint }
	 * 
	 */
	public ArrayOfQoSConstraint createArrayOfQoSConstraint() {
		return new ArrayOfQoSConstraint();
	}

	/**
	 * Create an instance of {@link ArrayOfCandidateService }
	 * 
	 */
	public ArrayOfCandidateService createArrayOfCandidateService() {
		return new ArrayOfCandidateService();
	}

}
