package testSolr;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class QueryReader {

	private String path, str;
	private LinkedList<HashMap<String, String>> queryMap = null;
	private Stream<String> lines;
	private Document doc;
	private HashMap<String, String> tempHashMap = null;
	
	
	public QueryReader(String path_){
		this.path = path_;
		queryMap = new LinkedList<HashMap<String, String>>();
	}
	
	public LinkedList<HashMap<String, String>> getQueries() throws IOException{
		
		File f = new File(path);
		str = "";
		lines = Files.lines(f.toPath(), Charset.defaultCharset());
		lines.forEach(line -> {
			str += line + "\n";
			if (line.contentEquals("</top>")) {
//				System.out.println(str);
				tempHashMap = new HashMap<String, String>();
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
				iteration(doc.getDocumentElement());
				queryMap.add(tempHashMap);				
				str = "";

			}
			// System.out.println(line.toString());
		});
		return queryMap;
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
		if (node.getNodeName().equals("num")) {
			tempHashMap.put(node.getNodeName(), node.getTextContent());

		} else if (node.getNodeName().equals("DE-title")) {
			tempHashMap.put(node.getNodeName(), node.getTextContent());

		} else if (node.getNodeName().equals("DE-desc")) {
			tempHashMap.put(node.getNodeName(), node.getTextContent());

		} else if (node.getNodeName().equals("DE-narr")) {
			tempHashMap.put(node.getNodeName(), node.getTextContent());
			
		}
	}
}
