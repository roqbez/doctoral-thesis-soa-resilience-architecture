package br.ufsc.gsigma.catalog.services.specifications.tests;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import br.ufsc.gsigma.catalog.services.specifications.impl.CatalogSpecificationServiceImpl;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessStandardSpecification;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaskTaxonomy;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy;
import br.ufsc.gsigma.infrastructure.util.xml.SimpleXMLSerializerUtil;

public class SpecificationXmlTest {

	public static void main(String[] args) throws Exception {

		ProcessStandardSpecification spec = null;

		try (InputStream in = SpecificationXmlTest.class.getClassLoader().getResourceAsStream(CatalogSpecificationServiceImpl.PROCESSES_ROOT_PATH + "/ubl/specification.xml")) {
			spec = SimpleXMLSerializerUtil.read(ProcessStandardSpecification.class, in);
		}

		for (ProcessTaxonomy p : spec.getProcessTaxonomy().getChilds()) {
			if ("ubl/ordering".equals(p.getTaxonomyClassification())) {
				for (ProcessTaxonomy p2 : p.getChilds()) {
					if ("ubl/ordering/orderingprocess".equals(p2.getTaxonomyClassification())) {
						for (int i = 0; i < p2.getTasks().size(); i++) {

							ProcessTaskTaxonomy t = p2.getTasks().get(i);

							if ("ubl/ordering/orderingprocess/buyerParty/acceptOrder".equals(t.getTaxonomyClassification())) {

								ProcessTaskTaxonomy ptax = new ProcessTaskTaxonomy();

								ptax.setName("Reject Order");
								ptax.setTaxonomyClassification("ubl/ordering/orderingprocess/buyerParty/rejectOrder");
								ptax.setParent(t.getParent());
								ptax.setParticipantName(t.getParticipantName());

								p2.getTasks().add(i + 1, ptax);
							}
						}
					}
				}
			}
		}

		Map<String, String> wsdlLocations = new TreeMap<String, String>(spec.getWsdlLocations());
		wsdlLocations.put("ubl/ordering/orderingprocess/buyerParty/rejectOrder", "ordering/orderingprocess/buyerParty/UBL_OrderingProcess_BuyerParty_RejectOrder.wsdl");

		spec.setWsdlLocations(wsdlLocations);

		try (OutputStream out = new FileOutputStream("specification-ubl.xml")) {
			SimpleXMLSerializerUtil.write(spec, out);
		}

	}

}
