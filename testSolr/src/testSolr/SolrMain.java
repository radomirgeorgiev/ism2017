package testSolr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.solr.common.SolrDocument;

import de.unidue.inf.is.eval.MiniTRECEval;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class SolrMain {

	private String pathIn, pathOut, pathQuery;
	private String urlSolr;
	private int queryCase;
	private boolean readCollection;
	private boolean indexCollection;
	private boolean doQuery;
	private boolean reduceQrelsDE;
	private boolean withPOS;

	private String queryID;
	private List<String> queryResult = new LinkedList<String>();
	private String path1 = ".\\";
	private Stream<String> lines;

	public SolrMain(String pathCollectionToRead_, String pathXMLCollectionToWrite_, String pathQuery_, String urlSolr_,
			int queryCase_, boolean readCollection_, boolean indexCollection_, boolean doQuery_, boolean reduceQrelsDE_,
			boolean withPOS_) throws Exception {

		this.pathIn = pathCollectionToRead_;
		this.pathOut = pathXMLCollectionToWrite_;
		this.pathQuery = pathQuery_;
		this.urlSolr = urlSolr_;
		this.queryCase = queryCase_;
		this.readCollection = readCollection_;
		this.indexCollection = indexCollection_;
		this.doQuery = doQuery_;
		this.reduceQrelsDE = reduceQrelsDE_;
		this.withPOS = withPOS_;

	}

	public void run() throws Exception {

		if (readCollection) {
			CollectionReader cr = new CollectionReader(pathIn, pathOut);
			cr.initFileStreamsForAllFiles();
		}
		if (indexCollection) {
			Initializer init = new Initializer(pathOut, urlSolr);
			init.createCollection();
		}
		if (reduceQrelsDE) {
			transform();
		}
		if (doQuery) {
			QueryReader qr = new QueryReader(pathQuery);
			LinkedList<HashMap<String, String>> myMap = new LinkedList<HashMap<String, String>>(qr.getQueries());
			for (HashMap<String, String> tempMap : myMap) {
				que(tempMap);
			}
			writeResultToFile();
			execute();
		}

	}

	private void query(String words) {
		MyQuery mq = new MyQuery(urlSolr);
		int counter = 1;
		int tempID = Integer.parseInt(queryID.substring(2, queryID.length() - 1));
		System.out.println("qID " + tempID);
		String documentID = "";
		double score;
		for (SolrDocument doc : mq.doQuery(words)) {
			String did = doc.getFieldValue("DOCID").toString();
			documentID = did.substring(1, did.length() - 1);
			String sc = doc.getFieldValue("score").toString();
			score = Double.parseDouble(sc.substring(0, sc.length()));

			String result = tempID + " " + 0 + " " + documentID + " " + counter + " " + score + " " + "test";
			queryResult.add(result);
			counter++;

		}
	}

	private String posTagger(String queryString) {
		String result = "";
		Properties props = new Properties();
		props.setProperty("pos.model", "./collection/german-ud.tagger");
		props.setProperty("annotators", "tokenize, ssplit, pos");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(queryString);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

				String word = token.get(CoreAnnotations.TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (pos.equals("PRON") || pos.equals("NOUN") || pos.equals("VERB") || pos.equals("NUM")) {
					result += word + " ";
				}

			}
		}
		return result;
	}

	private void que(HashMap<String, String> myMap) throws Exception {
		String query = "";
		String query1 = "";
		String query2 = "";
		String query3 = "";

		for (Map.Entry<String, String> keys : myMap.entrySet()) {
			// System.out.println("Key: " +keys.getKey() + " Text:
			// "+keys.getValue());
			if (keys.getKey().equals("num")) {
				queryID = keys.getValue();
			} else if (keys.getKey().equals("DE-title")) {
				query1 = keys.getValue();
			} else if (keys.getKey().equals("DE-desc")) {
				query2 = keys.getValue();
			} else if (keys.getKey().equals("DE-narr")) {
				query3 = keys.getValue();
			}

		}

		switch (queryCase) {

		case (0):
			query = query1;
			break;
		case (1):
			query = query2;
			break;
		case (2):
			query = query3;
			break;
		case (3):
			query = query1 + " " + query2;
			break;
		case (4):
			query = query1 + " " + query3;
			break;
		case (5):
			query = query2 + " " + query3;
			break;
		case (6):
			query = query1 + " " + query2 + " " + query3;
			break;
		}

		String tempQuery;
		if (withPOS) {
			tempQuery = posTagger(query);
		} else {
			tempQuery = query;
		}
		List<String> tokenizedList = new LinkedList<String>(Tokenizer.tokenize(tempQuery));

		for (String str : tokenizedList) {
			boolean b = new StopwordRemover().isStopWord(str);
			if (!b)
				query += str + " ";
		}
		System.out.println("Query words: " + query);

		query(query);
	}

	private void execute() throws IOException {
		String reduce;
		if (reduceQrelsDE) {
			reduce = "qrelsDE";
		} else {
			reduce = "qrels_DE";
		}
		String[] eval = { "-measures", "-trecrel", reduce, "-trectop", "queryResult" };
		;
		MiniTRECEval.main(eval);
	}

	private void writeResultToFile() throws IOException {

		eraseData();
		try {

			File file = new File(path1 + "queryResult");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String str : queryResult) {
				bw.write(str + "\n");
			}
			bw.close();

			System.out.println("Writing Results Done!!!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void eraseData() throws IOException {
		String filePath = "./";
		String file = "queryResult";

		PrintWriter writer = new PrintWriter(filePath + file);
		writer.print("");
		writer.close();

	}

	private void transform() throws IOException {
		File fi = new File("./qrels_DE");
		List<String> listOf = new LinkedList<String>();
		lines = Files.lines(fi.toPath(), Charset.defaultCharset());
		lines.forEach(line -> {
			String[] temp = line.split(" ");
			if (temp[2].contains("FR")) {
				listOf.add(line);
			}
		});
		PrintWriter writer = new PrintWriter("./qrelsDE");
		for (String str : listOf) {
			writer.print(str + "\n");
		}
		writer.close();
	}

}
