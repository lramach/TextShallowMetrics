
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class negations{
	
	//If negative words were found and a neg. descriptor is seen => (-)(-) = (+)
	public static String[] NEGATIVE_DESCRIPTORS = new String[4879];
	
	public static String[] temp =	{
			//negated phrases (topical words I spotted in text)
			"NOTHING", "nowhere", "scarcely", "scarce", "zero", "drawback",
		      "barely", "hardly", "deny", "refuse", "fail", "failed",
		      "without", "ambiguous", "ambiguity", "neither", "empty",
				"deviation", "lacks", "lack", "lacking", "lacked", "abrupt", "abruptly", "somewhat", "copied", "copy",
				"overbalanced", "ambiguous", "missing", "poor",
				"negative", "negatively", "underrepresented", "duplication", "wrong", "mistake", "mistakes","duplications",
				"duplicate", "duplicated", "duplicating", "avoids", "messy", "deleted", "cumbersome", "strange",
				"strangely", "misspell", "misspelling", "misspellings", "misspelt", "verbose", "confuse", "confusion", "confusing",
				"confused", "confuses", "trivial", "triviality", "typo", "typos", "somewhat", "concerns", "concern",
				"barring", "overuse", "repitition", "useless", "biased", "rushed", "absent", "wordy", "bad", "less", 
				"unclear", "difficult", "vague", "briefly", "hard", "broken","replicate","replicated", "digress", "clutter",
				"cluttered", "inadequate", "deviation", "contrived", "contrive", "horrid", "trouble","uneven", "unevenly", "alot",
				"incorrect"
		};
	
	public negations() throws IOException{
		//Appending words from Bing Liu's opinion lexicon to identify negative words in the text
		File negatedWordsFile = new File("/Users/lakshmi/Documents/workspace/GraphRelevance_differing_single_patterns_predict/src/negative-words.csv");
		BufferedReader reader = new BufferedReader(new FileReader(negatedWordsFile));
		String text;
		int i = 0;
		while((text = reader.readLine()) != null){
			NEGATIVE_DESCRIPTORS[i] = text;
			i++;
		}
		//there are 96 words already in temp, appending those to NEGATIVE_DESCRIPTORS
		for(i = 4783; i < 4783+96; i++){
			NEGATIVE_DESCRIPTORS[i] = temp[i-4783];
		}
	}
	/*
	 * SENTENCE CLAUSE OR PHRASE FOLLOWING THESE WORDS CARRY A NEGATIVE MEANING (EITHER SUBTLE OR OVERT)
	 */
	public static final String[] NEGATED_WORDS = { "not", "n't", "WON'T", "DON'T", "DIDN'T", 
	      "DOESN'T", "WOULDN'T", "COULDN'T", "SHOULDN'T", "WASN'T", 
	      "WEREN'T", "AREN'T", "ISN'T", "HAVEN'T", "HASN'T", "HADN'T",  "NOBODY",  
	      "CAN'T","SHALLN'T", "MUSTN'T", "AIN'T", "cannot",
	      
	      //without the apostrophe
	      "cant", "dont", "wont","isnt","hasnt", "hadnt", "havent","arent", "werent", "wouldnt",
	      "didnt", "couldnt", "shouldnt", "mustnt", "shallnt",
	      
	      //other words that indicate negations (negative quantifiers)
	      "NO", "NEVER", "NONE",
	  }; 
	
	
	
	public static final String[] NEGATIVE_PHRASES = { "too concise",
		"taken from", "been removed", "too long", "off topic",
		"too short", "run on", "too much", "been directly", "similar to",
		"at odds", "grammatical errors", "grammatical error", "available online",
		"make up", "made up", "crammed up"//, "a lot"
	};
}