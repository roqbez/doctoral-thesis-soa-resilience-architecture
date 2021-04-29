package br.ufsc.gsigma.infrastructure.ws.context;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.math.NumberUtils;

import br.ufsc.gsigma.infrastructure.util.CompressionUtil;
import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBBooleanMapAttributeAdapter;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBStringMapAttributeAdapter;
import br.ufsc.services.core.util.json.JsonUtil;

@XmlRootElement(namespace = ExecutionContext.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ExecutionContext implements Serializable, Cloneable {

	public static final String NAMESPACE = "http://gsigma.ufsc.br/execution";

	public static final String NAME = "executionContext";

	public static final QName QNAME = new QName(NAMESPACE, NAME);

	private static final long serialVersionUID = 1L;

	private String executionId;

	private int timestamp;

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> attributes = new LinkedHashMap<String, String>();

	@XmlJavaTypeAdapter(JAXBStringMapAttributeAdapter.class)
	private Map<String, String> transientAttributes = new LinkedHashMap<String, String>();

	@XmlJavaTypeAdapter(JAXBBooleanMapAttributeAdapter.class)
	private Map<String, Boolean> flags = new LinkedHashMap<String, Boolean>();

	@XmlTransient
	private Set<String> notPropagatedTransientAttributesKeys = new HashSet<String>();

	@Override
	public Object clone() throws CloneNotSupportedException {
		ExecutionContext clone = new ExecutionContext();
		clone.executionId = executionId;
		clone.timestamp = timestamp;

		clone.flags = new LinkedHashMap<String, Boolean>(flags);
		clone.attributes = new LinkedHashMap<String, String>(attributes);

		for (Entry<String, String> e : transientAttributes.entrySet()) {
			if (notPropagatedTransientAttributesKeys.contains(e.getKey()))
				clone.transientAttributes.put(e.getKey(), e.getValue());
		}

		return clone;
	}

	public String toBase64() throws Exception {

		ExecutionContext executionContext = (ExecutionContext) clone();

		for (String key : new ArrayList<String>(executionContext.attributes.keySet())) {
			executionContext.attributes.put(key, decodeCompressedAtt(executionContext.attributes.get(key)));
		}

		return Base64.encodeBase64URLSafeString(CompressionUtil.compressByteArray(JsonUtil.encode(executionContext).getBytes(StandardCharsets.UTF_8)));
	}

	public static ExecutionContext fromBase64(String str) throws Exception {
		return (ExecutionContext) JsonUtil.getValue(CompressionUtil.uncompressByteArray(Base64.decodeBase64(str)), ExecutionContext.class);
	}

	public static Boolean getFlagValue(String flag) {
		ExecutionContext executionContext = get();
		if (executionContext != null) {

			Boolean v = getFlagValue(executionContext, flag);
			return v != null ? v : null;
		}
		return null;
	}

	public static Boolean getFlagValue(ExecutionContext executionContext, String flag) {
		if (executionContext == null)
			return null;
		return executionContext.getFlags().get(flag);
	}

	public static <T> T getAttribute(String name, Class<T> type, T defaultValue) {
		ExecutionContext executionContext = get();
		return getAttribute(executionContext, name, type, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(ExecutionContext executionContext, String name, Class<T> type, T defaultValue) {
		if (executionContext != null) {

			String s = executionContext.getAttribute(name);

			if (s != null) {
				if (Boolean.class.isAssignableFrom(type))
					return (T) Boolean.valueOf(s);
				else if (Integer.class.isAssignableFrom(type))
					return (T) Integer.valueOf(s);
				else if (Long.class.isAssignableFrom(type))
					return (T) Long.valueOf(s);
				else if (String.class.isAssignableFrom(type))
					return (T) s;
				else
					return executionContext.getAttribute(name, type);
			}
		}
		return defaultValue;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int incrementTimestamp() {
		return ++timestamp;
	}

	public void removeAttribute(String key) {

		if (attributes != null) {
			attributes.remove(key);
		}
		notPropagatedTransientAttributesKeys.remove(key);
	}

	public void addAttribute(String key, String value) {

		if (attributes == null)
			attributes = new LinkedHashMap<String, String>();

		attributes.put(key, value);

		notPropagatedTransientAttributesKeys.add(key);
	}

	public void addAttribute(String key, Object value) {
		if (attributes == null)
			attributes = new LinkedHashMap<String, String>();

		if (value instanceof String) {
			attributes.put(key, (String) value);
		} else {

			try {
				String json = JsonUtil.encode(value);

				byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

				byte[] compressedBytes = CompressionUtil.compressByteArray(jsonBytes);

				String v = Base64.encodeBase64URLSafeString(compressedBytes);

				attributes.put(key, v);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getAttribute(String key) {
		if (attributes != null)
			return attributes.get(key);
		else
			return null;
	}

	public <T> T getAttribute(String key, Class<T> clazz) {

		String s = getAttribute(key);

		return getAttributeValue(clazz, s);
	}

	@SuppressWarnings("unchecked")
	private <T> T getAttributeValue(Class<T> clazz, String s) {

		s = decodeCompressedAtt(s);

		if (String.class.isAssignableFrom(clazz)) {
			return (T) s;
		}

		if (JsonUtil.isJson(s)) {
			return (T) JsonUtil.decode(s, clazz);
		}

		return null;
	}

	private String decodeCompressedAtt(String s) {
		if (s != null && Base64.isBase64(s) && !NumberUtils.isDigits(s)) {

			byte[] bytes = Base64.decodeBase64(s);

			if (CompressionUtil.isByteArrayCompressed(bytes)) {
				try {
					bytes = CompressionUtil.uncompressByteArray(bytes);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			s = new String(bytes, StandardCharsets.UTF_8);
		}
		return s;
	}

	public <T> Map<String, T> getAttributes(String prefix, Class<T> type) {

		Map<String, T> result = new LinkedHashMap<String, T>();

		for (Entry<String, String> e : getAttributes().entrySet()) {

			String key = e.getKey();

			if (key.startsWith(prefix)) {
				result.put(key, getAttributeValue(type, e.getValue()));
			}
		}
		return result;
	}

	public void addTransientAttribute(String key, String value) {
		if (transientAttributes == null)
			transientAttributes = new LinkedHashMap<String, String>();

		transientAttributes.put(key, value);
	}

	public void addTransientAttribute(String key, Object value) {
		if (transientAttributes == null)
			transientAttributes = new LinkedHashMap<String, String>();

		transientAttributes.put(key, value instanceof String ? (String) value : JsonUtil.encode(value));
	}

	public String getTransientAttribute(String key) {
		if (transientAttributes != null)
			return transientAttributes.get(key);
		else
			return null;
	}

	public <T> T getTransientAttribute(String key, Class<T> clazz) {

		String s = getTransientAttribute(key);

		return getAttributeValue(clazz, s);
	}

	public Map<String, String> getAttributes() {
		return attributes != null ? Collections.unmodifiableMap(attributes) : null;
	}

	public Map<String, String> getTransientAttributes() {
		return transientAttributes != null ? Collections.unmodifiableMap(transientAttributes) : null;
	}

	public Map<String, Boolean> getFlags() {
		return flags;
	}

	public static ExecutionContext get() {
		return get(false);
	}

	public static ExecutionContext get(boolean create) {

		ExecutionContext executionContext = (ExecutionContext) ThreadLocalHolder.getThreadLocalMap().get(ExecutionContext.class.getName());

		if (executionContext == null && create) {
			executionContext = new ExecutionContext();
			executionContext.set();
		}

		return executionContext;
	}

	public static void set(ExecutionContext executionContext) {
		ThreadLocalHolder.getThreadLocalMap().put(ExecutionContext.class.getName(), executionContext);
	}

	public void set() {
		ExecutionContext.set(this);
	}

	public void remove() {
		ThreadLocalHolder.clearThreadLocal();
	}

	public HTTPRequestInfo getHTTPRequestInfo() {
		return getAttribute(ExecutionAttributes.ATT_HTTP_REQUEST_INFO, HTTPRequestInfo.class);
	}

}
