package grank.transform;

import java.io.*;
import java.util.*;

import openBabel.MoleculeLoader;
import Dictionary.argumentsDictionary;
import Dictionary.vertexDictionary;
import joelib2.io.MoleculeIOException;
import ctree.util.*;
import grank.mine.MotifHistSet;
import grank.pvalue.*;
import graph.Edge;
import graph.Graph;
import graph.Vertex;

import tool.BuildGraph;
import tool.FileUtils;
import tool.NeighborHood;
import tool.RandomWalk;
import tool.RandomWalkBySet;
import tool.RandomWalkV1;
import tool.RebuildGraph;
/**
 * Feature vector of a graph, i.e., a histogram w.r.t. a feature set.
 * @author Huahai He
 * @version 1.0
 */
public class Hist {
  public String id;
  public int[] hist;
  public int label;
  public Hist(String _id, int[] _hist) {
    id = _id;
    //System.out.println(id);
    hist = _hist;
    label=-1;
  }
  public Hist(String _id, int[] _hist,int _label) {
	    id = _id;
	    //System.out.println(id);
	    hist = _hist;
	    label=_label;
	  }
  /*
     public String toString() {
    StringBuffer sb = new StringBuffer(id + '\n');
    for (int i = 0; i < hist.length; i++) {
      sb.append(hist[i]);
      if (i < hist.length - 1) {
        sb.append(' ');
      }
    }
    return sb.toString();
     }*/

  public int size() {
    int size = 0;
    for (int i = 0; i < hist.length; i++) {
      size += hist[i];
    }
    return size;
  }

  public static void saveHists(Hist[] H, int maxCnt, String hist_file) throws
      IOException {
    PrintWriter out = new PrintWriter(argumentsDictionary.ids.substring(0,argumentsDictionary.ids.lastIndexOf("."))+"/"+hist_file);
    int cnt = Math.min(H.length, maxCnt);
    //int m = H[0].hist.length;
    for (int i = 0; i < cnt; i++) {
      if(H[i]==null)
	      continue;
      out.print(H[i].id+":");
      //System.out.println(H[i].id);
      for (int j = 0; j < H[i].hist.length - 1; j++) {
        out.print(H[i].hist[j] + " ");
      }
      out.println(H[i].hist[H[i].hist.length - 1]);
    }
    out.close();
  }

  public static void saveHists(Hist[] H, String hist_file) throws IOException {
    saveHists(H, H.length, hist_file);
  }

  /**
   * Test if this feature vector contains another feature vector,
   * i.e., for all i, hist[i]>=X.hist[i]
   */
  public boolean contains(Hist X) {
    assert (hist.length == X.hist.length);
    for (int i = 0; i < hist.length; i++) {
      if (hist[i] < X.hist[i]) {
        return false;
      }
    }
    return true;
  }

  public static Hist[] loadHists(String hist_file) throws IOException {

    Scanner in = new Scanner(new FileReader(hist_file));
    Vector<Hist> buf = new Vector<Hist> ();
    //String id;
    while (in.hasNext()) {
      String line = in.nextLine();
      StringTokenizer st=new StringTokenizer(line,": ");
      //String[] list = line.split(" ");
      int[] hist = new int[st.countTokens()];
      String id=st.nextToken();
      for (int i = 1; i < hist.length; i++) {
        hist[i-1] = Short.parseShort(st.nextToken());
      }
      buf.add(new Hist(id, hist));
    }
    in.close();

    Hist[] array = new Hist[buf.size()];
    buf.toArray(array);
    return array;

  }

  /**
   * Return the maximum size of histograms
   * @param H Hist[]
   * @return int
   */
  public static int maxSize(Hist[] H) {
    int maxZ = 0;
    for (Hist h : H) {
      int Z = PValue.sum(h.hist);
      if (Z > maxZ) {
        maxZ = Z;
      }
    }
    return maxZ;
  }

