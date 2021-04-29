package br.ufsc.gsigma.services.execution.bpel.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Teste {

	public static void main(String[] args) throws IOException {
		System.out.println(encodeFileToBase64Binary("d:/PaymentProcess.zip"));
	}

	private static String encodeFileToBase64Binary(String fileName) throws IOException {

		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		return org.apache.axiom.om.util.Base64.encode(bytes);
	}

	private static byte[] loadFile(File file) throws IOException {

		try (InputStream is = new FileInputStream(file)) {

			long length = file.length();
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}
			byte[] bytes = new byte[(int) length];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}

			return bytes;
		}
	}

}
