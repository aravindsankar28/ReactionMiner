package ctree.toolbox;

import graph.Graph;

import java.io.IOException;
import java.util.Scanner;

import tool.FileUtils;

import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFile;
import ctree.util.Opt;

public class FrequencyDistribution {

	/**
	 * @param args
	 */
	double[][] distribution;
	
	public FrequencyDistribution(String[] args) throws IOException
	{
		Opt opt=new Opt(args);
		LGraph[][] cluster=new LGraph[opt.args()-2][];

		LGraph[] queries = LGraphFile.loadLGraphs(opt.getArg(opt.args()-1));
		LGraph[] graphdb = LGraphFile.loadLGraphs(opt.getArg(0));
		distribution=new double[queries.length][opt.args()-2];
		for (int i=1;i<opt.args()-1;i++)
		{
			String[] ids=FileUtils.getFileContents(args[i]).split("\n");
			cluster[i-1]=new LGraph[ids.length];
			int pos=0;
			for (String id: ids)
				for(LGraph g:graphdb)
				{
					if (g.getId().equals(id))
					{
						cluster[i-1][pos++]=g;
						break;
					}
				}
			System.out.println("Completed reading cluster "+i);
		}
		

		do{
			for(int i=0;i<queries.length;i++)
			{
				for(int j=0;j<cluster.length;j++)
					distribution[i][j]=1.0*subgraphFrequencyCounter.count(cluster[j], queries[i])/cluster[j].length;
			}
			
			for(int i=0;i<queries.length;i++)
			{
				for(int j=0;j<cluster.length;j++)
					System.out.print(Math.round(distribution[i][j]*100)+" ");
				System.out.println();
			}
			if(opt.hasOpt("loop"))
			{
				System.out.println("Make changes in query file and press \"OK\"");
				new Scanner(System.in).next();
				queries = LGraphFile.loadLGraphs(opt.getArg(opt.args()-1));
			}
		} while(opt.hasOpt("loop"));

	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FrequencyDistribution fdb=new FrequencyDistribution(args);
	}

}
