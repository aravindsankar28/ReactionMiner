package Harvard;

import java.io.FileNotFoundException;

import graph.Graph;
import tool.BuildGraph;
import tool.FileUtils;

public class ChunkIntoSubsets {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Graph[] graphdb=BuildGraph.loadGraphs(args[0]);
		int size=new Integer(args[1]);
		int c=0;
		int part=0;
		StringBuffer s=new StringBuffer("");
		for (Graph g:graphdb )
		{
			s.append(g.toString()+"\n");
			c++;
			if (c==size)
			{
				FileUtils.writeToFile(args[0]+".part"+part, s.toString());
				s=new StringBuffer("");
				c=0;
				part++;
			}
		}
		FileUtils.writeToFile(args[0]+".part"+part, s.toString());

	}

}
