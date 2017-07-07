package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

public class CrawlerMain {

	private String url;
	private String collectionToIndex;
	private String outputFolder;
	private int numberOfDocuments;
	private boolean searchExternalLinks;
	private boolean indexing;
	private boolean writing;
	private Set<String> pagesVisited = new HashSet<String>();
	private List<String> pagesToVisit = new LinkedList<String>();
	private List<Document> listOfFetchedDocuments = new LinkedList<Document>();

	public CrawlerMain(String url_, int numberOfDocuments_, boolean searchExternalLinks_, boolean indexing_,
			String collectionToIndex_, String outputFolder_, boolean writing_) {
		this.url = url_;
		this.numberOfDocuments = numberOfDocuments_;
		this.searchExternalLinks = searchExternalLinks_;
		this.indexing = indexing_;
		this.collectionToIndex = collectionToIndex_;
		this.outputFolder = outputFolder_;
		this.writing = writing_;
	}

	public void search() throws MalformedURLException {
		while (pagesVisited.size() < numberOfDocuments) {
			String currentUrl;
			CrawlerConnection leg = new CrawlerConnection();
			if (pagesToVisit.isEmpty()) {
				currentUrl = url;
				pagesVisited.add(url);
			} else {
				currentUrl = nextUrl();
			}
			leg.crawl(currentUrl);

			// if (searchForTerm) {
			// boolean success = leg.searchForWord(searchTerm);
			// if (success) {
			// System.out.println(String.format("**Success** Word %s found at
			// %s", searchTerm, currentUrl));
			// break;
			// }
			// }

			if (searchExternalLinks) {
				pagesToVisit.addAll(leg.getLinks());
			} else {
				pagesToVisit.addAll(filterLinks(leg.getLinks()));
			}

			if (writing) {
				writeToFile(leg.getDocument(), currentUrl);
			}

			listOfFetchedDocuments.add(leg.getDocument());

		}

		System.out.println("\n**Done** Visited " + pagesVisited.size() + " web page(s)");

		if (indexing) {
			CrawlerSolrIndexer si = new CrawlerSolrIndexer(collectionToIndex, listOfFetchedDocuments);
			try {
				si.indexingHTML();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private List<String> filterLinks(List<String> listOfURLS) {
		List<String> listToReturn = new LinkedList<String>();

		for (String tempURL : listOfURLS) {

			if (checkForValideURL(tempURL)) {
				URL tUrl, pUrl;
				try {
					tUrl = new URL(tempURL);
					pUrl = new URL(url);
					if (tUrl.getAuthority().equals(pUrl.getAuthority())) {

						listToReturn.add(tempURL);

					}
				} catch (MalformedURLException e) {

				}

			}

		}
		return listToReturn;
	}

	private String nextUrl() {
		String nextUrl;
		do {
			nextUrl = pagesToVisit.remove(0);
		} while (pagesVisited.contains(nextUrl));
		pagesVisited.add(nextUrl);
		return nextUrl;
	}

	private boolean checkForValideURL(String url) {
		String[] schemes = { "http", "https" };
		UrlValidator urlV = new UrlValidator(schemes);
		return urlV.isValid(url);
	}

	private void writeToFile(Document doc, String url) throws MalformedURLException {
		if(checkForValideURL(url)){
			URL u1 = new URL(url);
			if(url.endsWith(".html")){				
				System.out.println("Autority: "+u1.getAuthority());
			} else {
				System.out.println("URL: " + url);
				System.out.println(u1.getHost());
				System.out.println();
			}
		}
//		try {
//
//			File file = new File(collectionToIndex + url + ".html");
//
//			// if file doesnt exists, then create it
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write(doc.toString());
//			bw.close();
//
//			System.out.println(url + " Done!!!");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	private URL toURI(String tURL) {
		URL temp = null;
		try {
			temp = new URL(tURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}
}