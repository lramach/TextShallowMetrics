
public class vertex {
	int nodeID;
	int type; //if it is a noun - 1, adjective - 2, verb - 3, adverb - 4
	public String name;
	int index;//identifies the sentence number in which the vertex is present
	int degree;//number of relations the node has with other nodes
	//assuming that a vertex has only one property
	vertex[] property;// a vertex could contain a property - adverb, adjectives
	int propertyCount;//indicates the number of properties a vertex has
	int MAX = 50;//a vertex can have a maximum of 3 properties
	public int frequency;//the number of times the vertex was repeatedly accessed
	int state;// indicates if the word is negated or not - [true - not negated and false - negated] 
	String POSTag;
	
	//for semantic role labelling
	String label;
	String parent;
	
	public vertex(String vertexName, int vertexType, int indexValue, int State, String lab, String par, String tag){
		name = vertexName;
		type = vertexType;
		property = new vertex[MAX];
		propertyCount = 0; //initialized to 0 since the vertex has no properties at start-up
		frequency = 0;
		index = indexValue;
		nodeID = -1;//to identify if the id has been set or not
		state = State;//they are not negated by default
		
		//for semantic role labelling
		label = lab;
		parent = par;
		
		POSTag = tag;
	}
}