package br.ufsc.gsigma.infrastructure.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class HostAddressUtil {

	public static void main(String[] args) {
		System.out.println(getPublicIp());
	}

	public static String getPublicIp() {

		try {

			HttpURLConnection conn = (HttpURLConnection) new URL("http://ws.ufsc.br/ip").openConnection();

			conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			conn.setRequestMethod("GET");

			int respCode = conn.getResponseCode();

			String response;

			InputStream in = respCode == 200 ? conn.getInputStream() : conn.getErrorStream();

			try {
				response = toString(in, StandardCharsets.UTF_8);
			} finally {
				in.close();
			}

			if (respCode != 200) {
				throw new Exception("Error executing request: " + respCode + " - " + conn.getResponseMessage());
			}

			return response;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static long copy(InputStream input, OutputStream output) throws IOException {
		long count = 0;
		int n;
		byte[] buffer = new byte[4096];
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	private static String toString(final InputStream input, final Charset encoding) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(input, baos);
		return new String(baos.toByteArray(), encoding);
	}
}
