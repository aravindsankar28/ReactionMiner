package tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import ctree.util.Opt;

import Dictionary.*;
import grank.transform.Hist;
import graph.*;

public class GraphViz {

	public static String graphPath;
	public static String background;
	public static Hist[] sigHists;
	public static String head="graph G {\nnode[shape=plaintext width=.05 height=.1 fontsize=12]\nedge[len=1 ]\ngraph[size=5,5]\n";
	public static boolean filterSubGraphs;//default value: false
	static Vector<Graph> sigs=new Vector<Graph>();
	static String path;
	static HashMap topEdges;
	public static void main(String[] args) throws IOException
	{
		graphPath=args[1];
		background=args[0];
		Opt opt=new Opt(args);
		path=graphPath.substring(0,graphPath.lastIndexOf("."))+"/subHist/"+"fp/" ;
		System.out.println(graphPath);
		System.out.println(background);
		System.out.println(path);
		//StringBuffer s=new StringBuffer("");
		BuildGraph.loadGraphs(background);
		//Collections.sort(vertexDictionary.freq);
		//Collections.sort(edgeDictionary.freq);
		//System.out.println(vertexDictionary.freq);
		//System.out.println(edgeDictionary.freq);
		//new Scanner(System.in).next();
		argumentsDictionary.restart=0.25;
		argumentsDictionary.topAtoms=opt.getInt("nta");
		RandomWalk r=new RandomWalkBySet(argumentsDictionary.restart);
		System.out.println(vertexDictionary.freq);
		topEdges=r.topEdges;
		sigHists=Hist.loadHists(graphPath.substring(0,graphPath.lastIndexOf("."))+"/sig.hist");
		//String[] graphs=FileUtils.getFilesWithExt("fp",path);
		filterSubGraphs=false;
		for(int i=0;i<sigHists.length;i++)
		{
			//System.out.println("coming here");
			//System.out.println(graphs[i]);
			//String fileName=path+graphs[i];
			//s.append("cd \""+graphs[i].substring(0,graphs[i].lastIndexOf("."))+"_data\"\ncreateGIF.bat\ncd ..\n");
			drawFromCG(i);		
		}
		Integer[] ids=GraphChecker.getNonDuplicates(sigs);
		if(ids[0]==null)
			return;
		path+="motif/";
		checkDir(path);
		PrintWriter out=null;
		PrintWriter bat=new PrintWriter(path+"createGIF.sh");
		StringBuffer sigGraphs=new StringBuffer("");
		for(int i=0;i<ids.length;i++)
		{
			//System.out.println(i+" "+Arrays.toString(ids)+graphdb[0]);
			out=new PrintWriter(path+"/neato/"+sigs.elementAt(ids[i]).id+".neato");
			out.println(drawGraph(sigs.elementAt(ids[i])));
			sigGraphs.append(sigs.elementAt(ids[i]).toString());
			bat.println("neato -Tgif \"neato/"+sigs.elementAt(ids[i]).id+".neato\" -o \"gif/"+sigs.elementAt(ids[i]).id+"-"+i+".gif\"");		
			out.close();
		}
		FileUtils.writeToFile(graphPath.substring(0,graphPath.lastIndexOf("."))+"/significantGraphs.txt", sigGraphs.toString());
		bat.close();
		/*FileUtils.writeToFile(path+"/createAllGIF.bat", s.toString());
		String path=graphPath.substring(0,graphPath.lastIndexOf("."))+"/subHist/" ;
		BuildGraph.loadGraphs("ca.txt");
		//Graph[] graphdb=BuildGraph.loadGraphsCG("cm/subHist/5-706,182,0.0-57.cg");
		drawFromCG(path+"5-706,182,0.0-57.cg");*/
	}
	/*
	public static void drawFromOrig(String fileName) throws FileNotFoundException
	{
		System.out.println("Loading Graphs...");
		Graph[] graphdb=BuildGraph.loadGraphs(fileName);
		System.out.println("Graphs loaded...");
		drawGraphs(graphdb,fileName.substring(0,fileName.lastIndexOf(".")));

	}
	*/
	public static void drawFromCG(int index) throws FileNotFoundException
	{
		String fileName=path+sigHists[index].id+".fp";
		if(BuildGraph.graphdb==null)
			init(); //initializes the dictionary
		
		if(!new File(fileName).exists())
			return;
		//System.out.println(fileName);
		Graph[] graphdb=BuildGraph.loadGraphsCG(fileName);
		if(graphdb[0]!=null)
		{
			//System.out.println(sigHists[index].id);
			//System.out.println(Arrays.toString(profile));
			//System.out.println(Arrays.toString(sigHists[index].hist));
			//System.out.println();			
			drawGraphs(graphdb,index);//fileName.substring(fileName.lastIndexOf("/"),fileName.lastIndexOf(".")));
		}
		else{
			//System.out.println("null graph");
		}
	}
	
