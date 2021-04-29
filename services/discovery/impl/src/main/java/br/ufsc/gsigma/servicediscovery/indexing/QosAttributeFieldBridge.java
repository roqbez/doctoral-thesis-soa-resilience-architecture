package br.ufsc.gsigma.servicediscovery.indexing;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;

public class QosAttributeFieldBridge implements FieldBridge {

	public static final FieldType TYPE_QOS_ATTRIBUTE = new FieldType();
	static {
		TYPE_QOS_ATTRIBUTE.setTokenized(false);
		TYPE_QOS_ATTRIBUTE.setOmitNorms(true);
		TYPE_QOS_ATTRIBUTE.setIndexOptions(IndexOptions.DOCS);
		TYPE_QOS_ATTRIBUTE.setNumericType(FieldType.NumericType.DOUBLE);
		TYPE_QOS_ATTRIBUTE.setStored(true);
		TYPE_QOS_ATTRIBUTE.freeze();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

		Collection<ServiceQoSInformationItem> qoSItens = (Collection<ServiceQoSInformationItem>) value;

		for (ServiceQoSInformationItem q : qoSItens) {
			String fieldName = "QoS." + q.getQoSItemName() + "." + q.getQoSAttributeName();
			DoubleField field = new DoubleField(fieldName, q.getQoSValue(), TYPE_QOS_ATTRIBUTE);
			document.add(field);
		}
	}
}
