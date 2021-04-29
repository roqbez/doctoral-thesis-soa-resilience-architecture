package br.ufsc.gsigma.servicediscovery.indexing;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.support.QoSMinMax;
import br.ufsc.gsigma.servicediscovery.support.ServiceQoSUtil;

public class QoSCapableServiceScoreProvider extends CustomScoreProvider {

	private static final Logger logger = LoggerFactory.getLogger(QoSCapableServiceScoreProvider.class);

	private static final Set<String> USED_FIELDS = new HashSet<String>();

	static {
		USED_FIELDS.add("providedId");
		USED_FIELDS.add("serviceClassification");
	}

	private QoSCapableServiceQuery query;

	public QoSCapableServiceScoreProvider(LeafReaderContext context, QoSCapableServiceQuery query) {
		super(context);
		this.query = query;
	}

	public float customScore(int docId, float subQueryScore, float valSrcScores[]) throws IOException {

		final QoSMinMax qoSMinMax = query.getQoSMinMax();
		final Map<String, Double> qoSWeights = query.getQoSWeights();

		IndexReader r = context.reader();

		DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor(USED_FIELDS) {
			@Override
			public Status needsField(FieldInfo fieldInfo) throws IOException {
				Status s = super.needsField(fieldInfo);
				if (s == Status.NO)
					s = fieldInfo.name.startsWith("QoS") ? Status.YES : Status.NO;
				return s;
			}
		};

		r.document(docId, visitor);

		Document doc = visitor.getDocument();

		String id = doc.get("providedId").substring(2);
		String serviceClassification = doc.get("serviceClassification");

		double utility = 0;

		for (IndexableField f : doc.getFields()) {

			if (f.name().startsWith("QoS")) {

				String qoSKey = f.name().substring(4);
				double qoSValue = (double) f.numericValue();

				QoSAttribute qoSAtt = query.getQoSAttributes().get(qoSKey);

				Double qMax = qoSMinMax.getQoSMaxValue(serviceClassification, qoSKey);
				Double qMin = qoSMinMax.getQoSMinValue(serviceClassification, qoSKey);

				Double globalQoSDelta = qoSMinMax.getGlobalQoSDeltaValue(qoSKey);

				Double u = ServiceQoSUtil.calculateQoSAttributeUtility(qoSAtt, qoSValue, qMax, qMin, globalQoSDelta, qoSWeights);

				if (u != null)
					utility += u;

			}
		}

		this.query.getDocUtility().put(id, utility);

		float fUtility = (float) utility;

		if (logger.isTraceEnabled()) {
			logger.trace("Utility of document docId=" + id + " is " + fUtility);
		}

		return fUtility;

	}
}