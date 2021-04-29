package br.ufsc.gsigma.infrastructure.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public abstract class PayloadConverterUtil {

	public static byte[] xmlNodeToJson(Node xml) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(xml), new StreamResult(baos));

		byte[] inputXml = baos.toByteArray();

		XmlMapper xmlMapper = new XmlMapper();
		JsonNode node = xmlMapper.readTree(inputXml);
		ObjectMapper jsonMapper = new ObjectMapper();
		return jsonMapper.writeValueAsBytes(node);
	}

	public static Node jsonStreamToNode(InputStream is) throws Exception {

		ObjectMapper jsonMapper = new ObjectMapper();
		JsonNode node = jsonMapper.readTree(is);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.newDocument();

		DOMResult result = new DOMResult(doc);

		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		xmlFactory.createXMLStreamWriter(result);

		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.writeValue(xmlFactory.createXMLStreamWriter(result), node);

		return result.getNode().getFirstChild();
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
