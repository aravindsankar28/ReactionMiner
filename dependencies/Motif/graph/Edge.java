package graph;
import Dictionary.*;
public class Edge {

	public int node1;
	public int node2;
	public int bond;
	public int type;
	public int id;

	public Edge(int node1, int node2, int bond, String label,int id) {
		// TODO Auto-generated constructor stub
		assignNodes(node1,node2,id);

		this.bond=bond;
		type=edgeDictionary.addID(label);

	}
	public Edge(Edge e, Integer map1, Integer map2, int id) {
		// TODO Auto-generated constructor stub
		this.node1=map1;
		this.node2=map2;
		this.id=id;
		this.bond=e.bond;
		this.type=e.type;
	}
	private void assignNodes(int node1, int node2,int id) {
		// TODO Auto-generated method stub
		if(node1<node2)
		{
			this.node1=node1;
			this.node2=node2;
		}
		else
		{
			this.node1=node2;
			this.node2=node1;			
		}		
		this.id=id;
	}
	public int getNeighbor(int id) {
		// TODO Auto-generated method stub
		if (id==node1)
			return node2;
		else
			return node1;
		
	}
	
	public String toString()
	{
		String t= (String) edgeDictionary.types.get(type);
		t.split("-");
		String desc="";
		desc+=(vertexDictionary.labels.get(new Integer(t.split("-")[0]))+"-"+t.split("-")[1]+"-"+vertexDictionary.labels.get(new Integer(t.split("-")[2])));
		//System.out.println(desc);
		return desc;
			
	}
	
}
