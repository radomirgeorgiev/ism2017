package crawler;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.util.ContentStreamBase.StringStream;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

public class CrawlerSolrIndexer {

	private String solrWebCollection;
	private List<Document> listOfFetchedDocuments;
	private HttpSolrClient solrClient;

	public CrawlerSolrIndexer(String solrWebCollection_, List<Document> listOfFetchedDocuments_) {
		this.solrWebCollection = solrWebCollection_;
		this.listOfFetchedDocuments = listOfFetchedDocuments_;
	}

	public void indexingHTML() throws IOException, SAXException, ParserConfigurationException, SolrServerException {
		solrClient = new HttpSolrClient.Builder(solrWebCollection).build();
		initialize();
	}

	private void initialize() throws IOException, SAXException, ParserConfigurationException, SolrServerException {

		ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");

		for (Document doc : listOfFetchedDocuments) {
			if (doc.hasText()) {
				StringStream ss = new StringStream(doc.toString());
				up.addContentStream(ss);
				up.setAction(ACTION.COMMIT, true, true);
				up.process(solrClient);
			}

		}

	}

}
