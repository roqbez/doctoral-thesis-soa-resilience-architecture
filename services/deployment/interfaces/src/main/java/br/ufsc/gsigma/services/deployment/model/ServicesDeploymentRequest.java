package br.ufsc.gsigma.services.deployment.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServicesDeploymentRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String deploymentServer;

	private boolean changePortMappingIfNecessary;

	@XmlElementWrapper
	@XmlElement(name = "item")
	private List<ServiceDeploymentRequestItem> itens = new LinkedList<ServiceDeploymentRequestItem>();

	public String getDeploymentServer() {
		return deploymentServer;
	}

	public void setDeploymentServer(String deploymentServer) {
		this.deploymentServer = deploymentServer;
	}

	public boolean isChangePortMappingIfNecessary() {
		return changePortMappingIfNecessary;
	}

	public void setChangePortMappingIfNecessary(boolean changePortMappingIfNecessary) {
		this.changePortMappingIfNecessary = changePortMappingIfNecessary;
	}

	public List<ServiceDeploymentRequestItem> getItens() {
		return itens;
	}

	public void setItens(List<ServiceDeploymentRequestItem> itens) {
		this.itens = itens;
	}

}