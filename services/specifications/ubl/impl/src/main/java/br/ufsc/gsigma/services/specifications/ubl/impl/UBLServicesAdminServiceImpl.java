package br.ufsc.gsigma.services.specifications.ubl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.cxf.ServiceAvailabilityFeature;
import br.ufsc.gsigma.services.events.ServiceStartedEvent;
import br.ufsc.gsigma.services.events.ServiceStoppedEvent;
import br.ufsc.gsigma.services.specifications.ubl.bootstrap.RunUBLServices;
import br.ufsc.gsigma.services.specifications.ubl.interfaces.UBLServicesAdminService;
import br.ufsc.gsigma.services.specifications.ubl.util.Util;

public class UBLServicesAdminServiceImpl implements UBLServicesAdminService {

	private static final Logger logger = Logger.getLogger(UBLServicesAdminServiceImpl.class);

	private ServiceAvailabilityFeature serviceAvailabilityFeature;

	private Map<String, String> mapServicePathNamespace = new HashMap<String, String>();

	private Map<String, String> mapNamespaceServicePath = new HashMap<String, String>();

	private Map<String, UBLServicesAdminService> adminServices = new HashMap<String, UBLServicesAdminService>();

	private boolean isSendEvents() {
		return "true".equals(System.getProperty("sendEvents", "false"));
	}

	private UBLServicesAdminService getUBLServicesAdminService(String hostPort) {

		UBLServicesAdminService service = adminServices.get(hostPort);

		if (service == null) {
			service = ServiceClient.getClient(UBLServicesAdminService.class, hostPort + "/services/UBLServicesAdminService");
			adminServices.put(hostPort, service);
		}
		return service;
	}

	public UBLServicesAdminServiceImpl(ServiceAvailabilityFeature serviceAvailabilityFeature) {
		this.serviceAvailabilityFeature = serviceAvailabilityFeature;
	}

	public void registerService(String servicePath, Object serviceImpl) {
		String serviceNamespace = Util.getServiceNamespace(serviceImpl);
		this.mapServicePathNamespace.put(servicePath, serviceNamespace);
		this.mapNamespaceServicePath.put(serviceNamespace, servicePath);
	}

	@Override
	public boolean isServiceAvailable(String servicePath) {
		return serviceAvailabilityFeature.isServiceAvailable(servicePath);
	}

	@Override
	public String setServiceAvailable(String servicePath) {
		serviceAvailabilityFeature.setServiceAvailable(servicePath);

		if (servicePath != null) {
			String namespace = mapServicePathNamespace.get(servicePath);
			String serviceUrl = RunUBLServices.getServiceUrlBase() + servicePath;
			if (isSendEvents()) {
				EventSender.getInstance().sendEvent(new ServiceStartedEvent(namespace, serviceUrl));
			}
		} else {
			for (String sp : mapServicePathNamespace.keySet()) {
				String namespace = mapServicePathNamespace.get(sp);
				String serviceUrl = RunUBLServices.getServiceUrlBase() + sp;
				if (isSendEvents()) {
					EventSender.getInstance().sendEvent(new ServiceStartedEvent(namespace, serviceUrl));
				}
			}
		}
		return UBLServicesAdminService.STATUS_SERVICE_AVAILABLE;
	}

	@Override
	public String setServiceUnavailable(String servicePath) {
		serviceAvailabilityFeature.setServiceUnvailable(servicePath);

		if (servicePath != null) {
			String namespace = mapServicePathNamespace.get(servicePath);
			String serviceUrl = RunUBLServices.getServiceUrlBase() + servicePath;
			if (isSendEvents()) {
				EventSender.getInstance().sendEvent(new ServiceStoppedEvent(namespace, serviceUrl));
			}
		} else {
			for (String sp : mapServicePathNamespace.keySet()) {
				String namespace = mapServicePathNamespace.get(sp);
				String serviceUrl = RunUBLServices.getServiceUrlBase() + sp;
				if (isSendEvents()) {
					EventSender.getInstance().sendEvent(new ServiceStoppedEvent(namespace, serviceUrl));
				}
			}
		}
		return UBLServicesAdminService.STATUS_SERVICE_UNAVAILABLE;
	}

