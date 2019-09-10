package Analysis;


import graph.*;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;

import ctree.util.Opt;

import Dictionary.argumentsDictionary;
import Dictionary.edgeDictionary;
import Dictionary.vertexDictionary;

import tool.BuildGraph;
import tool.FileUtils;
import tool.RandomWalk;
import tool.RandomWalkBySet;

public class GlobalFeatureDistribution {

	public static void main(String[] args) throws FileNotFoundException
	{
		Opt opt=new Opt(args);
		String background=opt.getArg(0);
		Graph[] graphdb=BuildGraph.loadGraphs(background);
		argumentsDictionary.topAtoms=opt.getInt("nta");
		RandomWalk r=new RandomWalkBySet(argumentsDictionary.restart);

		Graph[][] cluster=new Graph[opt.args()-1][];
		int[] vsize=new int[cluster.length];
		int[] esize=new int[cluster.length];
		HashMap<Integer,String> edgeTypes=new HashMap<Integer,String>();
		for (int i=1;i<opt.args();i++)
		{
			String[] ids=FileUtils.getFileContents(args[i]).split("\n");
			cluster[i-1]=new Graph[ids.length];
			int pos=0;
			for (String id: ids)
				for(Graph g:graphdb)
				{
					if (g.id.equals(id))
					{
						cluster[i-1][pos++]=g;
						vsize[i-1]+=g.V.length;
						esize[i-1]+=g.E.length;
						break;
					}
				}
			System.out.println("Completed reading cluster "+i);
		}
		
		double[][] profile=new double[cluster.length][vertexDictionary.labels.size()+r.topEdges.size()];
		for(int i=0;i<cluster.length;i++)
		{
			for(int j=0;j<cluster[i].length;j++)
			{
				Graph g=cluster[i][j];
				for(Vertex v:g.V)
				{
					profile[i][v.label]+=1.0/vsize[i];
				}
				for(Edge e:g.E)
				{
					
					Integer edgePos=(Integer)r.topEdges.get(e.type);
					if(edgePos!=null)
					{
						profile[i][edgePos+vertexDictionary.labels.size()]+=1.0/esize[i];
						edgeTypes.put(edgePos+vertexDictionary.labels.size(), e.toString());
					}

				}
			}
		}
		StringBuffer s=new StringBuffer("");
		for(int i=0;i<profile[0].length;i++)
		{
			String type=i<vertexDictionary.labels.size()?vertexDictionary.labels.get(i):
				edgeTypes.get(i);
			s.append(type+" ");
			for(int j=0;j<profile.length;j++)
			{
				s.append(profile[j][i]+" ");
			}
			s.append("\n");
		}
	
		System.out.println(s.toString());
	}
}
