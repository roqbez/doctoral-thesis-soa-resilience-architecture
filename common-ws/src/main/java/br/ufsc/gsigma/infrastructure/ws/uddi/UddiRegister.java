package br.ufsc.gsigma.infrastructure.ws.uddi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.juddi.v3.client.UDDIConstants;
import org.apache.juddi.v3.client.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.AccessPoint;
import org.uddi.api_v3.AuthToken;
import org.uddi.api_v3.BindingTemplate;
import org.uddi.api_v3.BindingTemplates;
import org.uddi.api_v3.BusinessDetail;
import org.uddi.api_v3.BusinessEntity;
import org.uddi.api_v3.BusinessService;
import org.uddi.api_v3.CategoryBag;
import org.uddi.api_v3.Description;
import org.uddi.api_v3.GetAuthToken;
import org.uddi.api_v3.KeyedReference;
import org.uddi.api_v3.KeyedReferenceGroup;
import org.uddi.api_v3.Name;
import org.uddi.api_v3.SaveBusiness;
import org.uddi.api_v3.SaveService;
import org.uddi.api_v3.ServiceDetail;
import org.uddi.api_v3.TModelInstanceDetails;
import org.uddi.api_v3.TModelInstanceInfo;
import org.uddi.v3_service.UDDIPublicationPortType;
import org.uddi.v3_service.UDDISecurityPortType;

public class UddiRegister {

	private final static Logger logger = LoggerFactory.getLogger(UddiRegister.class);

	public static void publishService(Transport transport, String authLogin, String authPassword, String businessServiceKey, String serviceKey, String serviceNameStr, String serviceDescriptionStr, String endpointStr, String endpointType,
			TModelInstanceInfo[] tModelInstanceInfos) throws Exception {

		publishService(transport, authLogin, authPassword, businessServiceKey, serviceKey, null, serviceNameStr, serviceDescriptionStr, endpointStr, endpointType, "br.ufsc.gsigma.binding.converters.SoapServiceProtocolConverter", tModelInstanceInfos,
				null, null, null);
	}

	public static void publishService(Transport transport, String authLogin, String authPassword, String businessServiceKey, String serviceKey, String serviceNameStr, String serviceDescriptionStr, String endpointStr, String endpointType,
			String serviceProtocolConverter, TModelInstanceInfo[] tModelInstanceInfos) throws Exception {

		publishService(transport, authLogin, authPassword, businessServiceKey, serviceKey, null, serviceNameStr, serviceDescriptionStr, endpointStr, endpointType, serviceProtocolConverter, tModelInstanceInfos, null, null, null);
	}

	public static void publishService(Transport transport, String authLogin, String authPassword, String businessServiceKey, String serviceKey, String serviceKeyPrefix, String serviceNameStr, String serviceDescriptionStr, String endpointStr,
			String endpointType, String serviceProtocolConverter, TModelInstanceInfo[] tModelInstanceInfos, KeyedReference[] qoSConstraintsKeyedReferences, String nodeName, Map<String, Integer> mapServiceKeyPrefixCount) throws Exception {

		BusinessService service = getBusinessServiceForService(businessServiceKey, serviceKey, serviceKeyPrefix, serviceNameStr, serviceDescriptionStr, endpointStr, endpointType, serviceProtocolConverter, tModelInstanceInfos,
				qoSConstraintsKeyedReferences, nodeName, mapServiceKeyPrefixCount);

		publishBusinessServices(transport, authLogin, authPassword, Collections.singleton(service));

	}

