package tool;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

import graph.Graph;

public class selectGraphs {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Graph[] database=BuildGraph.loadGraphs(args[0]);
		Vector<Graph> selected=new Vector<Graph>();
		HashMap<String,Integer> id=new HashMap();
		int minSize=Integer.MAX_VALUE;
		for(int i=1;i<args.length-1;i++)
		{
			String[] ids=FileUtils.getFileContents(args[i]).trim().split("\n");

			//minSize=Math.min(minSize, ids.length);
		}
		//int size=(int)(0.3*minSize);
		for(int i=1;i<args.length-1;i++)
		{
			String[] ids=FileUtils.getFileContents(args[i]).trim().split("\n");

			for(int j=0;j<ids.length;j++)
			{
				id.put(ids[j].replace("-", "_"), 1);
			}
			
		}		
		for (Graph g:database)
		{
			//System.out.println(g.id);
			if(id.get(g.id)!=null)
				selected.add(g);
		}
		
		StringBuffer s=new StringBuffer("");
		for(Graph g:selected)
		{
			s.append(g.toString());
		}
		FileUtils.writeToFile(args[args.length-1], s.toString().trim());
	}

}
