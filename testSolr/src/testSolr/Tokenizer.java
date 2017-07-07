package testSolr;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Klasse Tokenizer
 */
public class Tokenizer {

    private static final String LETTER = "0-9A-Za-zÜÖÄßüöä";
    
    private static final String WHITESPACE = "\\n\\s\\t"; 
    
    private static final String TOKENREGEX = "[" + LETTER + "]+";
    
    private static final String SEPARATORREGEX = "[" + WHITESPACE + "]*";
    
    private static List<String> tokens;

    /**
     * Tokenize
     * Tokanieziert gegeben Text und speicher die Tokens in einer Liste
     * @param input 
     * @return die Liste
     * @throws wirft eine UnsupportedEncodingException 
     */
    public static List<String> tokenize(String input) throws UnsupportedEncodingException {
    
        tokens = new ArrayList<String>();

        String tokenRegex = "(" + TOKENREGEX + ")";
        String delimiterRegex = "(" + SEPARATORREGEX + ")";

   
        Pattern p = Pattern.compile(tokenRegex + delimiterRegex);
        Matcher m = p.matcher(input);

        while (m.find()) {
            tokens.add(m.group(1));
        }
        return tokens;
    }


}
