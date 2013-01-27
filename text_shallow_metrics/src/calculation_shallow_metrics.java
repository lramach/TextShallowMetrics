import is2.lemmatizer.Options;
import is2.parser.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.regex.*;


import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class calculation_shallow_metrics {

	static int SIM_MATCH = 5;
	static int numTexts = 2;
	public int numSentences;
	static int numReviews;
	static int numPatterns;
	public int NUM = 6000;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{		
		//getting the text
		calculation_shallow_metrics tc = new calculation_shallow_metrics();
		//MaxentTagger posTagger = new MaxentTagger("C:/Users/Lakshmi/Add-ons/stanford-postagger-2011-04-20/models/bidirectional-distsim-wsj-0-18.tagger");
		MaxentTagger posTagger = new MaxentTagger("/Users/lakshmi/Documents/Computer - workspaces et al/Add-ons/stanford-postagger-2011-09-14/models/bidirectional-distsim-wsj-0-18.tagger");
		
		//for the graph-genator's SRL code
		String[] opts ={"-model","/Users/lakshmi/Downloads/srl-20101031/featuresets/prs-eng.model"};
		Options options = new Options(opts);
		// create a parser
		Parser parser = new Parser(options);
		
		PrintWriter csvWriter = new PrintWriter(new FileWriter("/Users/lakshmi/Documents/Thesis/semantic-patterns-2012/Expertiza-full-patterns/results-shallow-metrics.csv"));
		csvWriter.append("Text"); csvWriter.append(',');
		csvWriter.append("Noun count"); csvWriter.append(',');
		csvWriter.append("Verb count"); csvWriter.append(',');
		csvWriter.append("Adjective count"); csvWriter.append(',');
		csvWriter.append("Adverb count"); csvWriter.append('\n');
		csvWriter.close();
		
		File texts = new File("/Users/lakshmi/Documents/Thesis/semantic-patterns-2012/Expertiza-full-patterns/suggest.csv");
		BufferedReader textReader = new BufferedReader(new FileReader(texts));
		String temp ="";
		while((temp = textReader.readLine()) != null){
			String[][] text = tc.getReview(0, temp);//training set	
			graphGenerator g = new graphGenerator();
			g.generateGraph(text[0], posTagger, 0, parser);//0 is a flag to indicate train reviews
			
			//writing the shallow features of the sentence to a file
			csvWriter = new PrintWriter(new FileWriter("/Users/lakshmi/Documents/Thesis/semantic-patterns-2012/Expertiza-full-patterns/results-shallow-metrics.csv", true));
			csvWriter.append(text[0][1]); csvWriter.append(',');
			csvWriter.append(g.numNouns+""); csvWriter.append(',');
			csvWriter.append(g.numVerbs+""); csvWriter.append(',');
			csvWriter.append(g.numAdjs+""); csvWriter.append(',');
			csvWriter.append(g.numAdv+""); csvWriter.append('\n');
			csvWriter.close();
		}
		
		
	}
	
	public String[][] getReview(int flag, String text) throws IOException
	{
		System.out.println("Inside getReview, text: "+text);
		//BufferedReader reader = new BufferedReader(new FileReader(filename));
		//String text;
		
		int SENTENCES = 500;//assuming each review has upto 5 sentences max.
		int MAX = NUM;
		
		String[][] reviews;
		if(flag == 0)
			reviews = new String[1][MAX * SENTENCES];
		else
			reviews = new String[MAX][SENTENCES];
		
		int i = 0, j = 0;
		Pattern digit = Pattern.compile("\\d+,\\d+");//looking for numbers with a comma in between like 1,000 for example/
		Pattern percent = Pattern.compile("\\d+\\.\\d+");//looking for numbers with period 10.1 for example
		
		//while(text != null)//reader.readLine(), && j < MAX
		//{
		  	System.out.println("Review:: "+text);
		    if(flag == 1)//reset i (the sentence counter) to 0 for test reviews
		    	i = 0;
		   	/******* Pre-processing the review text **********/
		   	//replacing commas in large numbers, makes parsing sentences with commas confusing!
		   	Matcher matchDigit = digit.matcher(text);//creating matcher for the reg-ex pattern
		   	while(matchDigit.find()){//if the matcher finds a hit for the patterns
		   		int start = matchDigit.start();
		   		int end = matchDigit.end();
		   		//start is added or else the index of "," would be wrt to the substring's length
		   		int index = start + text.substring(start, end).indexOf(",");
		   		//identifying the new text
		   		text = text.substring(0,index)+text.substring(index+1);
		   		System.out.println("Pattern digit"+text);
		   		//re-initializing matcher to check for patterns
		   		matchDigit = digit.matcher(text);
		   	}    	
		    
		   	//for normal comparison, do not remove quoted text
			text = text.replaceAll("\"", "");		   	
		   	text = text.replaceAll(";", "");
		   	text = text.replaceAll(",", "");
	    	
	    	//replacing digit in percent with "point"
	    	Matcher matchPercent = percent.matcher(text);//creating matcher for the reg-ex pattern
	    	while(matchPercent.find()){//if the matcher finds a hit for the patterns
	    		int start = matchPercent.start();
	    		int end = matchPercent.end();
	    		//System.out.println("Substring:: "+text.substring(start,end));
	    		//start is added or else the index of "," would be wrt to the substring's length
	    		int index = start + text.substring(start, end).indexOf(".");
	    		//identifying the new text
	    		text = text.substring(0,index)+"point"+text.substring(index+1);
	    		//re-initializing matcher to check for patterns
	    		matchPercent = percent.matcher(text);
	    	}
	    	/******* End of pre-processing **********/
	    	System.out.println("Pattern digit 2"+text);
	    	//break the text into multiple sentences
	    	int begin = 0;
	    	if(text.contains(".") || text.contains("?") || text.contains("!") || text.contains(",") || text.contains(";") )//new clause or sentence
	    	{
		    	while(text.contains(".") || text.contains("?") || text.contains("!") || text.contains(",") || text.contains(";") )//the text contains more than 1 sentence
		    	{
		    		int end = 0;
		    		if(text.contains("."))
		    			end = text.indexOf(".");
		    		if ((text.contains("?") && end != 0 && end > text.indexOf("?")) || (text.contains("?") && end == 0))//if a ? occurs before a .
		    			end = text.indexOf("?");
		    		if((text.contains("!") && end!= 0 && end > text.indexOf("!")) || (text.contains("!") && end ==0))//if an ! occurs before a . or a ?
		    			end = text.indexOf("!");
		    		if((text.contains(",") && end != 0 && end > text.indexOf(",")) || (text.contains(",") && end == 0))//if a , occurs before any of . or ? or ! 
		    			end = text.indexOf(",");
		    		if((text.contains(";") && end != 0 && end > text.indexOf(";")) || (text.contains(";") && end == 0))//if a ; occurs before any of . or ?, ! or , 
		    			end = text.indexOf(";");
		    		
		    		//check if the string between two commas or punctuations is there to buy time e.g. ", say," ",however," ", for instance, "... 
		    		if(flag == 0)//training
		    			reviews[0][i] = text.substring(begin, end);
		    		else //testing
		    			reviews[j][i] = text.substring(begin, end);
		    		//System.out.println("###### "+reviews[j][i]);
		    		i++;//incrementing the sentence counter
		    		text = text.substring(end+1);//from end+1 to the end of the string variable
			    	//System.out.println("Remaining txt:"+text);		    		
			    }
		    }
	    	else{//if there is only 1 sentence in the text
	    		if(flag == 0){//training	    			
	    			reviews[0][i] = text;
	    			i++;//incrementing the sentence counter
	    		}
	    		else{ //testing
	    			reviews[j][i] = text;
	    			//j++;
	    		}
		    }
	    	
		    if(!text.isEmpty()){//if text is not empty
		    	if(flag == 0){//training
		    		reviews[0][i] = text;
		    		i++;
		    	}
		    	else{ //testing
		    		reviews[j][i] = text;
		    		//j++;
		    	}
	    	}

		    if(flag == 1)//incrementing reviews counter only for test reviews only
		    	j++;  

	    //}//outer while loop
		
		//setting the number of reviews before returning
		if(flag == 0){//training
			numReviews = 1;//for training the number of reviews is 1
			numSentences = i;
			System.out.println("**** Number of review sentences for training:: "+numSentences);
		}
		else //testing
			numReviews = j;
		
		System.out.println("******* Number of reviews:: "+numReviews);
		return reviews;
	}
}
