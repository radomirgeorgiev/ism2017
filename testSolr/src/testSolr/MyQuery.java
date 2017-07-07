package testSolr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;

public class MyQuery {

	private SolrClient solr;

	public MyQuery(String url) {
		solr = new HttpSolrClient.Builder(url).build();
	}

	public SolrDocumentList doQuery(String myQQ) {

		SolrQuery query = new SolrQuery();
		query.add(CommonParams.Q, "TEXT:"+myQQ);
		query.add(CommonParams.ROWS, "50");
		query.add(CommonParams.FL, "DOCID, score");
		QueryResponse response = null;
		try {
			response = solr.query("ism2017", query); 
		} catch (Exception e) {
			// log the exception
		}
		
		return response.getResults();

	}

}
