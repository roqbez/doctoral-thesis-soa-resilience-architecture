package br.ufsc.gsigma.services.execution.bpel.util;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ode.bpel.iapi.ProcessConf;

import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.common.hash.HashUtil;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.services.execution.bpel.bootstrap.RunExecutionService;

public abstract class ProcessUtil {

	public static String getProcessUrl(String processName) {
		return "http://" + RunExecutionService.EXECUTION_SERVICE_HOSTNAME + ":" + RunExecutionService.EXECUTION_SERVICE_PORT + "/ode/processes/" + processName;
	}

	public static String getProcessExecutionServiceUrl() {
		return RunExecutionService.PROCESS_EXECUTION_SERVICE_URL;
	}

	public static String getApplicationId(ProcessConf processConf) throws Exception {
		return getApplicationId(getBusinessProcess(processConf), getServicesInformation(processConf), processConf.getProcessId(), processConf.getDeployDate());
	}

	public static String getApplicationId(br.ufsc.gsigma.catalog.services.model.Process businessProcess, ServicesInformation servicesInformation, QName processId, Date processDeploymentDate) {

		int hash = HashUtil.getHashFromValues( //
				businessProcess.getName(), //
				processId //
		// servicesInformation //
		// processDeploymentDate // is this necessary?
		);

		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(hash);

		return DigestUtils.sha1Hex(buf.array());
	}

	public static ServicesInformation getServicesInformation(ProcessConf processConf) throws Exception {

		File fServicesInformation = null;

		for (File f : processConf.getFiles()) {
			if (f.getName().equals("servicesInformation.xml")) {
				fServicesInformation = f;
				break;
			}
		}

		if (fServicesInformation == null)
			throw new IllegalArgumentException("servicesDescription.xml not found");

		return JAXBSerializerUtil.read(ServicesInformation.class, fServicesInformation);
	}

	public static Process getBusinessProcess(ProcessConf processConf) throws Exception {

		File fBusinessProcess = null;

		for (File f : processConf.getFiles()) {
			if (f.getName().equals("businessProcess.xml")) {
				fBusinessProcess = f;
				break;
			}
		}

		if (fBusinessProcess == null)
			throw new IllegalArgumentException("businessProcess.xml not found");

		return JAXBSerializerUtil.read(br.ufsc.gsigma.catalog.services.model.Process.class, fBusinessProcess);
	}

}
