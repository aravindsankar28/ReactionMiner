package Dictionary;

import graph.Graph;
import graph.Vertex;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Vector;

import tool.BuildGraph;
import tool.FileUtils;


public class vertexDictionary {

	public static HashMap<Integer,String> labels=new HashMap();
	//public static int size=0;
	public static Vector freq=new Vector();
	//public static int total=0;
	public static int addID(String label)
	{
		label=label.toLowerCase();
		//total++;
		/*
		Integer id=(Integer)labels.get(label);
		if(id==null)
		{
			labels.put(label,labels.size());
			freq.add(1);
			return labels.size()-1;			
		}
		int currVal=(Integer)(freq.elementAt(id));
		freq.set(id, currVal+1);
		return id;
		*/
		for(int i=0;i<labels.size();i++)
		{
			if(label.equals((String)labels.get(i)))
			{
				int currVal=(Integer)(freq.elementAt(i));
				if(BuildGraph.pass==1)
					freq.set(i, currVal+1);
				return i;
			}
		}
		labels.put(labels.size(), label);
		if(BuildGraph.pass==1)
			freq.add(1);
		else
			freq.add(0);
		return labels.size()-1;
	}
	
	public static void print()
	{
		System.out.println(labels.toString());
		System.out.println(freq.toString());
		System.out.println(Arrays.toString(getProb()));
		HashMap<Integer,Integer> l=new HashMap();
		for(Graph g:BuildGraph.graphdb)
		{
			for(Vertex v:g.V)
			{
				if(l.containsKey(v.label))
					l.put(v.label,l.get(v.label)+1);
				else
					l.put(v.label,1);
			}
		}
		Iterator it=l.keySet().iterator();
		while(it.hasNext())
		{
			System.out.print(l.get(it.next())+",");
		}
	}
	
	public static double[] getProb()
	{
		double[] prob=new double[labels.size()];
		int total=0;
		for(int i=0;i<labels.size();i++)
		{
			total+=(Integer)freq.get(i);
		}		
		for(int i=0;i<labels.size();i++)
		{
			prob[i]=(Integer)freq.get(i)*1.0/total;
		}
		return prob;
	}

	public static void reset()
	{
		BuildGraph.pass=1;
		freq=new Vector();
		freq.setSize(labels.size());
		Collections.fill(freq,0);
	}
	public static HashMap getTop(int k) {
		// TODO Auto-generated method stub
		//TreeMap p=new TreeMap();
		HashMap<Integer,Integer> topAtoms=new HashMap<Integer,Integer>();
		if (argumentsDictionary.loadTopAtoms!="")
		{
			System.out.println("Reloading top atoms");
			String content=FileUtils.getFileContents(argumentsDictionary.loadTopAtoms).trim().split("\n")[0];
			for (String val:content.trim().split(" "))
			{
				for (Integer a:vertexDictionary.labels.keySet())
				{
					if (vertexDictionary.labels.get(a).equals(val))
						topAtoms.put(a,1);
				}
			}
		}
		else
		{
			PriorityQueue<Atom> p=new PriorityQueue<Atom>(k,new atomComparator());
			//System.out.println("freq: "+freq.size());
			for(int i=0;i<freq.size();i++)
				p.add(new Atom((Integer)freq.elementAt(i),i));
			for(int i=0;i<k;i++)
			{
				Atom a=p.poll();
				topAtoms.put(a.index,a.freq);
			}
		}
		//System.out.print("TopAtoms: ");
		//System.out.println(topAtoms.toString());
		return topAtoms;
	}
}
