package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

@SuppressWarnings("rawtypes")
public class WSDLDefinitionDelegate implements Definition {

	private static final long serialVersionUID = 1L;

	private Definition delegate;

	public WSDLDefinitionDelegate(Definition delegate) {
		this.delegate = delegate;
	}

	public void addExtensibilityElement(ExtensibilityElement extElement) {
		delegate.addExtensibilityElement(extElement);
	}

	public void setDocumentBaseURI(String documentBaseURI) {
		delegate.setDocumentBaseURI(documentBaseURI);
	}

	public void setDocumentationElement(Element docEl) {
		delegate.setDocumentationElement(docEl);
	}

	public ExtensibilityElement removeExtensibilityElement(ExtensibilityElement extElement) {
		return delegate.removeExtensibilityElement(extElement);
	}

	public void setExtensionAttribute(QName name, Object value) {
		delegate.setExtensionAttribute(name, value);
	}

	public Element getDocumentationElement() {
		return delegate.getDocumentationElement();
	}

	public String getDocumentBaseURI() {
		return delegate.getDocumentBaseURI();
	}

	public List getExtensibilityElements() {
		return delegate.getExtensibilityElements();
	}

	public void setQName(QName name) {
		delegate.setQName(name);
	}

	public QName getQName() {
		return delegate.getQName();
	}

	public void setTargetNamespace(String targetNamespace) {
		delegate.setTargetNamespace(targetNamespace);
	}

	public Object getExtensionAttribute(QName name) {
		return delegate.getExtensionAttribute(name);
	}

	public String getTargetNamespace() {
		return delegate.getTargetNamespace();
	}

	public void addNamespace(String prefix, String namespaceURI) {
		delegate.addNamespace(prefix, namespaceURI);
	}

	public Map getExtensionAttributes() {
		return delegate.getExtensionAttributes();
	}

	public List getNativeAttributeNames() {
		return delegate.getNativeAttributeNames();
	}

	public String getNamespace(String prefix) {
		return delegate.getNamespace(prefix);
	}

	public String removeNamespace(String prefix) {
		return delegate.removeNamespace(prefix);
	}

	public String getPrefix(String namespaceURI) {
		return delegate.getPrefix(namespaceURI);
	}

	public Map getNamespaces() {
		return delegate.getNamespaces();
	}

	public void setTypes(Types types) {
		delegate.setTypes(types);
	}

	public Types getTypes() {
		return delegate.getTypes();
	}

	public void addImport(Import importDef) {
		delegate.addImport(importDef);
	}

	public Import removeImport(Import importDef) {
		return delegate.removeImport(importDef);
	}

	public List getImports(String namespaceURI) {
		return delegate.getImports(namespaceURI);
	}

	public Map getImports() {
		return delegate.getImports();
	}

	public void addMessage(Message message) {
		delegate.addMessage(message);
	}

	public Message getMessage(QName name) {
		return delegate.getMessage(name);
	}

	public Message removeMessage(QName name) {
		return delegate.removeMessage(name);
	}

	public Map getMessages() {
		return delegate.getMessages();
	}

	public void addBinding(Binding binding) {
		delegate.addBinding(binding);
	}

	public Binding getBinding(QName name) {
		return delegate.getBinding(name);
	}

	public Binding removeBinding(QName name) {
		return delegate.removeBinding(name);
	}

	public Map getBindings() {
		return delegate.getBindings();
	}

	public Map getAllBindings() {
		return delegate.getAllBindings();
	}

	public void addPortType(PortType portType) {
		delegate.addPortType(portType);
	}

	public PortType getPortType(QName name) {
		return delegate.getPortType(name);
	}

	public PortType removePortType(QName name) {
		return delegate.removePortType(name);
	}

	public Map getPortTypes() {
		return delegate.getPortTypes();
	}

	public Map getAllPortTypes() {
		return delegate.getAllPortTypes();
	}

	public void addService(Service service) {
		delegate.addService(service);
	}

	public Service getService(QName name) {
		return delegate.getService(name);
	}

	public Service removeService(QName name) {
		return delegate.removeService(name);
	}

	public Map getServices() {
		return delegate.getServices();
	}

	public Map getAllServices() {
		return delegate.getAllServices();
	}

	public Binding createBinding() {
		return delegate.createBinding();
	}

	public BindingFault createBindingFault() {
		return delegate.createBindingFault();
	}

	public BindingInput createBindingInput() {
		return delegate.createBindingInput();
	}

	public BindingOperation createBindingOperation() {
		return delegate.createBindingOperation();
	}

	public BindingOutput createBindingOutput() {
		return delegate.createBindingOutput();
	}

	public Fault createFault() {
		return delegate.createFault();
	}

	public Import createImport() {
		return delegate.createImport();
	}

	public Input createInput() {
		return delegate.createInput();
	}

	public Message createMessage() {
		return delegate.createMessage();
	}

	public Operation createOperation() {
		return delegate.createOperation();
	}

	public Output createOutput() {
		return delegate.createOutput();
	}

	public Part createPart() {
		return delegate.createPart();
	}

	public Port createPort() {
		return delegate.createPort();
	}

	public PortType createPortType() {
		return delegate.createPortType();
	}

	public Service createService() {
		return delegate.createService();
	}

	public Types createTypes() {
		return delegate.createTypes();
	}

	public ExtensionRegistry getExtensionRegistry() {
		return delegate.getExtensionRegistry();
	}

	public void setExtensionRegistry(ExtensionRegistry extReg) {
		delegate.setExtensionRegistry(extReg);
	}

}
