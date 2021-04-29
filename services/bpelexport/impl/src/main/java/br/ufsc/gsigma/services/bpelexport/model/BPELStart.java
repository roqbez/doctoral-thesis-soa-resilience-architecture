package br.ufsc.gsigma.services.bpelexport.model;

import java.util.ArrayList;
import java.util.List;

public class BPELStart {

	private List<BPELLink> sourceLinks = new ArrayList<BPELLink>();

	public List<BPELLink> getSourceLinks() {
		return sourceLinks;
	}

	public void setSourceLinks(List<BPELLink> sourceLinks) {
		this.sourceLinks = sourceLinks;
	}

}
