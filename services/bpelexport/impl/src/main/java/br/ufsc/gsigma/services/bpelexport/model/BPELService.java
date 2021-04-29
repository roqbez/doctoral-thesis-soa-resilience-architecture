package br.ufsc.gsigma.services.bpelexport.model;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.gsigma.catalog.services.model.QoSCriterion;

public class BPELService {

	private String prefix;
	private String name;
	private String wsdlLocation;
	private String namespace;
	private String partnerLinkName;
	private String taxonomyClassification;
	private String operationName;

	private List<QoSCriterion> qoSConstraints = new ArrayList<QoSCriterion>();

	private List<String> candidateServices = new ArrayList<String>();

	private BPELVariable inputVariable = new BPELVariable();
	private BPELVariable outputVariable = new BPELVariable();
	private BPELVariable outputCallbackVariable = new BPELVariable();

	private BPELVariable aliveInputVariable = new BPELVariable();
	private BPELVariable aliveOutputVariable = new BPELVariable();

	private String callbackSOAPAction;
	private String callbackOperation;

	private String portType;

	public BPELService(String type, String namespace, String taxonomyClassification, Integer index) {

		String[] typeSplit = type.split("_");

		this.operationName = lowerFirstLetter(typeSplit[typeSplit.length - 1]);

		this.prefix = lowerFirstLetter(typeSplit[typeSplit.length - 2]) + "_" + operationName;
		this.name = type + index;
		this.wsdlLocation = type + ".wsdl";
		this.partnerLinkName = type + "_Partner";
		this.namespace = namespace;
		this.taxonomyClassification = taxonomyClassification;

		this.portType = prefix + ":" + name;

		this.inputVariable.setName("input_" + prefix + index);
		this.inputVariable.setMessageType(prefix + ":" + operationName + "AsyncRequestMsg");

		this.outputVariable.setName("output_" + prefix + index);
		this.outputVariable.setMessageType(prefix + ":" + operationName + "AsyncCallbackRequestMsg");

		this.outputCallbackVariable.setName("output_" + prefix + index + "_callback");
		this.outputCallbackVariable.setMessageType(prefix + ":" + operationName + "AsyncCallbackResponseMsg");

		this.aliveInputVariable.setMessageType(prefix + ":aliveRequestMsg");
		this.aliveOutputVariable.setMessageType(prefix + ":aliveResponseMsg");

		operationName += "Async";

	}

	private String lowerFirstLetter(String str) {
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getWsdlLocation() {
		return wsdlLocation;
	}

	public void setWsdlLocation(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getPartnerLinkName() {
		return partnerLinkName;
	}

	public void setPartnerLinkName(String partnerLinkName) {
		this.partnerLinkName = partnerLinkName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getCandidateServices() {
		return candidateServices;
	}

	public void setCandidateServices(List<String> candidateServices) {
		this.candidateServices = candidateServices;
	}

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

	public String getClassification() {
		return taxonomyClassification;
	}

	public void setTaxonomyClassification(String taxonomyClassification) {
		this.taxonomyClassification = taxonomyClassification;
	}

	public List<QoSCriterion> getQoSConstraints() {
		return qoSConstraints;
	}

	public void setQoSConstraints(List<QoSCriterion> qoSConstraints) {
		this.qoSConstraints = qoSConstraints;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public BPELVariable getInputVariable() {
		return inputVariable;
	}

	public void setInputVariable(BPELVariable inputVariable) {
		this.inputVariable = inputVariable;
	}

	public BPELVariable getOutputVariable() {
		return outputVariable;
	}

	public void setOutputVariable(BPELVariable outputVariable) {
		this.outputVariable = outputVariable;
	}

	public String getPortType() {
		return portType;
	}

	public void setPortType(String portType) {
		this.portType = portType;
	}

	public BPELVariable getAliveInputVariable() {
		return aliveInputVariable;
	}

	public void setAliveInputVariable(BPELVariable aliveInputVariable) {
		this.aliveInputVariable = aliveInputVariable;
	}

	public BPELVariable getAliveOutputVariable() {
		return aliveOutputVariable;
	}

	public void setAliveOutputVariable(BPELVariable aliveOutputVariable) {
		this.aliveOutputVariable = aliveOutputVariable;
	}

	public BPELVariable getOutputCallbackVariable() {
		return outputCallbackVariable;
	}

	public void setOutputCallbackVariable(BPELVariable outputCallbackVariable) {
		this.outputCallbackVariable = outputCallbackVariable;
	}

	public String getCallbackSOAPAction() {
		return callbackSOAPAction;
	}

	public void setCallbackSOAPAction(String callbackSOAPAction) {
		this.callbackSOAPAction = callbackSOAPAction;
	}

	public String getCallbackOperation() {
		return callbackOperation;
	}

	public void setCallbackOperation(String callbackOperation) {
		this.callbackOperation = callbackOperation;
	}

	@Override
	public String toString() {
		return name;
	}

}
