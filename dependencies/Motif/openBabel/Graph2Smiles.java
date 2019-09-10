package openBabel;

import java.io.FileNotFoundException;
import java.io.IOException;

import graph.Graph;
import tool.BuildGraph;
import tool.FileUtils;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFile;
import ctree.util.Opt;

public class Graph2Smiles {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static String getSmilesString(LGraph g) throws IOException
	{
		LGraph[] graphs = new LGraph[1];
		graphs[0] = g;
		LGraphFile.saveLGraphs(graphs, "temp.txt");		
		Graph x = BuildGraph.loadGraphs("temp.txt")[0];		
		return x.getSmiles().getSmileRep()+"\n";						
	}
	
	
	public static String getSmilesString(String graph_file) throws FileNotFoundException
	{
		
		Graph[] graphdb=BuildGraph.loadGraphs(graph_file);
		StringBuffer s=new StringBuffer("");
		for (Graph g:graphdb)
		{
			s.append(g.getSmiles().getSmileRep()+"\n");
		}
		
		return s.toString();
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		//Opt opt=new Opt(args);
		//Graph[] graphdb=BuildGraph.loadGraphs("../../data/molStereo_Hadded_PH_CoA/"+"/C00090.mol");
		Graph[] graphdb=BuildGraph.loadGraphs("../../data/mol/"+"/C00047.mol");
		StringBuffer s=new StringBuffer("");
		for (Graph g:graphdb)
		{
			s.append(g.getSmiles().getSmileRep()+"\n");
		}
		System.out.println(s.toString());
	}

}
