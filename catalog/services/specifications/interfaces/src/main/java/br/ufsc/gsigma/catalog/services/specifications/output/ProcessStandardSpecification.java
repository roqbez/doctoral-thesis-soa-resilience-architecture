package br.ufsc.gsigma.catalog.services.specifications.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import br.ufsc.gsigma.catalog.services.model.Document;
import br.ufsc.gsigma.catalog.services.model.Participant;

@Root
public class ProcessStandardSpecification implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String name;

	@Element
	private ProcessTaxonomy processTaxonomy = new ProcessTaxonomy();

	@ElementList
	private List<Document> documents = new ArrayList<Document>();

	@ElementList
	private List<Participant> participants = new ArrayList<Participant>();

	@ElementMap(name = "wsdlLocations", entry = "wsdlLocation", key = "taxonomyClassification", attribute = true)
	private Map<String, String> wsdlLocations = new LinkedHashMap<String, String>();

	public ProcessStandardSpecification() {

	}

	public ProcessStandardSpecification(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}

	public Map<String, String> getWsdlLocations() {
		return wsdlLocations;
	}

	public void setWsdlLocations(Map<String, String> wsdlLocations) {
		this.wsdlLocations = wsdlLocations;
	}

	public ProcessTaxonomy getProcessTaxonomy() {
		return processTaxonomy;
	}

	public void setProcessTaxonomy(ProcessTaxonomy processTaxonomy) {
		this.processTaxonomy = processTaxonomy;
	}

}
