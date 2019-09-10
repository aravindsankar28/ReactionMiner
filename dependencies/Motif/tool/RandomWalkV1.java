package tool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

import graph.*;
import Dictionary.*;

public class RandomWalkV1  extends RandomWalk{


	public RandomWalkV1(double restart)
	{
		super(restart);
	}
	public int[][] walk(Graph g)
	{
		//double[][] adj=g.getAdjMatrix();
		//System.out.println("Performing Random Walk Version 1");
		Vertex[] V=g.V;
		Edge[] E=g.E;
		
		int offset=vertexDictionary.labels.size();
		double[][] profile=new double[V.length][offset+topEdges.size()];
		
		for(int i=0;i<V.length;i++)
		{
			//System.out.println(i);
			Arrays.fill(profile[i],0); //initial profile counter to 0
			if(V[i].edges.size()>0)
			{
				double dist=100;
				Vertex curr=V[i]; //current Vertex
				int currSize=1;
				profile[i][curr.label]++;
				while(dist>argumentsDictionary.delta)
				{
					
					double[] origProfile=profile[i].clone();
					int origSize=currSize;
					for(int j=0;j<10;j++)
					{
						//System.out.println(j);
						double prob=Math.random();
						if(prob<=restart) //if probability is less than restart, go back to starting vertex
						{
							curr=V[i];
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
								profile[i][edgePos+offset]++;
							}
							else
							{
								profile[i][curr.label]++;
							}
							currSize++;
						}
					}
					dist=L1Dist(origProfile,profile[i],origSize,currSize);
					//System.out.println(dist);
				}
				for(int j=0;j<profile[i].length;j++)
					profile[i][j]/=currSize;
			}
		}

		return approximate(profile,10);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}

}
