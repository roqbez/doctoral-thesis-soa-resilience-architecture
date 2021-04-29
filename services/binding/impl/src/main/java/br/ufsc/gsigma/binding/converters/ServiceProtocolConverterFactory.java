package br.ufsc.gsigma.binding.converters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

public abstract class ServiceProtocolConverterFactory {

	private static List<String> PROTOCOL_NAMES = Arrays.asList( //
			SoapServiceProtocolConverter.class.getName(), //
			JsonServiceProtocolConverter.class.getName() //
	);

	private static ConcurrentHashMap<String, ServiceProtocolConverter> converters = new ConcurrentHashMap<String, ServiceProtocolConverter>();

	public static ServiceProtocolConverter get(String clazz) {

		if (StringUtils.isEmpty(clazz)) {
			return null;
		}

		return converters.computeIfAbsent(clazz, (key) -> {
			try {
				return (ServiceProtocolConverter) Class.forName(key).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static List<String> getServiceProtocolConverterNames() {
		return PROTOCOL_NAMES;
	}

}
