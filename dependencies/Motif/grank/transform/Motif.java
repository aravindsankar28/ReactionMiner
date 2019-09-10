package grank.transform;

import graph.Graph;

public class Motif {

	public Graph g;
	public Hist hist;
	public double score;
	public int rank;
	public Motif(Graph g,Hist hist,double score)
	{
		this.g=g;
		this.hist=hist;
		this.score=score;
	}
}
