package br.ufsc.gsigma.architecture.experiments.util;

import java.io.File;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParserScripts {

	public static void main(String[] args) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();

		XPathFactory xPathfactory = XPathFactory.newInstance();

		XPath xpath = xPathfactory.newXPath();

		Collection<File> list = FileUtils.listFiles(new File("src/main/resources/br/ufsc/gsigma/architecture/experiments/files/thesis1"), new String[] { "xml" }, false);

		for (File f : list) {

			if (f.getName().startsWith("deploymentService")) {

				Document doc = builder.parse(f);

				NodeList nodes = (NodeList) xpath.compile("//task").evaluate(doc, XPathConstants.NODESET);

				for (int i = 0; i < nodes.getLength(); i++) {
					Element elm = (Element) nodes.item(i);

					Element processNode = (Element) elm.getParentNode().getParentNode();

					String processName = processNode.getElementsByTagName("name").item(0).getTextContent();

					String taskName = elm.getElementsByTagName("name").item(0).getTextContent();

					String participantName = ((Element) ((Element) ((Element) elm.getElementsByTagName("participants").item(0)).getElementsByTagName("taskParticipant").item(0)).getElementsByTagName("participant").item(0)).getElementsByTagName("name").item(0).getTextContent();

					if ("Partner 1 - Coordinator".equals(participantName)) {
						participantName = "Partner 1";
					} else if ("Customer".equals(participantName)) {
						participantName = "Partner 4";
					} else if ("UBL Accounting Customer".equals(participantName)) {
						participantName = "Partner 3";
					} else if ("UBL Accounting Supplier".equals(participantName)) {
						participantName = "Partner 2";
					} else if ("UBL Payee Party".equals(participantName)) {
						participantName = "Partner 1";
					}

					System.out.println(processName + "\t" + taskName + "\t" + participantName);
				}

			}
		}

	}

}
