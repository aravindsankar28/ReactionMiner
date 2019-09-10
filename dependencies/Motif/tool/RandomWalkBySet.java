package tool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math.stat.Frequency;

import grank.transform.Hist;
import graph.*;
import Dictionary.*;

public class RandomWalkBySet  extends RandomWalk{

	//HashMap atomHists=new HashMap();
	Vector[] histSet;
	Graph[] graphdb;
	//double[][][] profile=new double[vertexDictionary.labels.size()][][];
	public RandomWalkBySet(double restart)
	{
		super(restart);
		
	}
	public Vector[] getHistsBySet(Graph[] graphdb) {
		// TODO Auto-generated method stub
		//profile=new double[V.length][offset+topEdges.size()];
		int offset=vertexDictionary.labels.size();
		histSet=new Vector[offset];
		this.graphdb=graphdb;
		for(int i=0;i<offset;i++)
		{
			histSet[i]=new Vector<Hist>();
		}
		for(int i=0;i<graphdb.length;i++)
		{
			walkSet(i);
		}
		//for(int i=0;i<histSet.length;i++)
			//System.out.println(vertexDictionary.labels.get(i)+"-"+histSet[i].size());
		return histSet;
	}
	public static String getFeatures()
	{
		
		//HashMap<String, Integer> features=new HashMap<String,Integer>();
		//System.out.println("-----------------------------------");
		StringBuffer features=new StringBuffer("");
		HashMap topAtoms=vertexDictionary.getTop(argumentsDictionary.topAtoms);
		for (Object e:vertexDictionary.labels.keySet())
		{
			if(topAtoms.get((Integer)e)!=null)
				features.append(vertexDictionary.labels.get((Integer)e)+" ");
		}
		features.append("\n");
		for (Object e:topEdges.keySet())
		{
			String edgeString=edgeDictionary.types.get((Integer) e);
			
			String[] parts=edgeString.trim().split("-");
			String edge="";
			if(parts.length==3)
			{
				edge=(vertexDictionary.labels.get(new Integer(parts[0]))+"-"+
						new Integer(parts[2])+"-"+
						vertexDictionary.labels.get(new Integer(parts[1])));
			}
			
			else if(parts.length==2)
			{
				edge=(vertexDictionary.labels.get(new Integer(parts[0]))+"-"+
						vertexDictionary.labels.get(new Integer(parts[1])));
			}
			features.append(edge+":"+((Integer)topEdges.get((Integer)e)+vertexDictionary.labels.size())+" ");
		}
		//System.out.println("-----------------------------------");
		//features.append("\n");
		for (Object e:vertexDictionary.labels.keySet())
		{
			//if(topAtoms.get((Integer)e)==null)
				features.append(vertexDictionary.labels.get((Integer)e)+":"+(Integer) e+" ");
		}
		return features.toString();
	}
	public void walkSet(int index)
	{
		//double[][] adj=g.getAdjMatrix();
		//System.out.println("Performing Random Walk Version 1");
		Graph g=graphdb[index];
		Vertex[] V=g.V;
		Edge[] E=g.E;
		
		int offset=vertexDictionary.labels.size();
		
		
		for(int i=0;i<V.length;i++)
		{
			//System.out.println(i);
			//Arrays.fill(profile[i],0); //initial profile counter to 0
			//int tours=0;
			double[] profile=new double[offset+topEdges.size()];
			double[] origProfile=null;
			if(V[i].edges.size()>0)
			{
				double dist=100;
				int step=0;
				Vertex curr=V[i]; //current Vertex
				int label=curr.label;
				int currSize=1;
				int origSize=1;
				profile[curr.label]++;
				while(dist>argumentsDictionary.delta)
				{			
					origProfile=profile.clone();
					origSize=currSize;
					
						//System.out.println(j);
						double prob=Math.random();
						if(prob<=restart) //if probability is less than restart, go back to starting vertex
						{
							curr=V[i];
							profile[curr.label]++;
							currSize++;
							//System.out.println("restart happened"+i);
						}
						else
						{
							prob-=restart;
							double delta=(1-restart)/(curr.edges.size());
							int next=new Double(Math.floor(prob/delta)).intValue(); //edge ID
							Edge selected=E[(Integer)curr.edges.elementAt(next)];
							curr=V[selected.getNeighbor(curr.id)];//next Vertex
							
							Integer edgePos=(Integer)topEdges.get(selected.type);
							if(edgePos!=null)
							{
								profile[edgePos+offset]++;
							}
							else
							{
								profile[curr.label]++;
							}
							currSize++;
						}
					
				
					dist=L1Dist(origProfile,profile,origSize,currSize);
					//System.out.println(dist);
				}
				for(int j=0;j<profile.length;j++)
					profile[j]/=currSize;
				histSet[label].addElement(new Hist(g.id+"-"+index+"-"+i, approximate(profile,10),label));
			}
		}
		
		//return approximate(profile,10);
	}


}
