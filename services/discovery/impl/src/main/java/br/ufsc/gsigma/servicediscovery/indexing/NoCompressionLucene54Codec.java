package br.ufsc.gsigma.servicediscovery.indexing;

import java.io.IOException;

import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.StoredFieldsWriter;
import org.apache.lucene.codecs.compressing.CompressingStoredFieldsFormat;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.compressing.Compressor;
import org.apache.lucene.codecs.compressing.Decompressor;
import org.apache.lucene.codecs.lucene54.Lucene54Codec;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.util.BytesRef;

public class NoCompressionLucene54Codec extends FilterCodec {

	public static final CompressionMode NO_COMPRESSION = new CompressionMode() {

		@Override
		public Compressor newCompressor() {
			return new Compressor() {
				@Override
				public void compress(byte[] bytes, int off, int len, DataOutput out) throws IOException {
					out.writeBytes(bytes, off, len);
				}
			};
		}

		@Override
		public Decompressor newDecompressor() {
			return new Decompressor() {
				@Override
				public void decompress(DataInput in, int originalLength, int offset, int length, BytesRef bytes) throws IOException {
					bytes.bytes = new byte[offset + length];
					in.readBytes(bytes.bytes, 0, offset + length);
					bytes.offset = offset;
					bytes.length = length;
				}

				@Override
				public Decompressor clone() {
					return this;
				}
			};
		}

		@Override
		public String toString() {
			return "NO_COMPRESSION";
		}

	};

	private final StoredFieldsFormat storedFieldsFormat;

	public NoCompressionLucene54Codec() {
		super("NoCompressionLucene54Codec", new Lucene54Codec());
		this.storedFieldsFormat = new NoCompressionLuceneStoredFieldsFormat();
	}

	@Override
	public StoredFieldsFormat storedFieldsFormat() {
		return storedFieldsFormat;
	}

	class NoCompressionLuceneStoredFieldsFormat extends StoredFieldsFormat {

		@Override
		public StoredFieldsReader fieldsReader(Directory directory, SegmentInfo si, FieldInfos fn, IOContext context) throws IOException {
			return impl().fieldsReader(directory, si, fn, context);
		}

		@Override
		public StoredFieldsWriter fieldsWriter(Directory directory, SegmentInfo si, IOContext context) throws IOException {
			return impl().fieldsWriter(directory, si, context);
		}

		StoredFieldsFormat impl() {
			return new CompressingStoredFieldsFormat("NoCompressionLucene50StoredFields", NO_COMPRESSION, 1 << 14, 128, 1024);
		}
	}
}
