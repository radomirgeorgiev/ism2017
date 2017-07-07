package testSolr;

public class Run {

	
	private static final String READ_DOCUMENT_FROM_FOLDER = "./fr_rundschau/";
	private static final String CORE_TO_BE_FILLED = "http://localhost:8983/solr/ism2017";
	private static final String WRITING_XML_INTO_FOLDER = "./NewDocuments/";
	private static final String READ_QUERIES_FROM_FILE = "./Top-de03.txt";
	private static final boolean INDEXING_TO_SOLR = false;
	private static final boolean CHANGE_DOCUMENTS_TO_XML = false;
	private static final boolean DO_QUERY = true;
	private static final boolean REDUCE_QRELS_DE = false;
	private static final boolean DO_QUERY_WITH_POS = true;
	
	/*
	 * Use-case  queries:
	 * 0 - Only "DE-title"
	 * 1 - Only "DE-desc"
	 * 2 - Only "DE-narr"
	 * 3 - 0+1
	 * 4 - 0+2
	 * 5 - 1+2
	 * 6 - 0+1+2
	 */
	private static final int QUERY_FORM = 0;
	
	
	public static void main(String[] args) throws Exception {
		SolrMain sm = new SolrMain(READ_DOCUMENT_FROM_FOLDER, WRITING_XML_INTO_FOLDER, READ_QUERIES_FROM_FILE, CORE_TO_BE_FILLED,
				QUERY_FORM, CHANGE_DOCUMENTS_TO_XML, INDEXING_TO_SOLR,DO_QUERY, REDUCE_QRELS_DE,
				DO_QUERY_WITH_POS);
		sm.run();
	}
}