	private static void init() throws FileNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("Loading Graphs...");
		BuildGraph.loadGraphs(background);
		System.out.println("Graphs loaded...");
	}

	public static void drawGraphs(Graph[] graphdb, int index) throws FileNotFoundException
	{
		
		if(graphdb.length<1)
		{
			System.out.println("null set");
			return;
		}
		System.out.println("coming here");
		Integer[] ids=null;
		if(filterSubGraphs) 
		{
			ids=GraphChecker.getNonSubGraphs(graphdb);
			if(ids[0]==null)
				return;
		}
		System.out.println(sigHists[index].id);
		//System.out.println(Arrays.toString(profile));
		System.out.println(Arrays.toString(sigHists[index].hist));
		System.out.println();			
		//for(int i=0;i<ids.length;i++)
		for(int i=0;i<graphdb.length;i++)
		{
			graphdb[i].id=sigHists[index].id+"_"+graphdb[i].id;
			//if(Math.abs(calcScore(graphdb[ids[i]],index))<=5)
			if(calcScore(graphdb[i],index)==1)
				sigs.add(graphdb[i]);
			//System.out.println(calcScore(graphdb[ids[i]],index));

		}
		

	}
	
	private static double calcScore(Graph g,int index) {
		// TODO Auto-generated method stub
		int[] profile=new int[sigHists[index].hist.length];
		double score=0;
		int[] vertices=new int[g.V.length];
		Arrays.fill(vertices,1);
		int count=0;
		for(int i=0;i<g.E.length;i++)
		{
			Edge selected=g.E[i];
			int vid1=selected.node1;
			int vid2=selected.node2;
			Integer edgePos=(Integer)topEdges.get(selected.type);
			if(edgePos!=null)
			{
				profile[edgePos+vertexDictionary.labels.size()]++;
				//vertices[vid1]=true;
				//vertices[vid2]=true;
				//count++;
			}
			else
			{
				vertices[vid1]=0;
				vertices[vid2]=0;				
			}
		}
		for(int i=0;i<g.V.length;i++)
		{
			//if(vertices[i]==0)
			{
				profile[g.V[i].label]++;
				//count++;
			}
		}
		//score=1;
	
		for(int i=0;i<profile.length;i++)
		{
			if(sigHists[index].hist[i]!=0 && profile[i]==0)
			{
				//score+=(sigHists[index].hist[i]-profile[i]/count*10);
				count+=Math.abs(sigHists[index].hist[i]-profile[i]);//return 0;
				if(profile[i]==0 && sigHists[index].hist[i]!=0)
					return 0;
			}
		}
		return 1;
		/*
		System.out.println("COUNT: "+count);
		if(count>0)
			return 0;
		return 1;*/
	}
	 static void checkDir(String fileName) {
		// TODO Auto-generated method stub
		File f=new File(fileName+"/neato/");
		if(f.exists())
		{
			FileUtils.deleteFolder(fileName);	
		}

		f.mkdirs();
		new File(fileName+"/gif/").mkdir();
	}

	public static String drawGraph(Graph g)
	{
		StringBuffer s=new StringBuffer("");
		Edge[] e=(Edge[]) g.E;
		Vertex[] v=(Vertex[])g.V;
		for(int j=0;j<e.length;j++)
		{
			int node1=e[j].node1;
			int node2=e[j].node2;
			int bond=e[j].bond;
			//System.out.println(node1+" "+node2);
			String nodeLabel1=vertexDictionary.labels.get(v[node1].label)+"_"+node1;
			String nodeLabel2=vertexDictionary.labels.get(v[node2].label)+"_"+node2;
			s.append(nodeLabel1+" -- "+nodeLabel2+"[label=\""+bond+"\"];\n");
		}
		return head+s.toString()+"}";
	}
	
}
