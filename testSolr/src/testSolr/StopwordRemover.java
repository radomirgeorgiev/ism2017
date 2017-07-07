package testSolr;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Klasse StopwordRemover.
 */
public class StopwordRemover {

	private static String fileName = "collection/stopwordsLong";
	private List<String> wordList = null;

	/**
	 * Etstelle Instanz von StopwordRemover Liest ein Dokument von gegebenen
	 * Ordner und speichert die Daten in einer Kette
	 *
	 * @throws Exception
	 */
	public StopwordRemover() throws Exception {
		File file = new File(fileName);
		try {
			wordList = new ArrayList<String>(FileUtils.readLines(file, Charset.defaultCharset()));
		} catch (Exception e) {
			wordList.clear();
		}
	}

	/**
	 * Prüfe ob ein Wort in der gegebene Liste ein Stopwort ist
	 *
	 * @param word
	 * @return gibt true zurück, wenn wahr ist, sonst false
	 */
	public boolean isStopWord(String word) {
		if (wordList == null) {
			return false;
		} else {
			return wordList.contains(word);
		}
	}

}
