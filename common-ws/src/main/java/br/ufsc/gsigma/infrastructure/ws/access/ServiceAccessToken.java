package br.ufsc.gsigma.infrastructure.ws.access;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.cxf.phase.PhaseInterceptorChain;

import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceAccessToken", namespace = "http://gsigma.ufsc.br")
public class ServiceAccessToken implements Serializable {

	private static final long serialVersionUID = 1L;

	private String token;

	private String tenantId;

	private ServiceAccessRequest request;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public ServiceAccessRequest getRequest() {
		return request;
	}

	public void setRequest(ServiceAccessRequest request) {
		this.request = request;
	}

	public void set() {
		ThreadLocalHolder.getThreadLocalMap().put(ServiceAccessToken.class.getName(), this);
	}

	public static ServiceAccessToken get() {

		ServiceAccessToken resp = (ServiceAccessToken) ThreadLocalHolder.getThreadLocalMap().get(ServiceAccessToken.class.getName());

		if (resp == null) {
			resp = (ServiceAccessToken) PhaseInterceptorChain.getCurrentMessage().get(ServiceAccessToken.class.getName());
		}

		return resp;
	}

	public static ServiceAccessToken remove() {
		return (ServiceAccessToken) ThreadLocalHolder.getThreadLocalMap().remove(ServiceAccessToken.class.getName());
	}

	@Override
	public String toString() {
		return "ServiceAccessToken [clientId=" + request.getClientId() + ", tenantId=" + tenantId + "]";
	}

}