	public static List<BusinessService> publishBusinessServices(Transport transport, String authLogin, String authPassword, Collection<BusinessService> services) throws Exception {

		UDDIPublicationPortType publish = transport.getUDDIPublishService();
		UDDISecurityPortType security = transport.getUDDISecurityService();

		if (logger.isInfoEnabled()) {

			StringBuilder sb = new StringBuilder();

			sb.append("Publishing " + services.size() + (services.size() > 1 ? " services" : " service") + " in UDDI at URL: " + UddiLocator.getUrlFromUDDIService(publish));

			for (BusinessService s : services) {
				sb.append("\n\t" + s.getServiceKey());
				sb.append("\n\t\tbusinessKey: " + s.getBusinessKey());
				sb.append("\n\t\tname: " + toNamesString(s.getName()));
				sb.append("\n\t\tdescription: " + toDescriptionsString(s.getDescription()));
				sb.append("\n\t\tbindingTemplates: " + toBindingTemplatesString(s.getBindingTemplates()));
			}
			logger.info(sb.toString());
		}

		GetAuthToken getAuthTokenRoot = new GetAuthToken();
		getAuthTokenRoot.setUserID(authLogin);
		getAuthTokenRoot.setCred(authPassword);

		AuthToken authToken = security.getAuthToken(getAuthTokenRoot);

		SaveService ss = new SaveService();
		ss.getBusinessService().addAll(services);
		ss.setAuthInfo(authToken.getAuthInfo());

		ServiceDetail sd = publish.saveService(ss);

		return sd.getBusinessService();

	}

	public static BusinessService getBusinessServiceForService(String businessServiceKey, String serviceKey, String serviceKeyPrefix, String serviceNameStr, String serviceDescriptionStr, String endpointStr, String endpointType,
			String serviceProtocolConverter, TModelInstanceInfo[] tModelInstanceInfos, KeyedReference[] qoSConstraintsKeyedReferences, String nodeName, Map<String, Integer> mapServiceKeyPrefixCount) {
		BusinessService service = new BusinessService();

		if (businessServiceKey != null)
			service.setBusinessKey(businessServiceKey);

		if (serviceKeyPrefix != null && tModelInstanceInfos != null && tModelInstanceInfos.length > 0) {
			serviceKey = serviceKeyPrefix + ":" + (nodeName != null ? nodeName + "-" : "") + tModelInstanceInfos[0].getTModelKey().replaceAll(":", "-");

			if (mapServiceKeyPrefixCount != null) {

				Integer c = mapServiceKeyPrefixCount.get(serviceKey);

				if (c == null)
					c = 1;
				else
					c++;

				serviceKey += "-" + c;

				mapServiceKeyPrefixCount.put(serviceKey, c);
			}
		}

		if (serviceKey != null)
			service.setServiceKey(serviceKey);

		Name serviceName = new Name();
		serviceName.setValue(serviceNameStr);
		service.getName().add(serviceName);

		Description serviceDescription = new Description();
		serviceDescription.setValue(serviceDescriptionStr);
		service.getDescription().add(serviceDescription);

		BindingTemplates bts = new BindingTemplates();
		service.setBindingTemplates(bts);

		BindingTemplate bt = new BindingTemplate();

		if (serviceKey != null) {
			bt.setBindingKey(serviceKey + "-binding");
		}

		Description bindingDescription = new Description();
		bindingDescription.setValue("Binding for " + serviceName.getValue());
		bt.getDescription().add(bindingDescription);

		AccessPoint ap = new AccessPoint();
		ap.setValue(endpointStr);
		ap.setUseType(endpointType);
		bt.setAccessPoint(ap);
		bts.getBindingTemplate().add(bt);

		TModelInstanceDetails tModelInstanceDetails = new TModelInstanceDetails();
		bt.setTModelInstanceDetails(tModelInstanceDetails);

		TModelInstanceInfo httpTransportTModelInstanceInfo = new TModelInstanceInfo();
		httpTransportTModelInstanceInfo.setTModelKey(UDDIConstants.TRANSPORT_HTTP);
		tModelInstanceDetails.getTModelInstanceInfo().add(httpTransportTModelInstanceInfo);

		if (tModelInstanceInfos != null) {
			for (TModelInstanceInfo t : tModelInstanceInfos)
				tModelInstanceDetails.getTModelInstanceInfo().add(t);
		}

		CategoryBag categoryBag = new CategoryBag();
		bt.setCategoryBag(categoryBag);

		categoryBag.getKeyedReference().add(new KeyedReference("uddi:uddi.org:categorization:types", UddiKeys.UDDI_PROCESSES_SERVICEPROTOCOLCONVERTER, serviceProtocolConverter));

		if ("wsdlDeployment".equals(endpointType)) {
			categoryBag.getKeyedReference().add(new KeyedReference("uddi:uddi.org:categorization:types", "uddi-org:types:wsdl", "wsdlDeployment"));
		}

		if (!ArrayUtils.isEmpty(qoSConstraintsKeyedReferences)) {

			KeyedReferenceGroup qoSkeyedReferenceGroup = new KeyedReferenceGroup();
			qoSkeyedReferenceGroup.setTModelKey(UddiKeys.UDDI_QOS_SERVICEQOSLIST);
			categoryBag.getKeyedReferenceGroup().add(qoSkeyedReferenceGroup);

			for (KeyedReference k : qoSConstraintsKeyedReferences)
				qoSkeyedReferenceGroup.getKeyedReference().add(k);
		}
		return service;
	}

