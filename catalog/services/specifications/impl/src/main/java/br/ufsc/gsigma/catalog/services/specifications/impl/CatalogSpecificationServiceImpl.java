package br.ufsc.gsigma.catalog.services.specifications.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.specifications.interfaces.CatalogSpecificationService;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessStandardSpecification;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy;
import br.ufsc.gsigma.infrastructure.util.Util;
import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.infrastructure.util.xml.SimpleXMLSerializerUtil;

public class CatalogSpecificationServiceImpl implements CatalogSpecificationService {

	private static final ClassLoader classLoader = CatalogSpecificationServiceImpl.class.getClassLoader();

	public static final String PROCESSES_ROOT_PATH = "process_standards";

	private static final Logger logger = LoggerFactory.getLogger(CatalogSpecificationServiceImpl.class);

	@Override
	public boolean ping() {
		return true;
	}

	@Override
	public ProcessStandardSpecification getProcessStandardSpecification(String processStandardId) {
		try (InputStream in = classLoader.getResourceAsStream(PROCESSES_ROOT_PATH + "/" + processStandardId.toLowerCase() + "/specification.xml")) {
			return SimpleXMLSerializerUtil.read(ProcessStandardSpecification.class, in);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Process getProcessFromProcessStandardSpecification(String taxonomyClassification) {

		String folder = null;

		try {

			folder = taxonomyClassification.substring(0, taxonomyClassification.indexOf('/') + 1).toLowerCase();

			try (InputStream in = classLoader.getResourceAsStream(PROCESSES_ROOT_PATH + "/" + folder.toLowerCase() + "/processes/"
					+ taxonomyClassification.substring(taxonomyClassification.indexOf('/'), taxonomyClassification.length()) + "/process.xml")) {
				return SimpleXMLSerializerUtil.read(Process.class, in);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public byte[] getDocumentsXSDZipFromProcessStandardSpecification(String processStandardId) {

		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				String xsdFolder = PROCESSES_ROOT_PATH + "/" + processStandardId.toLowerCase() + "/xsd";
				ZipUtils.zipFromClassLoaderResources(classLoader, xsdFolder, zos);
			}

			return baos.toByteArray();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public ProcessTaxonomy getProcessTaxonomy(String processStandardId) {

		String folder = processStandardId.toLowerCase();

		try (InputStream in = classLoader.getResourceAsStream(PROCESSES_ROOT_PATH + "/" + folder + "/specification.xml")) {
			ProcessStandardSpecification p = SimpleXMLSerializerUtil.read(ProcessStandardSpecification.class, in);
			return p.getProcessTaxonomy();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getServiceWSDLFromTaxonomyClassification(String taxonomyClassification) {

		if (taxonomyClassification == null)
			return null;

		try {

			logger.debug("taxonomyClassification=" + taxonomyClassification);

			String folder = taxonomyClassification.substring(0, taxonomyClassification.indexOf('/')).toLowerCase();

			try (InputStream in = classLoader.getResourceAsStream(PROCESSES_ROOT_PATH + "/" + folder + "/specification.xml")) {

				ProcessStandardSpecification p = SimpleXMLSerializerUtil.read(ProcessStandardSpecification.class, in);

				try (InputStream wsdlIs = classLoader.getResourceAsStream(PROCESSES_ROOT_PATH + "/" + folder + "/wsdl/" + p.getWsdlLocations().get(taxonomyClassification))) {
					return Util.readStreamAsString(wsdlIs);
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}
}
