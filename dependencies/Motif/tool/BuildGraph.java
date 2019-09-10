package tool;

import grank.transform.Hist;
import graph.*;
import joelib2.io.MoleculeIOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import Dictionary.*;

public class BuildGraph {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static Graph[] graphdb;
	public static int pass=0;
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		//String fileName=args[0];
		String fileName=argumentsDictionary.graph;
		Graph[] graphdb=loadGraphs(fileName);
		System.out.println(graphdb[0].toString("normal"));
		//System.out.println(GraphViz.drawGraph(NeighborHood.getNeighborHood(5, 22, 0)));

		/*graphdb[0].print();
		vertexDictionary.print();
		//edgeDictionary.print();
		System.out.println(vertexDictionary.labels.size());
		System.out.println(edgeDictionary.size);
		*/
	}


	public static Graph[] loadGraphs(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner sc=new Scanner(new File(fileName));
		Vector graphs=new Vector();
		pass++;
		while(sc.hasNext())
		{
			
			String id=sc.nextLine().trim();
			if(id.equals(""))
				continue;
			id=id.substring(1).trim();
			id=id.replace("-", "_");
			int numVertex=sc.nextInt();
			Vertex[] V=new Vertex[numVertex];
			Vector H=new Vector<Integer>();
			for(int i=0;i<numVertex;i++)
			{
				String label=sc.next().trim();
				while(label=="")
					label=sc.next().trim();
				//System.out.println(label);
				V[i]=new Vertex(label,i);
				if(label.toLowerCase().equals("h"))
					H.add(i);
			}
			int numEdge=sc.nextInt();
			Edge[] E=new Edge[numEdge];
			for(int i=0;i<numEdge;i++)
			{
				
				String label=sc.nextLine().trim();
				while(label.equals(""))
					label=sc.nextLine().trim();
				//System.out.println(label);
				String[] parts=label.split(" ");
				int node1=new Integer(parts[0]);
				int node2=new Integer(parts[1]);
				V[node1].edges.add(i);
				V[node2].edges.add(i);
				int vlabel1=V[node1].label;
				int vlabel2=V[node2].label;
				if(vlabel1>vlabel2)
				{
					int temp=vlabel1;
					vlabel1=vlabel2;
					vlabel2=temp;
				}

				int bond;
				if(parts.length>2)
				{
					bond=new Integer(parts[2]);
					
				}
				else	
					bond=1;
				E[i]=new Edge(node1,node2,bond,vlabel1+"-"+vlabel2+"-"+bond,i);
			}
			
			if(V[0].stringLabel().equals("a"))
			{
				//new Scanner(System.in).next();
				continue;
			}
											
			graphs.add(new Graph(V,E,id,H,graphs.size()));
		}
		graphdb=(Graph[])graphs.toArray(new Graph[1]);
		//System.out.println("Number of Graphs in Database: "+graphdb.length);
		return (graphdb);
	}

	public static Graph[] loadGraphsCG(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		pass++;
		Scanner sc=new Scanner(new File(fileName));
		Vector graphs=new Vector();
		Vector<Vertex> vertices=new Vector<Vertex>();
		Vector<Edge> edges=new Vector<Edge>();
		Vector H=new Vector<Integer>();
		//Graph g=null;
		boolean first=true;
		String id="";
		while(sc.hasNext())
		{
			String line=sc.nextLine().trim();
			if (line.trim().equals(""))
				continue;
			if (line.startsWith("#"))
				continue;			
			int index=line.indexOf("#");
			//System.out.println(line);
			if(index!=-1)
			{
				if(!first)//(g!=null)
				{
					Vertex[] V=(Vertex[]) vertices.toArray(new Vertex[1]);
					Edge[] E=(Edge[]) edges.toArray(new Edge[1]);
					graphs.add(new Graph(V,E,id,graphs.size()));
				}
				//System.out.println(graphs.size());
				
				vertices=new Vector<Vertex>();
				edges=new Vector<Edge>();
				H=new Vector<Integer>();
				id=line.substring(index+1).trim();
			}
			else
				if(line.charAt(0)=='v')
				{
					first=false;
					String[] parts=line.split(" ");
					vertices.add(new Vertex(new Integer(parts[2]),new Integer(parts[1])));
				}
				else
					if(line.charAt(0)=='e' || line.charAt(0)=='u')
					{
						String[] parts=line.split(" ");
						int node1=new Integer(parts[1]);
						int node2=new Integer(parts[2]);		
						int bond=new Integer(parts[3]);
						//System.out.println(node1);
						Vertex v1=vertices.elementAt(node1);
						Vertex v2=vertices.elementAt(node2);
						v1.edges.add(edges.size());
						v2.edges.add(edges.size());
						int vlabel1=v1.label;
						int vlabel2=v2.label;
						vertices.set(node1, v1);
						vertices.set(node2, v2);
						if(vlabel1>vlabel2)
						{
							int temp=vlabel1;
							vlabel1=vlabel2;
							vlabel2=temp;
						}
						edges.add(new Edge(node1,node2,bond,vlabel1+"-"+vlabel2+"-"+bond,edges.size()));
					}	
		}
		//System.out.println(g);
		if(vertices!=null && edges!=null && !first)//g!=null)
		{
			Vertex[] V=(Vertex[]) vertices.toArray(new Vertex[1]);
			Edge[] E=(Edge[]) edges.toArray(new Edge[1]);
			graphs.add(new Graph(V,E,id,graphs.size()));
		}
		return (Graph[])graphs.toArray(new Graph[1]);
	}


	
}
