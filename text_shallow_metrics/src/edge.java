
public class edge {
	int edgeID;
	int type; //if it is a verb or adverb
	String name;
	int index;//identifies the sentence number in which the edge is present
	int degree;//number of relations the node has with other nodes
	public vertex inVertex;
	double averageMatch;// holds the average match value found for the edge
	public vertex outVertex;
	int[] edgeMatch = new int[5];//holds the number of matches for each metric value
	public int frequency;//indicates the number of times the edge repeats
	
	//semantic role labels
	String label = null;
	public edge(String edgeName, int edgeType){
		name = edgeName;
		type = edgeType;//1 - verb, 2 - adjective, 3-adverb 
		averageMatch = 0.0;//initializing match to -1
		frequency = 0;	
		//initializing the number of matches for each metric value to 0
		edgeMatch[0] = 0; edgeMatch[1] = 0; edgeMatch[2] = 0; edgeMatch[3] = 0; edgeMatch[4] = 0;
	}
}