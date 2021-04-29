package br.ufsc.gsigma.infrastructure.ws.context;

import java.io.Serializable;

public class HTTPRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private HTTPRequestInfo propagatedRequestInfo;

	private String method;

	private String scheme;

	private String contextPath;

	private String servletPath;

	private String serverName;

	private int serverPort;

	private String remoteAddr;

	private String pathInfo;

	private String requestURI;

	private String requestURL;

	private String queryString;

	private boolean secure;

	public boolean isGetMethod() {
		return "GET".equals(method);
	}

	public boolean isPostMethod() {
		return "POST".equals(method);
	}

	public String getFullURL() {

		String servletPath = getServletPath();
		String pathInfo = getPathInfo();
		String queryString = getQueryString();

		StringBuffer path = new StringBuffer();

		path.append(servletPath);

		if (pathInfo != null) {
			path.append(pathInfo);
		}
		if (queryString != null) {
			path.append("?").append(queryString);
		}
		return getFullURL(path.toString());

	}

	public String getServletFullURL() {
		return getFullURL(getServletPath());
	}

	public String getFullURL(String path) {

		String scheme = getScheme();
		String serverName = getServerName();
		int serverPort = getServerPort();
		String contextPath = getContextPath();

		StringBuffer url = new StringBuffer();
		url.append(scheme).append("://").append(serverName);

		if ((serverPort != 80) && (serverPort != 443)) {
			url.append(":").append(serverPort);
		}

		if (contextPath != null)
			url.append(contextPath);

		url.append(path);

		return url.toString();

	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public HTTPRequestInfo getPropagatedRequestInfo() {
		return propagatedRequestInfo;
	}

	public void setPropagatedRequestInfo(HTTPRequestInfo propagatedRequestInfo) {
		this.propagatedRequestInfo = propagatedRequestInfo;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

}