	@Override
	public String changeServiceAvailability(String servicePath) {

		if (serviceAvailabilityFeature.isServiceAvailable(servicePath)) {
			return setServiceUnavailable(servicePath);
		} else {
			return setServiceAvailable(servicePath);
		}
	}

	@Override
	public void setServicesAvailability(List<String> availableServicesPaths, List<String> unavailableServicesPaths) {

		serviceAvailabilityFeature.setServicesAvailability(availableServicesPaths, unavailableServicesPaths);

		if (availableServicesPaths != null) {
			for (String servicePath : availableServicesPaths) {
				String namespace = mapServicePathNamespace.get(servicePath);
				String serviceUrl = RunUBLServices.getServiceUrlBase() + servicePath;
				if (isSendEvents()) {
					EventSender.getInstance().sendEvent(new ServiceStartedEvent(namespace, serviceUrl));
				}
			}
		}

		if (unavailableServicesPaths != null) {
			for (String servicePath : unavailableServicesPaths) {
				String namespace = mapServicePathNamespace.get(servicePath);
				String serviceUrl = RunUBLServices.getServiceUrlBase() + servicePath;
				if (isSendEvents()) {
					EventSender.getInstance().sendEvent(new ServiceStoppedEvent(namespace, serviceUrl));
				}
			}
		}
	}

