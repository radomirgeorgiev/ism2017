package testSolr;

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
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Initializer {

	private String urlSolr;
	private String documentFolder;
	private List<File> files;
	private Stream<String> lines;
	private List<String> documentText;
	private Document doc;
	private SolrInputDocument solrDoc;
	private HttpSolrClient solrClient;

	public Initializer(String documentFolder_, String urlSolr_) {
		this.documentFolder = documentFolder_;
		this.urlSolr = urlSolr_;
	}

	public void createCollection() throws IOException, SAXException, ParserConfigurationException {
		solrClient = new HttpSolrClient.Builder(urlSolr).build();
		initialize();
	}

	private void initialize() throws IOException, SAXException, ParserConfigurationException {
		int counter = 0;
		File f = new File(documentFolder);
		URI uri = f.toURI();
		files = Files.walk(Paths.get(uri)).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
		Collection<SolrInputDocument> colSolrDoc = new ArrayList<SolrInputDocument>();
		Iterator<File> it = files.iterator();
		while (it.hasNext()) {
			solrDoc = new SolrInputDocument();
			reader((File) it.next());
			colSolrDoc.add(solrDoc);
			counter++;
			if (counter == 1000) {
				commit(colSolrDoc);
				colSolrDoc.clear();
				counter = 0;
			}
		}
		commit(colSolrDoc);
	}

	private void commit(Collection<SolrInputDocument> collection) {
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

	private void reader(File file) throws IOException, SAXException, ParserConfigurationException {
		String temp = "";
		documentText = new LinkedList<String>();
		lines = Files.lines(file.toPath(), Charset.forName("UTF-8"));
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
		if (node.getNodeName().equals("field")) {
			solrDoc.addField(node.getAttributes().item(0).getNodeValue(), node.getTextContent());
		}
	}

}
