package br.ufsc.gsigma.infrastructure.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import br.ufsc.services.core.util.json.JsonUtil;

public abstract class JsonEncodeUtil {

	public static String encode(Map<String, Object> attrs) {

		if (attrs != null && !attrs.isEmpty()) {

			try {

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				try (OutputStream os = out) {
					JsonUtil.writeJsonStream(os, attrs);
				}

				return Base64.encodeBase64URLSafeString(CompressionUtil.compressByteArray(out.toByteArray()));

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> decode(String str) {
		return decode(str, Map.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T decode(String str, Class<T> clazz) {

		try {

			if (!JsonUtil.isJsonObject(str)) {
				byte[] bytes = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
				if (CompressionUtil.isByteArrayCompressed(bytes)) {
					bytes = CompressionUtil.uncompressByteArray(bytes);
				}
				return (T) JsonUtil.getValue(bytes, clazz);
			} else {
				return (T) JsonUtil.decode(str, clazz);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
