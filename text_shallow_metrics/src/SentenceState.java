
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class SentenceState {

	public static int POSITIVE = 0;
	public static int SUGGESTIVE = 1;
	public static int NEGATED = 2;
	
	public static int NEGATIVE_WORD = 3;
	public static int NEGATIVE_DESCRIPTOR = 4;
	public static int NEGATIVE_PHRASE = 5;
	public String[] brokenSentences = null;
	public int MAX = 10;
	
	public int[] identifySentenceState(String strWithPosTags) throws IOException{
		System.out.println("**** Inside identifySentenceState"+strWithPosTags);
		//break the sentence at the co-ordinating conjunction
		int numConjunctions = breakAtCoordinatingConjunctions(strWithPosTags);
		
		int[] states_array = new int[numConjunctions];
		if(brokenSentences == null){//no co-ordinating conjunction
			states_array[0] = sentenceState(strWithPosTags);
		}
		//identifying states for each of the sentence segments
		else{
			for(int i = 0; i < numConjunctions; i++){
				if(brokenSentences[i] != null){
					System.out.println("brokenSentences[i]:: "+brokenSentences[i]);
					states_array[i] = sentenceState(brokenSentences[i]);
				}
			}
		}
		//System.out.println("&&&&&&& Final State:: "+STATE);
		return states_array;
	}
	
	public int breakAtCoordinatingConjunctions(String strWithPosTags){
		StringTokenizer st;
		//String strWithPosTags = posTagger.tagString(sentence);
		st = new StringTokenizer(strWithPosTags);
		//System.out.println("Sentence with tags::"+strWithPosTags);
		int count = st.countTokens();
		int counter = 0;

		brokenSentences = new String[MAX];
		//if the sentence contains a co-ordinating conjunction
		if(strWithPosTags.contains("CC")){// || strWithPosTags.contains("IN")
			//System.out.println("Found a co-ordinating conjunction!");
			counter = 0;
			String temp = "";
			while(st.hasMoreTokens()){
				String ps = st.nextToken();
				if(ps.contains("CC")){//|| ps.contains("IN") //token contains CC//&& !ps.substring(0, ps.indexOf("/")).equalsIgnoreCase("of")
					//System.out.println("Sentence segment:: "+temp);
					brokenSentences[counter] = temp;// +" "+ps.substring(0, ps.indexOf("/"));//for "run/NN on/IN..."
					counter++;
					temp = ps.substring(0, ps.indexOf("/"));
					//the CC or IN goes as part of the following sentence
				}
				else{
					temp = temp +" "+ ps.substring(0, ps.indexOf("/"));
				}
			}
			if(!temp.isEmpty()){//setting the last sentence segment
				brokenSentences[counter] = temp;
				counter++;
			}
		}
		else{//if no co-ordinating conjunctions were found
			brokenSentences[counter] = strWithPosTags;
			counter++;
		}	
		return counter;
	}
	
	//Checking if the token is a negative token
	public int sentenceState(String strWithPosTags){
		System.out.println("***** Checking sentence state::"+strWithPosTags);
		//System.out.println("***** Checking sentence state::");
		int STATE = POSITIVE;
		//checking single tokens for negated words
		StringTokenizer st = new StringTokenizer(strWithPosTags);
		int count = st.countTokens();
		//System.out.println("Count:: "+count);
		String[] tokens = new String[count];
		String[] taggedTokens = new String[count];
		int i = 0;
		boolean interimNOUNVERB  = false;//0 indicates no interim nouns or verbs
				
		//fetching all the tokens
		while(st.hasMoreTokens()){
			String ps = st.nextToken();
			//setting the tagged string
			taggedTokens[i] = ps;
			if(ps.contains("/"))
				ps = ps.substring(0, ps.indexOf("/"));	
			if(ps.contains("."))
				tokens[i] = ps.substring(0, ps.indexOf("."));//ps.replaceAll(".", "") - DOESNT WORK
			else if(ps.contains(","))
				tokens[i] = ps.replaceAll(",", "");
			else if(ps.contains("!"))
				tokens[i] = ps.replaceAll("!", "");
			else if(ps.contains(";"))
				tokens[i] = ps.replaceAll(";", "");
			else
				tokens[i] = ps;
				//System.out.println("tokens[i]:"+tokens[i]);
				i++;
		}
		
		//iterating through the tokens to determine state
		String prevNegativeWord ="";
		for(int j = 0; j < count; j++){
			System.out.println("tokens[j]:"+tokens[j]);
			
			//checking type of the word
			int returnedType;
			//checking for negated words
			if(isNegativeWord(tokens[j]) == NEGATED)	
				returnedType = NEGATIVE_WORD;
			//checking for a negative descriptor (indirect indicators of negation)
			else if(isNegativeDescriptor(tokens[j]) == NEGATED)
				returnedType = NEGATIVE_DESCRIPTOR;
			//2-gram phrases of negative phrases
			else if(j+1 < count && isNegativePhrase(tokens[j]+" "+tokens[j+1]) == NEGATED){
				returnedType = NEGATIVE_PHRASE;
				j = j+1;
			}
			//if suggestion word is found
			else if(isSuggestive(tokens[j]) == SUGGESTIVE)
				returnedType = SUGGESTIVE;
			//2-gram phrases suggestion phrases
			else if(j+1 < count && isSuggestivePhrase(tokens[j]+" "+tokens[j+1]) == SUGGESTIVE){
				returnedType = SUGGESTIVE;
				j = j+1;
			}
			//else set to positive
			else
				returnedType = POSITIVE;
			
			//----------------------------------------------------------------------
			//comparing 'returnedType' with the existing STATE of the sentence clause
			//after returnedType is identified, check its state and compare it to the existing state
			
			System.out.println("token:: "+tokens[j]+" returnedType::"+returnedType+"STATE::"+STATE);
			System.out.println("prevNegativeWord:: "+prevNegativeWord);
			
			//if an interim non-negative or non-suggestive word was found
			if(returnedType == POSITIVE){
				if(interimNOUNVERB == false && (taggedTokens[j].contains("NN") || taggedTokens[j].contains("PR")  
						|| taggedTokens[j].contains("VB") || taggedTokens[j].contains("MD"))){
					interimNOUNVERB = true;
				}
			}
			
			
			if(STATE == POSITIVE && returnedType != POSITIVE){
				STATE = returnedType;
				//interimNOUNVERB = 0;//resetting
			}
			//when state is a negative word
			else if(STATE == NEGATIVE_WORD){//previous state
				if( returnedType == NEGATIVE_WORD){
					//these words embellish the negation, so only if the previous word was not one of them you make it positive
					if(!prevNegativeWord.equalsIgnoreCase("NO") && !prevNegativeWord.equalsIgnoreCase("NEVER") && !prevNegativeWord.equalsIgnoreCase("NONE"))
						STATE = POSITIVE;//e.g: "not had no work..", "doesn't have no work..", "its not that it doesn't bother me..."
					else
						STATE = NEGATIVE_WORD;//e.g: "no it doesn't help", "no there is no use for ..."
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == NEGATIVE_DESCRIPTOR || returnedType == NEGATIVE_PHRASE){
					STATE = POSITIVE;//e.g.: "not bad", "not taken from", "I don't want nothing", "no code duplication"// ["It couldn't be more confusing.."- anomaly we dont handle this for now!]
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == SUGGESTIVE){
					//e.g. " it is not too useful as people could...", what about this one?
					if(interimNOUNVERB == true)//there are some words in between
						STATE = NEGATIVE_WORD;
					else
						STATE = SUGGESTIVE;//e.g.:"I do not(-) suggest(S) ..."
					interimNOUNVERB = false;//resetting
				}
			}
			//when state is a negative descriptor
			else if(STATE == NEGATIVE_DESCRIPTOR){
				if(returnedType == NEGATIVE_WORD){
					if(interimNOUNVERB == true)//there are some words in between
						STATE = NEGATIVE_WORD;//e.g: "hard(-) to understand none(-) of the comments"
					else
						STATE = POSITIVE;//e.g."He hardly not...."
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == NEGATIVE_DESCRIPTOR){
					if(interimNOUNVERB == true)//there are some words in between
						STATE = NEGATIVE_DESCRIPTOR;//e.g:"there is barely any code duplication"
					else 
						STATE = POSITIVE;//e.g."It is hardly confusing..", but what about "it is a little confusing.."
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == NEGATIVE_PHRASE){
					if(interimNOUNVERB == true)//there are some words in between
						STATE = NEGATIVE_PHRASE;//e.g:"there is barely any code duplication"
					else 
						STATE = POSITIVE;//e.g.:"it is hard and appears to be taken from"
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == SUGGESTIVE){
					STATE = SUGGESTIVE;//e.g.:"I hardly(-) suggested(S) ..."
					interimNOUNVERB = false;//resetting
				}
			}
			//when state is a negative phrase
			else if(STATE == NEGATIVE_PHRASE){
				if(returnedType == NEGATIVE_WORD){
					if(interimNOUNVERB == true)//there are some words in between
						STATE = NEGATIVE_WORD;//e.g."It is too short the text and doesn't"
					else
						STATE = POSITIVE;//e.g."It is too short not to contain.."
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == NEGATIVE_DESCRIPTOR){
					STATE = NEGATIVE_DESCRIPTOR;//e.g."It is too short barely covering..."
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == NEGATIVE_PHRASE){
					STATE = NEGATIVE_PHRASE;//e.g.:"it is too short, taken from ..."
					interimNOUNVERB = false;//resetting
				}
				else if(returnedType == SUGGESTIVE){
					STATE = SUGGESTIVE;//e.g.:"I too short, I suggest ..."
					interimNOUNVERB = false;//resetting
				}
			}
			//when state is suggestive
			else if(STATE == SUGGESTIVE){//e.g.:"I might(S) not(-) suggest(S) ..."
				if(tokens[j].equalsIgnoreCase("not") || tokens[j].equalsIgnoreCase("n't")){//e.g. "I could not..."
					STATE = NEGATIVE_WORD;
				}
				if(returnedType == NEGATIVE_DESCRIPTOR){
					STATE = NEGATIVE_DESCRIPTOR;
				}
				if(returnedType == NEGATIVE_PHRASE){
					STATE = NEGATIVE_PHRASE;
				}
				//e.g.:"I suggest you don't.." -> suggestive
				interimNOUNVERB = false;//resetting
			}
			
			//setting the prevNegativeWord
			if(tokens[j].equalsIgnoreCase("NO") || tokens[j].equalsIgnoreCase("NEVER") || tokens[j].equalsIgnoreCase("NONE"))
				prevNegativeWord = tokens[j];
			
		}//end of for loop
		
		if(STATE == NEGATIVE_DESCRIPTOR || STATE == NEGATIVE_WORD || STATE == NEGATIVE_PHRASE)
			STATE = NEGATED;
				
		System.out.println("*** Complete Sentence State:: "+STATE);
		return STATE;
	}
		

	//Checking if the token is a negative token
	public int isNegativeWord(String word){
		int notNegated = POSITIVE;
		//System.out.println("Comparison of word: "+word);
		for (int i = 0; i < negations.NEGATED_WORDS.length; i++){
			//System.out.println("Comparing with "+negations.NEGATED_WORDS[i]);
			if (word.equalsIgnoreCase(negations.NEGATED_WORDS[i])){
				notNegated =  NEGATED;//indicates negation found
				//System.out.println("Returning match for:"+negations.NEGATED_WORDS[i]);
		    	break;
			}
		}
		//System.out.println("***isNegation:: "+word +" state:: "+notNegated);
		return notNegated;
	}
		
	//Checking if the token is a negative token
	public int isNegativeDescriptor(String word){
		int notNegated = POSITIVE;
		for (int i = 0; i < negations.NEGATIVE_DESCRIPTORS.length; i++){
			//System.out.println("Comparing with "+negations.NEGATIVE_DESCRIPTORS[i]);
			if (word.equalsIgnoreCase(negations.NEGATIVE_DESCRIPTORS[i])){
				notNegated =  NEGATED;//indicates negation found
				break;
			}
		}
		//System.out.println("***isNegation:: "+word +" state:: "+notNegated);
		return notNegated;
	}
		
		//Checking if the phrase is negative
	public int isNegativePhrase(String phrase){
		int notNegated = POSITIVE;
		for (int i = 0; i < negations.NEGATIVE_PHRASES.length; i++){
			//System.out.println("Comparing with "+negations.NEGATED_WORDS[i]);
			if (phrase.equalsIgnoreCase(negations.NEGATIVE_PHRASES[i])){
				notNegated =  NEGATED;//indicates negation found
				break;
			}
		}
		//System.out.println("***isNegation:: "+word +" state:: "+notNegated);
		return notNegated;
	}
		
	//Checking if the token is a suggestive token
	public int isSuggestive(String word){
		int notSuggestive = POSITIVE;
		for (int i = 0; i < suggestions.SUGGESTIVE_WORDS.length; i++){
			//System.out.println("Comparing with "+negations.NEGATED_WORDS[i]);
			if (word.equalsIgnoreCase(suggestions.SUGGESTIVE_WORDS[i])){
				notSuggestive =  SUGGESTIVE;//indicates negation found
				break;
			}
		}
		//System.out.println("***isSuggestive:: "+word +" state:: "+notSuggestive);
		return notSuggestive;
	}
		
	//Checking if the PHRASE is suggestive
	public int isSuggestivePhrase(String phrase){
		int notSuggestive = POSITIVE;
		for (int i = 0; i < suggestions.SUGGESTIVE_PHRASES.length; i++){
			if (phrase.equalsIgnoreCase(suggestions.SUGGESTIVE_PHRASES[i])){
				notSuggestive =  SUGGESTIVE;//indicates negation found
				break;
			}
		}
		//System.out.println("***isNegation:: "+word +" state:: "+notNegated);
		return notSuggestive;
	}
			
	/*
	 * for strings with more than one token, calls for patterns from textcollection
	 */
	public int isSuggestive(String word, int flag){
		int notSuggestive = POSITIVE;
		StringTokenizer st = new StringTokenizer(word);
		while(st.hasMoreTokens())
		{
			String ps = st.nextToken();
			for (int i = 0; i < suggestions.SUGGESTIVE_WORDS.length; i++){
				//System.out.println("Comparing with "+negations.NEGATED_WORDS[i]);
				if (ps.equalsIgnoreCase(suggestions.SUGGESTIVE_WORDS[i])){
					notSuggestive =  SUGGESTIVE;//indicates negation found
					break;
				}
			}
			if(notSuggestive == SUGGESTIVE)break;
		}
		//System.out.println("***isSuggestive:: "+word +" ::State:: "+notSuggestive);
		return notSuggestive;
	}

}