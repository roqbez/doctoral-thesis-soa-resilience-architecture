package br.ufsc.gsigma.services.specifications.ubl.tests;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import br.ufsc.gsigma.processcontext.ProcessContext;
import oasis.names.specification.ubl.schema.xsd.order_2.OrderType;
import services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder.PlaceOrderAsyncCallback;

public class MarshalTest {

	public static void main(String[] args) throws Exception {

		PlaceOrderAsyncCallback callback = new PlaceOrderAsyncCallback();

		ProcessContext processContext = new ProcessContext();
		processContext.setProcessId("7");
		processContext.setCallbackEndpoint("http://bindingservice.d-201603244.ufsc.br:9000/service");
		processContext.setCallbackSOAPAction("http://my.ubl.oasis.services/orderingprocessve_persistent/callbacks/ubl_orderingprocess_buyerparty_placeorder1");

		callback.setProcessContext(processContext);

		JAXBContext jbc = JAXBContext.newInstance(callback.getClass(), OrderType.class);

		Marshaller m = jbc.createMarshaller();

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document requestDoc = db.newDocument();

		m.marshal(callback, requestDoc);

		System.out.println(xmlNodeToString(requestDoc.getDocumentElement()));

		// String xml = PayloadConverterUtil.xmlNodeToString(requestDoc);
		//
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//
		// JAXBSerializerUtil.write(callback.getOutput(), baos, true);

		// System.out.println(new String(baos.toByteArray()));

	}

	public static String xmlNodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
		}
		return sw.toString();
	}
}
