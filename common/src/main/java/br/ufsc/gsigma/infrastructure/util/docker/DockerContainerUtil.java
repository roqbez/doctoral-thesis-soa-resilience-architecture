package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public abstract class DockerContainerUtil {

	public static boolean isRunningInContainer() {
		return getContainerId() != null;
	}

	public static String getContainerClientId() {
		return isRunningInContainer() ? getContainerHostname().trim().replaceAll("-", "") : null;
	}

	public static String getContainerId() {

		try {

			Process p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "awk -F/ '/docker/{print $NF;exit;}' /proc/self/cgroup" });
			p.waitFor();

			try (InputStream in = p.getInputStream()) {
				return IOUtils.toString(in, "UTF-8");
			}

		} catch (Exception e) {
			return null;
		}
	}

	public static String getContainerHostname() {

		try {

			Process p = Runtime.getRuntime().exec("hostname");
			p.waitFor();

			try (InputStream in = p.getInputStream()) {
				return IOUtils.toString(in, "UTF-8");
			}

		} catch (Exception e) {
			return null;
		}
	}

}