	public static TModelInstanceInfo getTModelInstanceInfo(String tModelKey) {
		TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo();
		tModelInstanceInfo.setTModelKey(tModelKey);
		return tModelInstanceInfo;
	}

	public static KeyedReference getKeyedReference(String tModelKey, String keyName, Object keyValue) {
		KeyedReference keyedReference = new KeyedReference();
		keyedReference.setTModelKey(tModelKey);
		keyedReference.setKeyName(keyName);
		keyedReference.setKeyValue(keyValue.toString());
		return keyedReference;
	}

	public static KeyedReference getKeyedReference(String tModelKey, Object keyValue) {
		KeyedReference keyedReference = new KeyedReference();
		keyedReference.setTModelKey(tModelKey);
		keyedReference.setKeyValue(keyValue.toString());
		return keyedReference;
	}

	public static BusinessDetail createBusinessEntity(Transport transport, String authLogin, String authPassword, String name) throws Exception {

		UDDISecurityPortType security = transport.getUDDISecurityService();
		UDDIPublicationPortType publish = transport.getUDDIPublishService();

		GetAuthToken getAuthTokenRoot = new GetAuthToken();
		getAuthTokenRoot.setUserID(authLogin);
		getAuthTokenRoot.setCred(authPassword);

		AuthToken authToken = security.getAuthToken(getAuthTokenRoot);

		BusinessEntity businessEntity = new BusinessEntity();
		Name businessEntityName = new Name();
		businessEntityName.setValue(name);
		businessEntity.getName().add(businessEntityName);

		SaveBusiness sb = new SaveBusiness();
		sb.getBusinessEntity().add(businessEntity);

		sb.setAuthInfo(authToken.getAuthInfo());

		return publish.saveBusiness(sb);
	}

	private static String toNamesString(List<Name> names) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (names != null) {
			int i = 0;
			for (Name n : names) {
				sb.append(n.getValue());
				if (i++ < names.size() - 1)
					sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private static String toDescriptionsString(List<Description> descriptions) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (descriptions != null) {
			int i = 0;
			for (Description d : descriptions) {
				sb.append(d.getValue());
				if (i++ < descriptions.size() - 1)
					sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private static String toBindingTemplatesString(BindingTemplates bindingTemplates) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (bindingTemplates != null && bindingTemplates.getBindingTemplate() != null) {
			int i = 0;

			for (BindingTemplate b : bindingTemplates.getBindingTemplate()) {
				if (b.getAccessPoint() != null) {
					sb.append(b.getAccessPoint().getValue());
					if (i++ < bindingTemplates.getBindingTemplate().size() - 1)
						sb.append(", ");
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
