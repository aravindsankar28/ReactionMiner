package tool;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import Dictionary.vertexDictionary;

import grank.transform.Hist;
import graph.Graph;
import graph.Vertex;

public class GraphChecker {

	static int[] map;
	static int[] available;
	static Graph g1;
	static Graph g2;
	public static void main(String[] args) throws FileNotFoundException
	{
		BuildGraph.loadGraphs("ca.txt");
		Graph[] graphdb=BuildGraph.loadGraphsCG("ca/subHist/fp/10-518,96,0.0-59.fp");
		getNonSubGraphs(graphdb);
		//System.out.println(isSubGraph(graphdb[5], graphdb[1]));


		
	}
	static public boolean isSubGraph(Graph g1, Graph g2)
	{
		GraphChecker.g1=g1;
		GraphChecker.g2=g2;
		int atom1=leastFrequent(g1);
		int atom2=leastFrequent(g2);
		Hist h1=getHist(g1);
		Hist h2=getHist(g2);
		if(!h2.contains(h1))
			return false;
		//System.out.println(h2.contains(h1));
		map=new int[g1.V.length];
		available=new int[g2.V.length];
		for(int i=0;i<g1.V.length;i++)
		{
			//System.out.println("i:"+i);
			for(int j=0;j<g2.V.length;j++)
			{
				Arrays.fill(map,-1);
				Arrays.fill(available,1);
				if (checkMapping(i,j))
					return true;
			}
		}
		//System.out.println("going back here");
		return false;
		
	}

	private static boolean checkMapping(int vid1, int vid2) {
		// TODO Auto-generated method stub
		Vertex v1=g1.V[vid1];
		Vertex v2=g2.V[vid2];
		//System.out.println(Arrays.toString(map));
		//new Scanner(System.in).next();
		if(available[vid2]==0)
			return false;
		if(map[vid1]!=-1)
		{
			return true;
		}
		if(v1.label!=v2.label)
		{
			return false;
		}
		if(v2.edges.size()<v1.edges.size())
		{
			return false;
		}
		else
		{

			map[vid1]=vid2;
			available[vid2]=0;
			//System.out.println("vid1:"+vid1+" vid2:"+vid2);
			int[] set1=fetchNeighbors(g1,vid1);
			int[] set2=fetchNeighbors(g2,vid2);
			for(int i=0;i<set1.length;i++)
			{
				for(int j=0;j<set2.length;j++)
					checkMapping(set1[i],set2[j]);
						//return false;
			}
		}
		//map[vid1]=-1;
		if(mapped())
			return true;
		else 
			return false;
		
	}

	private static boolean mapped() {
		// TODO Auto-generated method stub
		for(int i=0;i<map.length;i++)
			if(map[i]==-1)
				return false;
		
		return true;
	}
	private static int[] fetchNeighbors(Graph g, int vid) {
		// TODO Auto-generated method stub
		Vertex v=g.V[vid];
		int[] neighbors=new int[v.edges.size()];
		for(int i=0;i<v.edges.size();i++)
		{
			//System.out.println(i);
			int node1=g.E[(Integer)v.edges.elementAt(i)].node1;
			int node2=g.E[(Integer)v.edges.elementAt(i)].node2;
			if(node1!=v.id)
			{
				neighbors[i]=node1;
			}
			else
				neighbors[i]=node2;
		}
		return neighbors;
	}
	private static Hist getHist(Graph g) {
		// TODO Auto-generated method stub
		
		int[] hist=new int[vertexDictionary.labels.size()];
		for(int i=0;i<g.V.length;i++)
		{
			hist[g.V[i].label]++;
		}
		
		return new Hist(g.id+"",hist);
	}
	private static int leastFrequent(Graph g) {
		// TODO Auto-generated method stub
		HashMap m=new HashMap();
		for(int i=0;i<g.V.length;i++)
		{
			Integer val=(Integer) m.get(g.V[i].label);
			if(val==null)
				m.put(g.V[i].label, 1);
			else
				m.put(g.V[i].label, val+1);
		}
		//System.out.println(m.toString());
		int min=Integer.MAX_VALUE;
		int atom=0;
		Integer[] keys=(Integer[])(m.keySet().toArray(new Integer[1]));
		for(int i=0;i<m.size();i++)
		{
			int pos=keys[i];
			int val=(Integer)m.get(pos);
			if(val<min)
			{
				min=val;
				atom=pos;
			}
		}
		return atom;
	}
	public static Integer[] getNonSubGraphs(Graph[] graphdb) {
		// TODO Auto-generated method stub
		Vector nonSG=new Vector();
		for(int i=0;i<graphdb.length;i++)
		{
			boolean nonSub=true;
			if(graphdb[i].V.length<6)
				continue;
			for(int j=0;j<graphdb.length && nonSub;j++)
			{
				if(i!=j && graphdb[i]!=null && graphdb[j]!=null)
					if(isSubGraph(graphdb[i], graphdb[j]))
					{
						nonSub=false;
						//System.out.println(j+">"+i);
					}
			}
			if(nonSub)
			{
				nonSG.add(i);
				//new Scanner(System.in).next();
			}
			
			//System.out.println(i+" "+nonSub);
			//System.out.println(Arrays.toString(map));
			//new Scanner(System.in).next();
		}

		return (Integer[])(nonSG.toArray(new Integer[1]));
	}
	public static Integer[] getNonDuplicates(Vector<Graph> sigs) {
		Vector nonDup=new Vector();
		for(int i=0;i<sigs.size();i++)
		{
			boolean nonDp=true;
			for(int j=i+1;j<sigs.size() && nonDp;j++)
			{
					if(isSubGraph(sigs.elementAt(i), sigs.elementAt(j)) && isSubGraph(sigs.elementAt(j), sigs.elementAt(i)))
					{
						nonDp=false;
						sigs.removeElementAt(i--);
						//System.out.println(j+">"+i);
					}
			}
			if(nonDp)
			{
				nonDup.add(i);
				//new Scanner(System.in).next();
			}
			
			//System.out.println(i+" "+nonSub);
			//System.out.println(Arrays.toString(map));
			//new Scanner(System.in).next();
		}

		return (Integer[])(nonDup.toArray(new Integer[1]));
	}
	
	
}
