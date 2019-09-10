package tool;

import graph.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

public class DivideCompoundsByClass {

	public static void main(String[] args) throws FileNotFoundException
	{
		String graphClass="active";
		String dir="cancer/Yeast/";
		String graphName="167";
		String fileName=dir+graphName+"_outcome.txt";
		Graph[] graphs=BuildGraph.loadGraphs(dir+graphName+".txt");
		StringBuffer graphrep=new StringBuffer("");
		StringBuffer classify=new StringBuffer("");
		Scanner s=new Scanner(new File(fileName));
		//s.useDelimiter(",");
		Vector<String> ids=new Vector<String>();
		while (s.hasNext())
		{
			String id=s.next().trim();
			String currClass=s.next().trim();//.substring(1);
			//System.out.println(id);
			//System.out.println(currClass);
			if(currClass.equals(graphClass))
			{
				ids.add(id);
				graphrep.append(id+"\n");
			}
		}
		int num=ids.size();
		for(int j=0;j<num;j++)
		{
			for(int i=0;i<graphs.length;i++)
			{
			//System.out.println(graphs[i].id);
				if(ids.elementAt(j).equals(graphs[i].id))
				{
					classify.append(graphs[i].toString("normal"));
				}
			}
		}
		
		FileUtils.writeToFile(dir+"ca.txt",graphrep.toString() );
		graphClass="inactive";
		graphrep=new StringBuffer("");
		s=new Scanner(new File(fileName));
		//s.useDelimiter(",");
		ids=new Vector<String>();
		while (s.hasNext())
		{
			String id=s.next().trim();
			String currClass=s.next().trim();//.substring(1);
			//System.out.println(id);
			//System.out.println(currClass);
			if(currClass.equals(graphClass))
			{
				ids.add(id);
				graphrep.append(id+"\n");
			}
		}
		for(int j=0;j<num;j++)
		{
			for(int i=0;i<graphs.length;i++)
			{
			//System.out.println(graphs[i].id);
				if(ids.elementAt(j).equals(graphs[i].id))
				{
					classify.append(graphs[i].toString("normal"));
				}
			}
		}
		
		FileUtils.writeToFile(dir+"ci.txt",graphrep.toString() );
		FileUtils.writeToFile(dir+graphName+"_classify.txt",classify.toString() );
	}
	
}
