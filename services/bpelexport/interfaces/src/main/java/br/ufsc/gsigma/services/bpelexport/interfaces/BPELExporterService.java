package br.ufsc.gsigma.services.bpelexport.interfaces;

import javax.jws.WebParam;
import javax.jws.WebService;

import br.ufsc.gsigma.catalog.services.model.ITConfiguration;
import br.ufsc.gsigma.services.bpelexport.output.BPELProcessBundle;

@WebService
public interface BPELExporterService {

	public BPELProcessBundle convertToBPEL(@WebParam(name = "process") br.ufsc.gsigma.catalog.services.model.Process process, @WebParam(name = "itConfiguration") ITConfiguration ITConfiguration)
			throws Exception;

}
