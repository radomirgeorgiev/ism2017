package crawler;

import java.net.MalformedURLException;

public class Run {

	private static final String URL = "http://www.spiegel.de/";
	private static final String CORE_TO_BE_FILLED = "http://localhost:8983/solr/webcrawler";
	private static final String WRITING_INTO_FOLDER = "";
	private static final int NUMBER_OF_DOCUMENTS_TO_SEARCHING_OF = 100;
	private static final boolean SEARCH_EXTERNAL_LINKS = false;
	private static final boolean INDEXING_TO_SOLR = false;
	private static final boolean WRITING_RESULT = true;

	public static void main(String[] args) throws MalformedURLException {
		CrawlerMain crawler = new CrawlerMain(URL, NUMBER_OF_DOCUMENTS_TO_SEARCHING_OF, SEARCH_EXTERNAL_LINKS,
				INDEXING_TO_SOLR, CORE_TO_BE_FILLED, WRITING_INTO_FOLDER, WRITING_RESULT);
		crawler.search();
	}
}