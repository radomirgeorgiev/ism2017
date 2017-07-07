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

public class Test {

	private final String URL = "http://localhost:8983/solr/";
	private String queryID;
	private List<String> queryResult = new LinkedList<String>();
	private String path1 = ".\\";
	private Stream<String> lines;

	public Test() throws Exception {
		String path = "C:\\Users\\Nivelin Stoyanov\\Desktop\\Internet_Suchmashinen\\NewDocuments\\";
		String queryMapPath = "C:\\Users\\Nivelin Stoyanov\\Desktop\\Internet_Suchmashinen\\Top-de03.txt";

		// CollectionReader cr = new CollectionReader(path);
		// cr.initFileStreamsForAllFiles();
		// Initializer init = new Initializer(path);
		// init.createCollection();

		QueryReader qr = new QueryReader(queryMapPath);
		LinkedList<HashMap<String, String>> myMap = new LinkedList<HashMap<String, String>>(qr.getQueries());
		for (HashMap<String, String> tempMap : myMap) {
			que(tempMap);

		}

		// transform();
		writeResultToFile();
		execute();

	}

	private void query(String words) {
		MyQuery mq = new MyQuery(URL);
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
		for (Map.Entry<String, String> keys : myMap.entrySet()) {
			// System.out.println("Key: " +keys.getKey() + " Text:
			// "+keys.getValue());
			if (keys.getKey().equals("num")) {
				queryID = keys.getValue();
			}

			List<String> tokenizedList = new LinkedList<String>(Tokenizer.tokenize(posTagger(keys.getValue())));

			for (String str : tokenizedList) {
				boolean b = new StopwordRemover().isStopWord(str);
				if (!b)
					query += str + " ";
			}
			System.out.println("Query words: " + query);

		}
		query(query);
	}

	private void execute() throws IOException {

		String[] eval = {"-measures", "-trecrel", "qrelsDE", "-trectop", "queryResult"};
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