	@Override
	public String setRandomServiceAvailability(String servicePath) {
		if (new Random().nextInt(2) == 0) {
			return setServiceUnavailable(servicePath);
		} else {
			return setServiceAvailable(servicePath);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getServiceAvailability(String servicePath, List<String> hosts) {

		if (hosts == null || hosts.size() == 0)
			return Collections.EMPTY_LIST;

		List<String> result = new ArrayList<String>(hosts.size());

		for (String hostPort : hosts) {

			UBLServicesAdminService client = getUBLServicesAdminService(hostPort);
			try {
				if (client.isServiceAvailable(servicePath))
					result.add(hostPort);
			} catch (SOAPFaultException e) {
				logger.error("Error invoking UBLServicesAdminService at " + ((org.apache.cxf.endpoint.Client) client).getEndpoint().getEndpointInfo().getAddress(), e);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> setHostsServicesAvailability(List<String> servicesPath, List<String> hosts, boolean available) {

		Collection<String> result = new LinkedHashSet<String>();

		for (String hostPort : hosts) {

			UBLServicesAdminService client = getUBLServicesAdminService(hostPort);

			try {
				logger.debug("Invoking UBLServicesAdminService at " + ((org.apache.cxf.endpoint.Client) client).getEndpoint().getEndpointInfo().getAddress() //
						+ (available ? "\n\tavailableServicePaths=" + servicesPath //
								: "\n\tunavailableServicePaths=" + servicesPath)//
				);
				client.setServicesAvailability(available ? servicesPath : Collections.EMPTY_LIST, !available ? servicesPath : Collections.EMPTY_LIST);

				result.addAll(servicesPath);

			} catch (SOAPFaultException e) {
				logger.error("Error invoking UBLServicesAdminService at " + ((org.apache.cxf.endpoint.Client) client).getEndpoint().getEndpointInfo().getAddress(), e);
			}
		}

		return new ArrayList<String>(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> setRandomServiceAvailabilityCombination(Map<String, Integer> servicePath, List<String> hosts) {

		try {

			Collection<HostServicePath> as = new LinkedHashSet<HostServicePath>();
			Collection<HostServicePath> us = new LinkedHashSet<HostServicePath>();

			for (Entry<String, Integer> e : servicePath.entrySet()) {

				String sp = e.getKey();
				int nAvalServices = e.getValue();

				Set<Integer> randoms = new TreeSet<Integer>();

				while (randoms.size() != nAvalServices) {
					randoms.add(new Random().nextInt(hosts.size()));
				}

				randoms.forEach(n -> as.add(new HostServicePath(hosts.get(n), sp)));

				for (String h : hosts) {
					HostServicePath hsp = new HostServicePath(h, sp);
					if (!as.contains(hsp)) {
						us.add(hsp);
					}
				}
			}

			Map<String, List<HostServicePath>> mhas = as.stream().collect(Collectors.groupingBy(HostServicePath::getHost, Collectors.toList()));
			Map<String, List<HostServicePath>> mhus = us.stream().collect(Collectors.groupingBy(HostServicePath::getHost, Collectors.toList()));

			for (String hostPort : hosts) {

				List<HostServicePath> mhasl = mhas.get(hostPort) != null ? mhas.get(hostPort) : Collections.EMPTY_LIST;
				List<HostServicePath> mhusl = mhus.get(hostPort) != null ? mhus.get(hostPort) : Collections.EMPTY_LIST;

				List<String> availablePaths = mhasl.stream().map(HostServicePath::getServicePath).collect(Collectors.toList());
				List<String> unavailablePaths = mhusl.stream().map(HostServicePath::getServicePath).collect(Collectors.toList());

				UBLServicesAdminService client = getUBLServicesAdminService(hostPort);
				try {
					logger.debug("Invoking UBLServicesAdminService at " + ((org.apache.cxf.endpoint.Client) client).getEndpoint().getEndpointInfo().getAddress() //
							+ "\n\tavailableServicePaths=" + availablePaths //
							+ "\n\tunavailableServicePaths=" + unavailablePaths //
					);
					client.setServicesAvailability(availablePaths, unavailablePaths);
				} catch (SOAPFaultException e) {
					logger.error("Error invoking UBLServicesAdminService at " + ((org.apache.cxf.endpoint.Client) client).getEndpoint().getEndpointInfo().getAddress(), e);
				}
			}

			return as.stream().map(hp -> hp.toString()).collect(Collectors.toList());

		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	// public static void main(String[] args) {
	// UBLServicesAdminServiceImpl service = new UBLServicesAdminServiceImpl(null);
	//
	// Map<String, Integer> servicePath = new LinkedHashMap<String, Integer>();
	// servicePath.put("/services/ubl/paymentprocess/accountingCustomer/authorizePayment", 1);
	// servicePath.put("/services/ubl/paymentprocess/accountingCustomer/notifyOfPayment", 1);
	// servicePath.put("/services/ubl/paymentprocess/accountingSupplier/notifyPayee", 1);
	// servicePath.put("/services/ubl/paymentprocess/accountingSupplier/receiveAdvice", 1);
	// servicePath.put("/services/ubl/paymentprocess/payeeParty/receiveAdvice", 1);
	//
	// List<String> hosts = Arrays.asList(//
	// "http://ublservices.d-201603244.ufsc.br:11000", //
	// "http://ublservices2.d-201603244.ufsc.br:11001", //
	// "http://ublservices3.d-201603244.ufsc.br:11002", //
	// "http://ublservices4.d-201603244.ufsc.br:11003", //
	// "http://ublservices5.d-201603244.ufsc.br:11004"//
	// );
	//
	// List<String> combination = service.setRandomServiceAvailabilityCombination(servicePath, hosts);
	//
	// for (String s : combination)
	// System.out.println(s);
	// }

	private class HostServicePath {

		String host;

		String servicePath;

		public HostServicePath(String host, String servicePath) {
			this.host = host;
			this.servicePath = servicePath;
		}

		public String getHost() {
			return host;
		}

		public String getServicePath() {
			return servicePath;
		}

		@Override
		public String toString() {
			return host + servicePath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + ((servicePath == null) ? 0 : servicePath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HostServicePath other = (HostServicePath) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (servicePath == null) {
				if (other.servicePath != null)
					return false;
			} else if (!servicePath.equals(other.servicePath))
				return false;
			return true;
		}

		private UBLServicesAdminServiceImpl getOuterType() {
			return UBLServicesAdminServiceImpl.this;
		}

	}
}