  /**
   * Output information about a hist file
   * @param args String[]
   */
  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 1) {
      System.err.println("Usage: ... hist_file");
      System.exit(0);
    }

    String hist_file = opt.getArg(0);

    Hist[] hists = loadHists(hist_file);
    int m = hists[0].hist.length;
    int[] xmax = new int[m];
    Arrays.fill(xmax, 0);
    int maxSize = 0;
    int minSize = Integer.MAX_VALUE;
    int[] D = new int[hists.length];
    for (int j = 0; j < hists.length; j++) {
      Hist h = hists[j];
      for (int i = 0; i < m; i++) {
        if (h.hist[i] > xmax[i]) {
          xmax[i] = h.hist[i];
        }
      }
      int size = PValue.sum(h.hist);
      if (size > maxSize) {
        maxSize = size;
      }
      if (size < minSize) {
        minSize = size;
      }
      D[j] = size;
    }

    System.out.printf("m = %d \n", m);
    System.out.printf("xmax = \n");
    for (int i = 0; i < m; i++) {
      System.out.printf("%3d ", xmax[i]);
    }
    System.out.println();
    System.out.printf("maxSize = %d, minSize= %d\n", maxSize, minSize);
    System.out.printf("Number of histograms: %d\n", hists.length);

    int[] dbsizes = new int[hists.length];
    for (int i = 0; i < hists.length; i++) {
      dbsizes[i] = PValue.sum(hists[i].hist);
    }
    int[][] tmp = PValue.dbSizes(dbsizes);
    int[] dbZ = tmp[0];
    int[] dbN = tmp[1];
    System.out.printf("Number of distinct histgoram sizes: %d\n", dbZ.length);
    /*
         int[][] tmp = PValue.dbSizes(D);
         int[] dbZ = tmp[0];
         int[] dbN = tmp[1];
         System.out.println("dbZ[], dbN[] = ");
         for (int i = 0; i < dbZ.length; i++) {
      System.out.printf("%d: %d, %d\n", i, dbZ[i], dbN[i]);
         }
     */
  }

public static Hist[] loadHistsFromRandomWalk(String fileName) throws FileNotFoundException {
	// TODO Auto-generated method stub
	
	Graph[] graphdb=BuildGraph.loadGraphs(fileName);
	vertexDictionary.print();
	Vector<Hist> buf = new Vector<Hist> ();
	double time=System.currentTimeMillis();
	RandomWalk r=new RandomWalkV1(argumentsDictionary.restart);
	for(int i=0;i<graphdb.length;i++)
	{
		//System.out.println(i);
		int[][] profile=r.walk(graphdb[i]);
		for (int j=0;j<profile.length;j++)
		{
			buf.add(new Hist(graphdb[i].id+"-"+i+"-"+j, profile[j]));
			//System.out.println(graphdb[i].id+"-"+j);
		}

	}
	System.out.println("Time taken: "+(System.currentTimeMillis()-time)/1000.0);
    Hist[] array = new Hist[buf.size()];
    //System.out.println("Total number of nodes: "+buf.size());
    //new Scanner(System.in).next();
    buf.toArray(array);
    return array;
 }

public static Vector[] loadHistsFromRandomWalkAsSet(String fileName,boolean relabelAtoms) throws IOException, MoleculeIOException {
	// TODO Auto-generated method stub

	BuildGraph.loadGraphs(fileName);
	if(relabelAtoms)
	{
		MoleculeLoader.load(BuildGraph.graphdb);	
		for(Graph g:BuildGraph.graphdb)
			g.labelAtomsWithJOELibTyper();
	}
	//vertexDictionary.print();
	Vector<Hist> buf = new Vector<Hist> ();
	double time=System.currentTimeMillis();
	RandomWalkBySet r=new RandomWalkBySet(argumentsDictionary.restart);
	return r.getHistsBySet(BuildGraph.graphdb);

 }

public static Vector[] loadHistsFromRandomWalkAsSet(String fileName, String fg, boolean reLabelAtoms) throws IOException, MoleculeIOException {
	// TODO Auto-generated method stub
	System.out.println("Functional Group File:"+fg);
	RebuildGraph rg=new RebuildGraph(fileName,fg,reLabelAtoms);
	long time=System.currentTimeMillis();
	rg.sortFG(1);
	Graph[] graphdb=rg.replaceFG();
	System.out.println("Time taken to replace: "+(System.currentTimeMillis()-time));
	BuildGraph.graphdb=graphdb;
	vertexDictionary.print();
	Vector<Hist> buf = new Vector<Hist> ();
	time=System.currentTimeMillis();
	RandomWalkBySet r=new RandomWalkBySet(argumentsDictionary.restart);
	return r.getHistsBySet(graphdb);

 }
/*
public static Vector[] loadHistsFromRandomWalkAsSet(String fileName, String fg) throws FileNotFoundException {
	// TODO Auto-generated method stub
	System.out.println("Functional Group File:"+fg);
	RebuildGraph rg=new RebuildGraph(fileName,fg);
	long time=System.currentTimeMillis();
	rg.sortFG(1);
	Graph[] graphdb=rg.replaceFG();
	System.out.println("Time taken to replace: "+(System.currentTimeMillis()-time));
	BuildGraph.graphdb=graphdb;
	//vertexDictionary.print();
	Vector<Hist> buf = new Vector<Hist> ();
	time=System.currentTimeMillis();
	RandomWalkBySet r=new RandomWalkBySet(argumentsDictionary.restart);
	return r.getHistsBySet(graphdb);

 }
*/
/*
 * stores the histograms which contain a particular sub-histogram
 */
