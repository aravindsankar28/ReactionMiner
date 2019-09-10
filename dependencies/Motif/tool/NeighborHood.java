package tool;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

import java.util.HashMap;
import java.util.Vector;

public class NeighborHood {

	
	static HashMap vertexMap;
	static HashMap map;
	static int size;
	static Vector<Vertex> vertices;
	public static Graph[] getNeighborHoods(int radius, Integer[] nodes, int gid) {
		// TODO Auto-generated method stub
		map=new HashMap();
		Vector<Graph> subgraphs=new Vector<Graph>();
		//int distinct=0;
		for(int i=0;i<nodes.length;i++)
		{
			if(!map.containsKey(nodes[i]))
			{
				//distinct++;
				Graph subgraph=getNeighborHood(radius, nodes[i], gid);
				if(subgraph.V.length<40)
					subgraphs.add(subgraph);
				map.putAll(vertexMap);
			}
					
		}
		//System.out.println(distinct);
		return subgraphs.toArray(new Graph[1]);
	}
	public static Graph getNeighborHood(int radius, int vid, int gid) {
		// TODO Auto-generated method stub
		vertexMap=new HashMap();
		size=0;
		vertices=new Vector<Vertex>();
		Graph g=BuildGraph.graphdb[gid];
		//Graph neighborhood=new Graph();
		String id=g.id+"_"+vid;
		explore(g,vid,radius);
		Vertex[] V=vertices.toArray(new Vertex[1]);
		Vector<Edge> edges=new Vector<Edge>();
		int count=0;
		for(int i=0;i<g.E.length;i++)
		{
			Edge e=g.E[i];
			int node1=e.node1;
			int node2=e.node2;
			Integer map1=(Integer) vertexMap.get(node1);
			Integer map2=(Integer) vertexMap.get(node2);
			if(map1!=null && map2!=null)
			{
				edges.add(new Edge(e,map1,map2,count));
				V[map1].edges.add(count);
				V[map2].edges.add(count++);
			}
		}
		Edge[] E=edges.toArray(new Edge[1]);
		//System.out.println("-----------");
		//System.out.println(neighborhood.toString("CG"));
		Graph neighborhood=new Graph(V,E,id,-1);
		return neighborhood;
	}

	private static void explore(Graph g, int vid, int radius) {
		// TODO Auto-generated method stub
		if(radius<0)
			return;
		Vertex v=g.V[vid];
		vertices.add(new Vertex(v,size));
		vertexMap.put(vid, size++);
		for(int i=0;i<v.edges.size();i++)
		{
			int edgeid=(Integer)v.edges.elementAt(i);

				Edge e=g.E[edgeid];
				int node1=e.node1;
				int node2=e.node2;
				if(vertexMap.get(node1)==null)
				{
					explore(g,node1,radius-1);
				}
				else 
					if(vertexMap.get(node2)==null)
					{
						explore(g,node2,radius-1);
					}
		}
		
	}
}
