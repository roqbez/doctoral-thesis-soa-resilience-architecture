package br.ufsc.gsigma.binding.context;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;

import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.binding.util.BindingServiceConstants;
import br.ufsc.gsigma.infrastructure.util.CompressionUtil;
import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBStringMapAttributeAdapter;
import br.ufsc.services.core.util.json.JsonUtil;

@XmlRootElement(namespace = BindingServiceConstants.WS_NAMESPACE, name = BindingServiceConstants.WS_REQUEST_BINDING_CONTEXT_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestBindingContext implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public static final String NAMESPACE = BindingServiceConstants.WS_NAMESPACE;

	public static final String NAME = BindingServiceConstants.WS_REQUEST_BINDING_CONTEXT_NAME;

	public static final QName QNAME = new QName(NAMESPACE, NAME);

	@XmlAttribute
	private String token;

	@XmlAttribute
	private String applicationId;

	@XmlAttribute
	private String portTypeNS;

	@XmlAttribute
	private String portTypeName;

	@XmlAttribute
	private String processNS;

	@XmlAttribute
	private String processName;

	@XmlAttribute
	private String processInstanceId;

	@XmlAttribute
	private String processURL;

	@XmlAttribute
	private long originalBindingEngineConfigurationVersion;

	@XmlAttribute
	private String requestCorrelationId;

	private InvokedServiceInfo invokedServiceInfo;

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> attributes = new LinkedHashMap<String, String>();

	@Override
	public Object clone() throws CloneNotSupportedException {
		RequestBindingContext clone = new RequestBindingContext();

		clone.token = token;
		clone.applicationId = applicationId;
		clone.portTypeNS = portTypeNS;
		clone.processNS = processNS;
		clone.processName = processName;
		clone.processInstanceId = processInstanceId;
		clone.processURL = processURL;
		clone.originalBindingEngineConfigurationVersion = originalBindingEngineConfigurationVersion;
		clone.invokedServiceInfo = invokedServiceInfo;
		clone.requestCorrelationId = requestCorrelationId;

		for (Entry<String, String> e : attributes.entrySet()) {
			clone.attributes.put(e.getKey(), e.getValue());
		}
		return clone;
	}

	public static void set(RequestBindingContext requestBindingContext) {
		ThreadLocalHolder.getThreadLocalMap().put(RequestBindingContext.class.getName(), requestBindingContext);
	}

	public void set() {
		RequestBindingContext.set(this);
	}

	public static RequestBindingContext get() {
		return get(true);
	}

	public static RequestBindingContext get(boolean create) {

		RequestBindingContext requestBindingContext = (RequestBindingContext) ThreadLocalHolder.getThreadLocalMap().get(RequestBindingContext.class.getName());

		if (create && requestBindingContext == null) {
			requestBindingContext = new RequestBindingContext();
			requestBindingContext.set();
		}

		return requestBindingContext;
	}

	public String toBase64() throws Exception {
		return Base64.encodeBase64URLSafeString(CompressionUtil.compressByteArray(JsonUtil.encode(this).getBytes(StandardCharsets.UTF_8)));
	}

	public static RequestBindingContext fromBase64(String str) throws Exception {
		return (RequestBindingContext) JsonUtil.getValue(CompressionUtil.uncompressByteArray(Base64.decodeBase64(str)), RequestBindingContext.class);
	}

	public String getRequestCorrelationId() {
		return requestCorrelationId;
	}

	public void setRequestCorrelationId(String requestCorrelationId) {
		this.requestCorrelationId = requestCorrelationId;
	}

	public String getPortTypeNS() {
		return portTypeNS;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setPortTypeNS(String portTypeNS) {
		this.portTypeNS = portTypeNS;
	}

	public String getPortTypeName() {
		return portTypeName;
	}

	public void setPortTypeName(String portTypeName) {
		this.portTypeName = portTypeName;
	}

	public String getProcessNS() {
		return processNS;
	}

	public void setProcessNS(String processNS) {
		this.processNS = processNS;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessURL() {
		return processURL;
	}

	public void setProcessURL(String processURL) {
		this.processURL = processURL;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public InvokedServiceInfo getInvokedServiceInfo() {
		return invokedServiceInfo;
	}

	public void setInvokedServiceInfo(InvokedServiceInfo invokedServiceInfo) {
		this.invokedServiceInfo = invokedServiceInfo;
	}

	public long getOriginalBindingEngineConfigurationVersion() {
		return originalBindingEngineConfigurationVersion;
	}

	public void setOriginalBindingEngineConfigurationVersion(long originalBindingEngineConfigurationVersion) {
		this.originalBindingEngineConfigurationVersion = originalBindingEngineConfigurationVersion;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (originalBindingEngineConfigurationVersion ^ (originalBindingEngineConfigurationVersion >>> 32));
		result = prime * result + ((portTypeNS == null) ? 0 : portTypeNS.hashCode());
		result = prime * result + ((portTypeName == null) ? 0 : portTypeName.hashCode());
		result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
		result = prime * result + ((processNS == null) ? 0 : processNS.hashCode());
		result = prime * result + ((processName == null) ? 0 : processName.hashCode());
		result = prime * result + ((processURL == null) ? 0 : processURL.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

}
