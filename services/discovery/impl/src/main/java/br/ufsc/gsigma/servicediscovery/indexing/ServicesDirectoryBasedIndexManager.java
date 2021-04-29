package br.ufsc.gsigma.servicediscovery.indexing;

import java.util.Properties;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.hibernate.search.backend.spi.LuceneIndexingParameters;
import org.hibernate.search.indexes.spi.DirectoryBasedIndexManager;
import org.hibernate.search.spi.WorkerBuildContext;

public class ServicesDirectoryBasedIndexManager extends DirectoryBasedIndexManager {

	private LuceneIndexingParameters indexingParameters;

	@Override
	public void initialize(String indexName, Properties properties, Similarity similarity, WorkerBuildContext buildContext) {
		
		this.indexingParameters = new LuceneIndexingParameters(properties) {
			private static final long serialVersionUID = -1182908290679221304L;

			@Override
			public void applyToWriter(IndexWriterConfig writerConfig) {
				super.applyToWriter(writerConfig);
				writerConfig.setCodec(new NoCompressionLucene54Codec());
			}
		};
		
		super.initialize(indexName, properties, similarity, buildContext);
	}

	@Override
	public LuceneIndexingParameters getIndexingParameters() {
		return indexingParameters;
	}

}
