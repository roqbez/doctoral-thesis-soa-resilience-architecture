package br.ufsc.gsigma.infrastructure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CompressionUtil {

	private static final Logger logger = LoggerFactory.getLogger(CompressionUtil.class);

	public static boolean isByteArrayCompressed(byte[] bytes) {
		int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
		return (GZIPInputStream.GZIP_MAGIC == head);
	}

	public static byte[] uncompressByteArray(byte[] bytes) throws IOException {
		return uncompressByteArray(bytes, false);
	}

	public static byte[] uncompressByteArray(byte[] bytes, boolean ignoreLastByte) throws IOException {

		ByteArrayInputStream bais = null;
		GZIPInputStream gzis = null;
		ByteArrayOutputStream baos = null;

		try {
			bais = new ByteArrayInputStream(bytes, 0, ignoreLastByte ? bytes.length - 1 : bytes.length);
			gzis = new GZIPInputStream(bais);
			baos = new ByteArrayOutputStream(32768);

			IOUtils.copy(gzis, baos);
			gzis.close();
			bais.close();

			byte[] uncompressedByteArray = baos.toByteArray();
			baos.close();

			return uncompressedByteArray;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(gzis);
			IOUtils.closeQuietly(bais);
			IOUtils.closeQuietly(baos);
		}
	}

	public static byte[] compressByteArray(byte[] uncompressed) throws IOException {
		return compressByteArray(uncompressed, false);
	}

	public static byte[] compressByteArray(byte[] uncompressed, boolean appendAvoidTruncationByte) throws IOException {

		ByteArrayOutputStream baos = null;
		GZIPOutputStream gzos = null;

		try {
			baos = new ByteArrayOutputStream(32768);
			gzos = getGZIPOutputStream(baos);

			gzos.write(uncompressed, 0, uncompressed.length);
			gzos.close();

			if (appendAvoidTruncationByte)
				baos.write(1);

			byte[] compressed = baos.toByteArray();

			if (logger.isDebugEnabled()) {
				try {
					float percent = (1 - ((float) compressed.length / uncompressed.length)) * 100;
					logger.debug("[compressByteArray] - original: " + uncompressed.length + " comprimido: " + compressed.length + " (" + percent + ")%");
				} catch (Throwable ex) {
				}
			}

			return compressed;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(gzos);
			IOUtils.closeQuietly(baos);
		}
	}

	private static GZIPOutputStream getGZIPOutputStream(OutputStream out) throws IOException {

		return new GZIPOutputStream(out) {
			{
				def.setLevel(Deflater.BEST_COMPRESSION);
			}
		};

	}
}
