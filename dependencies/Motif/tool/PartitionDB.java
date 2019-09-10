package tool;

import graph.Graph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class PartitionDB {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
	String fileName="aido99_all";
        Graph[] graphdb=BuildGraph.loadGraphs(fileName+".txt");
        
        
        //s.append("graph G {\nnode[shape=plaintext width=.05 height=.1 fontsize=12]\nedge[len=1 ]\ngraph[size=5,5]\n");
        
        //System.out.println(graphdb[21450].id);
        int size=new Scanner(System.in).nextInt();
        //for(int size=5000;size<graphdb.length;size+=5000)
        while(size!=-1)
        {
        	
        	StringBuffer s=new StringBuffer("");
	        PrintWriter out=new PrintWriter(fileName+"_"+size+".txt");
	        for(int i=0;i<size;i++)
	        {
	        	s.append(graphdb[i].toString("normal")+"\n");
	        }
	        out.println(s.toString().trim());
	        out.close();    
	        size=new Scanner(System.in).nextInt();
        }
	}

}
