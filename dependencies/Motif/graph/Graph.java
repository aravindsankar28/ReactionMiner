package graph;

import java.util.Arrays;
import java.util.Vector;

import joelib2.smiles.SMILESParser;

import Dictionary.vertexDictionary;

public class Graph {
	
	public Vertex[] V;
	public Edge[] E;
	public String id;
	public Vector H;
	public int index;
	//public String label;
	public Graph(Vertex[] V, Edge[] E, String id,int index)
	{
		this.V=V.clone();
		this.E=E.clone();
		this.id=new String(id);
		this.H=new Vector();
		this.index=index;
	}
	public Graph(Vertex[] V, Edge[] E, String id,Vector H,int index)
	{
		this.V=V.clone();
		this.E=E.clone();
		this.id=new String(id);
		this.H=H;
		this.index=index;
	}
	public Vector Hcount()
	{
		Vector<Integer> H = new Vector<Integer>();
		for(int i=0; i<V.length; i++)
			if( ((String)vertexDictionary.labels.get(V[i].label)).equals("h") )
				H.add(V[i].id);
		return H;
	}
	public double[][] getAdjMatrix()
	{
		double[][] adjMatrix=new double[V.length][V.length];
		for(int i=0;i<V.length;i++)
		{
			Arrays.fill(adjMatrix[i],0);
			int numNeighbors=V[i].edges.size();
			double prob=1/numNeighbors;
			for(int j=0;j<numNeighbors;j++)
			{
				adjMatrix[i][E[(Integer)V[i].edges.elementAt(j)].getNeighbor(V[i].id)]=prob;
			}
		}
		return adjMatrix;
	}
	
	public Graph clone()
	{
		return new Graph(V.clone(),E.clone(),new String(id),(Vector)H.clone(),this.index);
	}
	public void print()
	{
		System.out.println("Printing "+id);
		
		for (int i=0;i<V.length;i++)
		{
			System.out.println(V[i].label);
		}
		System.out.println("---------Edges---------");
		for (int i=0;i<E.length;i++)
		{
			System.out.println(E[i].type);
		}
	}
	public String toString()
	{
		return this.toString("normal");
	}
	/**
	 * 
	 * @param format: either "normal" or "cg"
	 * @return
	 */
	public String toString(String format) {
		// TODO Auto-generated method stub
		if (format.equals("CG"))
		{
			return toStringCG();
		}
		else
			if(format.equals("normal"))
			{
				return toStringNormal();
			}
			else
			{
				System.err.println("Wrong Format Argument(CG/normal)");
				return null;
			}
		
	}
	private String toStringCG() {
		// TODO Auto-generated method stub
        StringBuffer s=new StringBuffer("");
        //s.append("graph G {\nnode[shape=plaintext width=.05 height=.1 fontsize=12]\nedge[len=1 ]\ngraph[size=5,5]\n");
        String head=s.toString();
        StringBuffer edges=new StringBuffer("");
        s.append("t # "+id+"\n");
        for(int j=0;j<V.length;j++)
        {
                s.append("v "+V[j].id+" "+V[j].label+"\n");
                for(int k=0;k<V[j].edges.size();k++)
                {
                	int edgeid=(Integer) V[j].edges.elementAt(k);
                    int node1=E[edgeid].node1;
                    int node2=E[edgeid].node2;
                    //String nodeLabel1=(String)vertexDictionary.labels.get(v[node1].label)+"_"+node1;
                    //String nodeLabel2=(String)vertexDictionary.labels.get(v[node2].label)+"_"+node2;
                    if(node1==j)
                    	edges.append("u "+node1+" "+node2+" "+E[edgeid].bond+"\n");
                }
        }
        s.append(edges.toString());
        return (s.toString().trim());
	}
	
	public Smile getSmiles()
	{
		Smile smile=new Smile(this);
		//System.out.println(smiles.toString()+" "+id);
		return smile ;
	}
	

	private String toStringNormal() {
		// TODO Auto-generated method stub
		StringBuffer g=new StringBuffer("#"+id+"\n"+V.length+"\n");
		for(int i=0;i<V.length;i++)
		{
			g.append(vertexDictionary.labels.get(V[i].label)+"\n");
		}
		g.append(E.length+"\n");
		for(int i=0;i<E.length;i++)
		{
			g.append(E[i].node1+" "+E[i].node2+" "+E[i].bond+"\n");
		}		
		return g.toString()+"\n";		
	}
	
	public void labelAtomsWithJOELibTyper()
	{
		Smile smile=new Smile(this);
		smile.relabelAtoms();
		
		
	}
}
