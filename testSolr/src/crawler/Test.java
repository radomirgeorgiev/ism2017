package crawler;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.ContentStreamBase.StringStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Test {

	
	private static final String URL = "http://localhost:8983/solr/webcrawler";
	private static final String FOLDER = "C://test//";
	private static SolrInputDocument solrDoc;
	private static HttpSolrClient solrClient;
	private static List<File> files;
	private static List<String> documentText;
	private static Document doc;
	private static Stream<String> lines;
	
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, SolrServerException {
		// TODO Auto-generated method stub

		createCollection();
		System.out.println("Fertisch!");
	}
	

	public static void createCollection() throws IOException, SAXException, ParserConfigurationException, SolrServerException {
		solrClient = new HttpSolrClient.Builder(URL).build();		
		initialize();
		
	}

	private static void initialize() throws IOException, SAXException, ParserConfigurationException, SolrServerException {
		int counter = 0;
		File f = new File(FOLDER);
		URI uri = f.toURI();
		files = Files.walk(Paths.get(uri)).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
		Collection<SolrInputDocument> colSolrDoc = new ArrayList<SolrInputDocument>();
		
		ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");		
		Iterator<File> it = files.iterator();
		
		while (it.hasNext()) {
			StringStream ss = new StringStream(it.next().toString());
			up.addContentStream(ss);
			up.setAction(ACTION.COMMIT, true, true);
			up.process(solrClient);

		}
		
	}
	
	private static void commit(Collection<SolrInputDocument> collection) {
		try {
			solrClient.add(collection);
			solrClient.commit();
			System.out.println("Commit 1000 Files");
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void reader(File file) throws IOException, SAXException, ParserConfigurationException {
		String temp = "";
		documentText = new LinkedList<String>();
		lines = Files.lines(file.toPath(), Charset.defaultCharset());
		lines.forEach(line -> {
			documentText.add(line);
		});
		for (String str : documentText) {
			temp += str;
		}
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(temp)));
		doc.normalize();
		iteration(doc.getDocumentElement());
	}

	private static void iteration(Node node) {
		filter(node);
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				iteration(currentNode);
			}
		}
	}

	private static void filter(Node node) {
		if (node.getNodeName().equals("field")) {
			solrDoc.addField(node.getAttributes().item(0).getNodeValue(), node.getTextContent());
		}
	}
}
