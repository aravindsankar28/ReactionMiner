package tool;

import graph.Graph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class DrawGraph {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		StringBuffer s=new StringBuffer("");
		String graphPath=args[0];
		String path=graphPath.substring(0,graphPath.lastIndexOf("."));
		GraphViz.checkDir(path);
		Graph[] graphdb=BuildGraph.loadGraphs(graphPath);
		//=BuildGraph.loadGraphs("");
		PrintWriter out=null;
		PrintWriter bat=new PrintWriter(path+"/createGIF.sh");
		for(Graph g: graphdb)
		{
				s.append(GraphViz.drawGraph(g));
				s.append("\n");
				out=new PrintWriter(path+"/neato/"+g.id+".neato");
				out.println(GraphViz.drawGraph(g));
				bat.println("neato -Tgif \"neato/"+g.id+".neato\" -o \"gif/"+g.id+".gif\"");		
				out.close();
		}
		bat.close();
	}


}