public static void saveContainers(Hist[] results, Hist[] db) throws FileNotFoundException {
	// TODO Auto-generated method stub
	PrintWriter out=null;
	//Vector[] motifs=new Vector[results.length];
	String path=argumentsDictionary.subHistDir;
	FileUtils.deleteFiles(path);
	StringBuffer cgScript=new StringBuffer("rm -rf fp\n");
	for(int i=0;i<results.length;i++)
	{
		Vector<Graph[]> motif=new Vector<Graph[]>();
		StringBuffer s=new StringBuffer("");
		if(results[i]==null)
			continue;
		int gid=0;
		int lastgid=-1;
		Vector<Integer> nodes=new Vector<Integer>();
		int numGraphs=0;
		for(int j=0;j<db.length;j++)
		{
			if(db[j].label==results[i].label && db[j].contains(results[i]))
			{
				s.append(db[j].id+"\n");
				//System.out.println(db[j].id);
				String[] parts=db[j].id.split("-");
				gid=new Integer(parts[1]);
				int node=new Integer(parts[2]);
				//System.out.println(gid+"-"+node);
				
				if((gid!=lastgid && lastgid!=-1))
				{
					motif.add(NeighborHood.getNeighborHoods(argumentsDictionary.radius, nodes.toArray(new Integer[1]), lastgid));
					lastgid=gid;
					nodes=new Vector<Integer>();
				}
				else
					lastgid=gid;
				
				nodes.add(node);
			}
		}
		motif.add(NeighborHood.getNeighborHoods(argumentsDictionary.radius, nodes.toArray(new Integer[1]), lastgid));
		StringBuffer graphs=new StringBuffer("");
		int size=0;
		//if(motif.size()<=1)
			//continue;
		
		for(int j=0;j<motif.size();j++)
		{
			Graph[] subgraphs=motif.elementAt(j);
			
			for(int k=0;k<subgraphs.length;k++)
			{
				size++;
				Graph g=subgraphs[k];
				if(g!=null)
				{
					graphs.append(g.toString("CG")+"\n");
					numGraphs++;
				}
			}
		}
		if(numGraphs<=3)
			continue;
		PrintWriter gout=new PrintWriter(path+results[i].id+".cg");
		out=new PrintWriter(path+results[i].id+".txt");
		//cgScript.append("../gSpan "+results[i].id+ ".cg -s"+numGraphs+"-o \n");
		cgScript.append("../fsg "+results[i].id+ ".cg -s80 -m 3 -x\n");
		gout.print(graphs.toString());
		gout.close();
		out.println(s);
		out.close();
	}
	/*
	String[] names=FileUtils.getFilesWithExt("cg",path);
	
	for(int i=0;i<names.length;i++)
	{
		String name=names[i];
		//int freq=new Integer(name.substring(name.lastIndexOf("-")+1,name.lastIndexOf(".")));
		//int support=new Double(freq*0.8).intValue();
		cgScript.append("../fsg "+name+ " -s"+numGraphs+" -m 5 -x\n");
	}
	*/
	FileUtils.writeToFile(path+"findFreqGraphs.sh",cgScript.toString()+"mkdir fp\nmv *.fp fp\n");
	/*int size=0;
	for(int i=0;i<motifs.length;i++)
	{
		size+=motifs[i].size();
	}
	System.out.println(size);*/
}

public static void saveContainers(Hist[][] results, Vector[] db, String sig_hist) throws IOException {
	// TODO Auto-generated method stub
	Vector<Hist> bufResults=new Vector();
	Vector<Hist> bufDB=new Vector();
	
	for(int i=0;i<results.length;i++)
	{
		if(results[i]==null)
			continue;
		for(int j=0;j<results[i].length;j++)
		{
			results[i][j].label=i;
			bufResults.add(results[i][j]);
		}
		Hist[] d=(Hist[])db[i].toArray(new Hist[1]);
		for(int j=0;j<d.length;j++)
			bufDB.add(d[j]);
		
	}
	Hist[] resultHists=MotifHistSet.removeCloseHists(bufResults);
	System.out.println("Total Hist Length: "+resultHists.length);
	saveContainers(resultHists, (Hist[])bufDB.toArray(new Hist[1]));
	saveHists(resultHists, sig_hist);
} 

}
