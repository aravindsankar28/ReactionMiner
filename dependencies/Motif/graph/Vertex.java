package graph;
import java.util.Vector;

import Dictionary.*;

public class Vertex {

	public Vertex(String label, int id) {
		// TODO Auto-generated constructor stub
		this.label=vertexDictionary.addID(label);
		this.id=id;
		edges=new Vector<Integer>();
	}
	public Vertex(int label, int id) {
		// TODO Auto-generated constructor stub
		this.label=label;
		this.id=id;
		edges=new Vector<Integer>();
		
	}
	
	public Vertex(Vertex v,int id) {
		// TODO Auto-generated constructor stub
		this.label=v.label;
		this.edges=new Vector<Integer>();
		this.id=id;
	}
	

	/**
	 * Change label of vertex and accordingly update vertexDictionary freq
	 * @param label
	 */
	public void changeLabel(String label)
	{
		int currVal=(Integer)vertexDictionary.freq.elementAt(this.label);

			vertexDictionary.freq.setElementAt(currVal-1,this.label);
		
		this.label=vertexDictionary.addID(label);
	}
	
	public int id; //vertex id. range:[0,number of vertices]
	public int label;
	public Vector<Integer> edges;
	public String stringLabel() {
		return vertexDictionary.labels.get(label);
	}
}
