package testSolr;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CollectionReader {

	private String pathIn, pathOut, str;
	private List<File> files;
	private Stream<String> lines;
	private Document doc;
	private HashMap<String, String> myLinkedMap;

	public CollectionReader(String pathCollectionToRead_, String pathXMLCollectionToWrite_)
			throws URISyntaxException, IOException {
		this.pathIn = pathCollectionToRead_;
		this.pathOut = pathXMLCollectionToWrite_;
	}

	public void initFileStreamsForAllFiles() throws URISyntaxException, IOException, XPathExpressionException,
			SAXException, ParserConfigurationException {
		// files = new ArrayList<File>();

		File f = new File(pathIn);
		URI uri = f.toURI();
		files = Files.walk(Paths.get(uri)).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());

		for (File fi : files) {

			System.out.println(fi.toString());

			// parse(fi);
			str = "";
			lines = Files.lines(fi.toPath(), Charset.defaultCharset());
			lines.forEach(line -> {
				str += line + "\n";
				if (line.contentEquals("</DOC>")) {
					System.out.println(str);
					try {
						doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(new InputSource(new StringReader(str)));
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					doc.normalize();
					fillMap(doc.getDocumentElement());
					try {
						createXML(myLinkedMap);
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					str = "";

				}
				// System.out.println(line.toString());
			});

		}
	}

	private void fillMap(Node node) {
		myLinkedMap = new HashMap<String, String>();
		iteration(node);
	}

	private void iteration(Node node) {
		filter(node);
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				iteration(currentNode);
			}
		}

	}

	private void filter(Node node) {
		if (node.getNodeName().equals("DOCID")) {
			myLinkedMap.put(node.getNodeName(), node.getTextContent());

		} else if (node.getNodeName().equals("DOCNO")) {
			myLinkedMap.put(node.getNodeName(), node.getTextContent());

		} else if (node.getNodeName().equals("WEEK")) {
			myLinkedMap.put(node.getNodeName(), node.getTextContent());

		} else if (node.getNodeName().equals("TITLE")) {
			if (myLinkedMap.containsKey(node.getNodeName())) {
				String temp = myLinkedMap.get(node.getNodeName());
				temp += " " + node.getTextContent();
				myLinkedMap.put(node.getNodeName(), temp);
			} else {
				myLinkedMap.put(node.getNodeName(), node.getTextContent());
			}
		} else if (node.getNodeName().equals("TEXT")) {
			myLinkedMap.put(node.getNodeName(), node.getTextContent());

		}
	}

	private void createXML(HashMap<String, String> myLinkedMap)
			throws ParserConfigurationException, TransformerException {

		String newDocumentName = "";

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("add");

		doc.appendChild(rootElement);

		Element document = doc.createElement("doc");
		rootElement.appendChild(document);

		for (Map.Entry<String, String> entry : myLinkedMap.entrySet()) {
			if (entry.getKey().equals("DOCID")) {
				newDocumentName = entry.getValue();
			}

			Element field = doc.createElement("field");
			document.appendChild(field);
			Attr attr = doc.createAttribute("name");
			attr.setValue(entry.getKey());
			field.setAttributeNode(attr);
			field.appendChild(doc.createTextNode(entry.getValue()));
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(pathOut + newDocumentName + ".xml"));

		transformer.transform(source, result);

	}

}
