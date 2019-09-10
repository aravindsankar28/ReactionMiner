package generic;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import joelib2.io.MoleculeIOException;
import tool.BuildGraph;
import tool.FileUtils;
import tool.RandomWalkBySet;
import tool.TanimotoMatrix;
import grank.transform.*;
import grank.pvalue.*;
import ctree.util.*;
import grank.simpvalue.*;
import grank.mine.*;

import Dictionary.*;
/**
 * GraphRank: an algorithm for mining significant subgraphs
 *
 * Assuming p_1>=p_2>=...>=p_m
 *
 * GraphRank4: lexicographical, depth-first, bottom up, flexible size,
 *             Mining Closed Only histograms
 *
 * @author Sayan Ranu
 * @version 1.0
 */
public class Descriptor {

  private static long cntEval = 0; // count of eval (lower bound pvalue)
  private static long cntAccurate = 0; // count of accurate pvalue computations
  private static long cntUpdate = 0; // count of updating top-K
  private static int numFreqBin = 0; // number of frequent bins
  //private static long cntFuture = 0; // count of pruning by future
  static Vector[] background=null;
  //static boolean[] origClass;
  static Hist[][][] results;
  static HashMap[] classMap;
  static String[][] classList;
  
  /**
   * GraphRank - the top level of the algorithm
   * x= root
   * b=0
   * z=0
   * S=base0
   * depth=0
   */
  private static void closedHist(int[] x, int b, int z,
                                 Vector < int[] > S,
                                 int depth, Environment env) throws
      MathException {

    /*if (!future(hist, pos0, base, env)) {
      return;
         }*/
	  //System.out.println("entering closedhist"+depth+"size= "+S.size());
	  //System.out.println(Arrays.toString(x));
	  //for(int i=0;i<x.length;i++)

    if (z > 0) {
      if (env.toEval ){//&& PValue.sum(x)>4) {
        eval(x, z, S.size(), env);
      }
      else { // Copy all closed sub-histograms to the answer set
        int[] h1 = new int[env.m];
        System.arraycopy(x, 0, h1, 0, env.m);
        Answer a = new Answer(h1, S.size(), 0);
        env.ans.add(a);

      }
      cntEval++;
    }

    Vector<int[]> S1 = new Vector<int[]> (S.size());

    int[] x1 = new int[env.m];
    for (int pos = b; pos < env.m; pos++) {
    	//System.out.println(pos);
    	//new Scanner(System.in).next();
      //for (int pos = m-1;pos>=pos0;pos--) {
      if (env.fbin[pos] == false) {
        continue;
      }
      int ground = x[pos]; // minimum value at pos
      Arrays.fill(x1, Integer.MAX_VALUE); // next minimum value at pos
      S1.clear();
      for (int i = 0; i < S.size(); i++) {
        int[] H = S.elementAt(i); // a supporting database histogram
        if (H[pos] > ground) {
          S1.add(H);
          for (int j = 0; j < env.m; j++) {
            if ( H[j] < x1[j]) {
              x1[j] = H[j];
            }
          }
        }
      }
      if (S1.size() < env.minSup) { // constraint of support
        continue;
      }

      // check if it violates lexicographical order
      boolean dup_flag = false;
      for (int j = 0; j < pos; j++) {
        if (x1[j] > x[j]) {
          dup_flag = true;
          break;
        }
      }
      if (dup_flag) {
        continue;
      }
      int z1 = PValue.sum(x1);
      //System.out.println("Comparing PValue: z1= "+z1+"env.hz= "+env.hZ);
      
      if (z1 > env.hZ) {
      //if(PValue.computePvalue(dbP, dbN, nG, sup))
        continue;
      }
      if (!futureSimModel(x1, pos, S1, env)) {
        //cntFuture++;
        continue;
      }

      closedHist(x1, pos, z1, S1, depth + 1, env);
    }
  }

  private static boolean futureSimModel(int[] x, int b,
                                        Vector < int[] > S, Environment env) throws
      MathException {
    int sup = S.size();
    int[] ceiling = new int[env.m];
    System.arraycopy(x, 0, ceiling, 0, env.m);
    for (int i = 0; i < S.size(); i++) {
      int[] h = S.elementAt(i);
      for (int j = 0; j < env.m; j++) {
        if (env.fbin[j] && h[j] > ceiling[j]) {
          ceiling[j] = h[j];
        }
      }
    }

    double pvalueLB = SimPValue.pvalue(env.simP, ceiling, env.nG, sup).pvalue; // lower bound
    if (pvalueLB > env.maxPvalue) {
      return false;
    }
    else {
      return true;
    }
  }

  // Return false if future can be pruned
  private static boolean future(int[] x, int b, Vector < int[] > S,
                                Environment env) throws MathException {
    int[] ceiling = new int[env.m];
    System.arraycopy(x, 0, ceiling, 0, env.m);
    for (int i = 0; i < S.size(); i++) {
      int[] h = S.elementAt(i);
      for (int j = b; j < env.m; j++) {
        if (env.fbin[j] && h[j] > ceiling[j]) {
          ceiling[j] = h[j];
        }
      }
    }
    int z = PValue.sum(ceiling);
    if (z > env.maxZ) {
      return true;
    }
    double[] probs = new double[env.maxZ + 1];
    Arrays.fill(probs, 0, z, 0);
    for (int s = z; s <= env.maxZ; s++) {
      probs[s] = PValue.lowerProb(env.p, ceiling, s);
    }
    double pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG,
                                         S.size());
    if (pvalue < env.maxPvalue) {
      return true;
    }
    else {
      return false;
    }

  }

  public static void eval(int[] x, int z, int sup,
                          Environment env) throws MathException {
    assert (z <= env.hZ);
    if (env.simP != null) {
      evalSimModel(x, z, sup, env);
      return;
    }
    // compute pvalue

    double pvalue = 0;

    // fast lower bound of pvalue
    if (env.preEval) {
      double[] dbP = new double[env.dbZ.length];
      for (int i = 0; i < env.dbZ.length; i++) {
        if (env.dbZ[i] >= z) {
          dbP[i] = PValue.lowerProb(env.p, x, env.dbZ[i]);
        }
        else {
          dbP[i] = 0;
        }
      }
      pvalue = PValue.computePvalue(dbP, env.dbN, env.nG, sup);
    }
    if (pvalue <= env.maxPvalue) {
      // accurate pvalue
      double[] probs = new double[env.maxZ + 1];
      probs = PValue.probSubsetRecursiveArray(env.p, x, z, env.maxZ);
      pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, sup);
      cntAccurate++;

      // add this node to answers
      if (pvalue <= env.maxPvalue) {
        int[] h1 = new int[env.m];
        System.arraycopy(x, 0, h1, 0, env.m);
        Answer a = new Answer(h1, sup, pvalue);
        env.ans.add(a);
        if (env.ans.size() > env.K) {
          env.ans.poll();
          env.maxPvalue = env.ans.peek().pvalue;
          if (env.verbose) {
            System.err.printf("pvalue=%g, sup=%d, z=%d\n",
                              env.maxPvalue, sup, z);
          }
        }
        cntUpdate++;
      }
    }
  }

  public static void evalSimModel(int[] x, int z, int sup, Environment env) throws
      MathException {
    double pvalue = SimPValue.pvalue(env.simP, x, env.nG, sup).pvalue;
    if (pvalue <= env.maxPvalue) {
      int[] x1 = new int[env.m];
      System.arraycopy(x, 0, x1, 0, env.m);
      Answer a = new Answer(x1, sup, pvalue);
      env.ans.add(a);
      if (env.ans.size() > env.K) {
        env.ans.poll();
        env.maxPvalue = env.ans.peek().pvalue;
        if (env.verbose) {
          System.err.printf("pvalue=%g, sup=%d, z=%d\n",
                            env.maxPvalue, sup, z);
        }
      }
      cntUpdate++;

    }
  }

  public static Hist[] mainProcess(double maxPvalue, int K, int minSup,
                                   int[][] H, double[] p, double[][] sim_p,
                                   int hZ, boolean toEval, boolean preEval,
                                   boolean verbose) throws
      MathException {

    int m = H[0].length;
    int nG = H.length;

    // frequency of each bin
    int fcnt = 0;
    boolean[] fbin = new boolean[m]; // true if fbin[i] is frequent
    for (int i = 0; i < m; i++) {
      int cnt = 0;
      for (int j = 0; j < nG; j++) {
        if (H[j][i] > 0) {
          cnt++;
        }
      }
      if (cnt >= minSup) {
        fcnt++;
        fbin[i] = true;
        //System.err.printf("%d ", cnt);
      }
      else {
        fbin[i] = false;
      }
    }
    numFreqBin = fcnt;
    //System.err.printf("\nFrequent bins: %d\n", fcnt);

    // Get dbZ, dbN, maxZ
    int[] dbsizes = new int[nG];
    for (int i = 0; i < nG; i++) {
      dbsizes[i] = PValue.sum(H[i]);
    }
    int[][] tmp = PValue.dbSizes(dbsizes);
    int[] dbZ = tmp[0];
    int[] dbN = tmp[1];
    int maxZ = 0;
    for (int Z : dbZ) {
      if (Z > maxZ) {
        maxZ = Z;
      }
    }

    // prepare parameters
    int[] root = new int[m];
    Arrays.fill(root, 0);
    Vector<int[]> base0 = new Vector<int[]> ();
    for (int[] h : H) {
      base0.add(h);
    }

    // answer set stored in a priority queue, reverse order, at most K answers
    PriorityQueue<Answer>
        ans = new PriorityQueue<Answer> (Math.min(K, 1000));

    Environment env = new Environment(m, p, sim_p, maxZ, H, nG, dbZ, dbN,
                                      maxPvalue,
                                      minSup, K, hZ, ans, fbin, toEval, preEval,
                                      verbose);
    closedHist(root, 0, 0, base0, 0, env);
    //System.out.println("am i coming here11111");
    // output
    /*
    Hist[] results = new Hist[ans.size()];
    for (int i = 0; i < results.length; i++) {
      Answer a = ans.poll();
      int size = PValue.sum(a.hist);
      if(size>=4)
      {
	      int i1 = results.length - i - 1;
	      String id = size + "-" + i1 + "," + a.sup + "," + a.pvalue;
	      Hist h = new Hist(id, a.hist);
	      results[i1] = h;
      }
    }
    return results;
    */

    return filteredResults(ans);
  }

  private static Hist[] filteredResults(PriorityQueue<Answer> ans) {
	// TODO Auto-generated method stub
	    Vector<Hist> buf = new Vector<Hist> ();
	    int anssize=ans.size();
	    for (int i = 0; i < anssize; i++) {
	      Answer a = ans.poll();
	      int size = PValue.sum(a.hist);
	      if(size>=4)
	      {
	    	  int count=0;
	    	  int max=0;
	    	  for(int j=0;j<a.hist.length;j++)
	    	  {
	    		  if(a.hist[j]>=3)
	    			  count++;
	    		  if(a.hist[j]>max)
	    			  max=a.hist[j];
	    	  }
	    	  if(count>=0 || max>=2)
	    	  {
			      int i1 = anssize - i - 1;
			      String id = size + "-" + i1 + "," + a.sup + "," + a.pvalue;
			      Hist h = new Hist(id, a.hist);
			      buf.add(h);
	    	  }
	      }
	    }

	    Hist[] results=new Hist[buf.size()];
	    buf.toArray(results);   
	    //System.out.println("am i coming here22222");
	    return results;
}
public static Hist[] removeCloseHists(Vector<Hist> buf)
{
    boolean skip;
    
    for(int i=0;i<buf.size();i++)
    {
    	skip=false;
    	Hist h1=buf.elementAt(i);
    	for(int j=i+1;j<buf.size()&& !skip;j++)
    	{
    		if(i!=j)
    		{
    			Hist h2=buf.elementAt(j);
    			boolean sub=true;
    			boolean sup=true;
    			boolean close=true;
    			for(int k=0;k<h1.hist.length && (sub || sup);k++)
    			{
    				if(h1.hist[k]>h2.hist[k])
    					sub=false;
    				if(h1.hist[k]<h2.hist[k])
    					sup=false;
    				if((h1.hist[k]!=0 && h2.hist[k]==0) || (h1.hist[k]==0 && h2.hist[k]!=0) || Math.abs(h1.hist[k]-h2.hist[k])>1)
    					close=false;
    			}
    			/*sup=false;
    			sub=false;
			close=false;*/
				if(sup || sub || close)
				{
					/*System.out.println(Arrays.toString(h1.hist));
					System.out.println(Arrays.toString(h2.hist));
					System.out.println("------------------------");*/
					String[] parts1=h1.id.split(",");
					String[] parts2=h2.id.split(",");
					if(new Double(parts1[2])<=new Double(parts2[2]))
					{
						buf.removeElementAt(j);
						j--;
						
					}
					else
    					//if(new Integer(parts1[2])>new Integer(parts2[2]))
    					{
    						buf.removeElementAt(i);
    						i--;
    						skip=true;
    					}    	
    					
				}
    		}
    	}
    }
    //Hist[] results=new Hist[buf.size()];
      
    //System.out.println("am i coming here22222");
    return buf.toArray(new Hist[1]);
}
public static void main(String[] args) throws IOException, MathException, MoleculeIOException {
    Opt opt = new Opt(args);
    int numTopAtoms=opt.getInt("nta");
    long time0 = System.currentTimeMillis();
    int K = 1000;//opt.getInt("K", Integer.MAX_VALUE); EDITED BY SAYAN
    int hZ = 10;//opt.getInt("hZ", Integer.MAX_VALUE); EDITED BY SAYAN
    int hz = 4;//opt.getInt("hz", 1); EDITED BY SAYAN
    double restart=opt.getDouble("r");
    double delta=opt.getDouble("d");
    boolean mu0Flag = true; // if true, then use graphMu0, o/w use histMu0
    if (opt.hasOpt("mu0") && opt.getString("mu0").equals("hist")) {
      mu0Flag = false;
    }
    Vector[] DB;
    
    
    results=new Hist[opt.args()-1][][];
    classList=new String[opt.args()-1][];
    classMap=new HashMap[opt.args()-1];
    for(int i=1;i<opt.args();i++)
	    fetchGraphIDS(opt.getArg(i),i-1);

    argumentsDictionary.set(opt.getArg(0),opt.getArg(0),restart,delta,numTopAtoms);
    
   
    if(opt.hasOpt("calcPatterns"))
    {
        if(!opt.getString("FG").equals("none"))
        	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.getString("FG"),opt.hasOpt("relabelAtoms"));
        else
        	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.hasOpt("relabelAtoms"));

	    String tmp = opt.getString("minSup", "1");
	    double ratio = Double.parseDouble(tmp)/100;
	    double maxPvalue = opt.getDouble("pvalue", 0.1);// EDITED BY SAYAN
	    int totalSize=0;
	    System.out.println("Ratio: "+ratio);
	    System.out.println("Pvalue: "+maxPvalue);
		    mu0Flag=false;//EDITED BY SAYAN
		    int totPos=0;
		    int totNeg=0;
		    for(int classify=0;classify<opt.args()-1;classify++)
		    {
			    double[][] simProb=null;  
			    argumentsDictionary.set(opt.getArg(0),opt.getArg(1+classify),restart,delta,numTopAtoms);
			    
			    DB = getSubset(background,classify);
			    Vector[] opposite=getOppositeSet(background, classify);
			    results[classify]=new Hist[DB.length][];
			    //for(int dbiter=0;dbiter<DB.length;dbiter++)
			   // {
				
			   // }
			    for(int dbiter=0;dbiter<DB.length;dbiter++)
			    {
			    	if(DB[dbiter]==null || DB[dbiter].size()==0)
			    		continue;
			    	Hist[] dbhist=(Hist[])DB[dbiter].toArray(new Hist[1]);
		    	    simProb = SimBasisProb.genBasisProb((Hist[])opposite[dbiter].toArray(new Hist[0]),11);
			        
		 
			    	if(DB[dbiter]==null || DB[dbiter].size()==0)
			    		continue;
			    	//Hist[] dbhist=(Hist[])DB[dbiter].toArray(new Hist[1]);
				    int[][] H = new int[dbhist.length][dbhist[0].hist.length];
				    for (int i = 0; i < H.length; i++) {
				      H[i] = dbhist[i].hist;
				    }
				
				    // minimum support, either integer or percentage
				    
				    
				    int minSup=(int)(ratio*dbhist.length);//edited by Sayan
				    int maxZ = Hist.maxSize(dbhist); // maximum histogram size
				    if (hZ > maxZ) {
				      hZ = maxZ;
				    }
				
				    double[] prob = null;//model.equals("complex") ? BasisProb.loadProb(opt.getArg(1)) : null; // feature probabilities	
				    boolean toEval = opt.getString("eval", "yes").equalsIgnoreCase("yes");
				    boolean preEval = opt.getString("preEval", "yes").equalsIgnoreCase("yes");
				    boolean verbose = opt.hasOpt("verbose");
				
				    
				    //System.err.println("Start mining ...");
				    results[classify][dbiter] = mainProcess(maxPvalue, K, minSup, H, prob, simProb, hZ,toEval, preEval, verbose);
				    if(classify==0)
				    	totPos+=results[classify][dbiter].length;
				    else
				    	totNeg+=results[classify][dbiter].length;
			    }
		    }
    	String features=RandomWalkBySet.getFeatures();
    	FileUtils.writeToFile(opt.getArg(0)+".patterns", features+"\n"+storeFeatures());
    }
    else if (opt.hasOpt("convertToVector"))
    {
    	argumentsDictionary.loadTopAtoms=opt.getString("convertToVector");
        if(!opt.getString("FG").equals("none"))
        	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.getString("FG"),opt.hasOpt("relabelAtoms"));
        else
        	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.hasOpt("relabelAtoms"));

    	FileUtils.writeToFile(opt.getArg(0)+".vectors", convertToVector(argumentsDictionary.loadTopAtoms));
    }
    	
    System.err.println("Total time: "+(System.currentTimeMillis()-time0));
    //new Scanner(System.in).next();
	    
  }


private static Vector[] getOppositeSet(Vector[] background, int val) {
	// TODO Auto-generated method stub
	HashMap m;
	String[] list;
	list=classList[val];

	m=new HashMap();
	for(int i=0;i<list.length ;i++)
	{
		m.put(list[i].trim(), 1);
	}

	Vector[] DB=new Vector[background.length];
	for(int i=0;i<background.length;i++)
	{
		if(background[i].size()==0)
			continue;
		Hist[] labelSet=(Hist[])background[i].toArray(new Hist[1]);
		DB[i]=new Vector<Hist>();
		for(int j=0;j<labelSet.length;j++)
		{
			if(!m.containsKey(labelSet[j].id.trim().split("-")[0]))
			{
					//System.out.println(i);
					DB[i].add(labelSet[j]);
			}
		}
	}
	return DB;

}


private static StringBuffer stringRep(double[] fv) {
	// TODO Auto-generated method stub
	StringBuffer rep=new StringBuffer("");
	for(int i=0;i<fv.length;i++)
	{
		//if(fv[i]!=0)
			rep.append(i+":"+fv[i]+" ");
	}
	rep.append("\n");
	return rep;
}



private static String convertToVector(String patterns)
{
	System.out.println("Converting to Vectors");

	String[] lines=FileUtils.getFileContents(patterns).trim().split("\n");
	HashMap<String,Integer> features=getFeatures(lines[1]);
	Hist[] sigHists=new Hist[lines.length-2];
	int dimension=lines[2].trim().split(" ").length-1;
	int dim=sigHists.length;
	for (int i=2;i<lines.length;i++)
	{
		String line=lines[i];
		int j=0;
		sigHists[i-2]=new Hist("",new int[0]);
		for (String val:line.trim().split(" "))
		{
			if (j==0)
			{
				sigHists[i-2].id=val;
				sigHists[i-2].hist=new int[dimension];
			}
			else
				sigHists[i-2].hist[j-1]=new Integer(val);
			j++;
		}
	}
    Hist[][] labelSet=new Hist[background.length][];
	HashMap<String, Integer> currentMap=getFeatures(RandomWalkBySet.getFeatures().trim().split("\n")[1]);

	for(int b=0;b<background.length;b++)
	{
		if(background[b].size()==0)
			labelSet[b]=null;
		else
		{
			labelSet[b]=(Hist[])background[b].toArray(new Hist[1]);
			for(int l=0;l<labelSet[b].length;l++)
			{
				labelSet[b][l].hist=rearrangeDimensions(labelSet[b][l].hist,features,currentMap);
			}
		}
	}
	long time=System.currentTimeMillis();
    HashMap<String,double[]> scores=new HashMap();
    double[] def =new double[dim];
    //Arrays.fill(def, 0.0);
    int index=0;
    for(int i=0;i<sigHists.length;i++)
    {
    		for(int b=0;b<labelSet.length;b++)
    		{
    			if(labelSet[b]==null)
    				continue;
    			
    			for(int l=0;l<labelSet[b].length;l++)
    			{
    				String id=labelSet[b][l].id.trim().split("-")[0];
    				
    				double dist=calcDistance(labelSet[b][l],sigHists[i]);
    				double[] currScore=(double[]) scores.get(id);
    				if(currScore!=null)
    				{
    					currScore[index]=Math.max(currScore[index], dist);
    					scores.put(id, currScore);
    				}
    				else
    				{
    					currScore=def.clone();
    					currScore[index]=dist;
    					scores.put(id, currScore);
    				}
    			}
    		}
    		index++;
    	
    }
    
	System.out.println("Constructing string rep");

    StringBuffer file=new StringBuffer("");
    for(int i=0;i<classList.length;i++)
    {
    	for(int j=0;j<classList[i].length;j++)
    	{
    		//System.out.println(classList[i][j].trim());
    		if(scores.get(classList[i][j].trim())==null)
    			continue;
    		//System.out.println("Coming here");

    		file.append(i+" ");
    		file.append(stringRep((double[])scores.get(classList[i][j].trim())));
    	}
    }
    return file.toString();
	
}

private static int[] rearrangeDimensions(int[] hist, HashMap<String, Integer> features,HashMap<String, Integer> currentMap) {
	// TODO Auto-generated method stub
	int[] newHist=new int[features.size()];
	
	for (String key:currentMap.keySet())
	{
		int currVal=currentMap.get(key);
		Integer oldVal=features.get(key);
		if (oldVal!=null)
		{
			//if(currVal!=oldVal)
				//System.out.println(oldVal+" : "+currVal);
			newHist[oldVal]=hist[currVal];
		}
		
	}
	for (String key:features.keySet())
	{
		Integer currVal=currentMap.get(key);
		Integer oldVal=features.get(key);
		if (currVal==null)
		{
			//if(currVal!=oldVal)
				//System.out.println("key : "+key);
			newHist[oldVal]=10;
		}
		
	}
	//System.out.println(Arrays.toString(newHist));
	return newHist;
}

private static HashMap<String, Integer> getFeatures(String line) {
	// TODO Auto-generated method stub
	HashMap<String,Integer> features=new HashMap<String,Integer>();
	for(String keyval:line.trim().split(" "))
	{
		String[] vals=keyval.trim().split(":");
		features.put(vals[0], new Integer(vals[1]));
	}
	return features;
}

private static String storeFeatures() throws FileNotFoundException {
	// TODO Auto-generated method stub
    Hist[][] classResults=new Hist[classList.length][];
    int dim=0;
    StringBuffer s=new StringBuffer("");
    for(int i=0;i<classList.length;i++)
    {
	    classResults[i]=getResults(results[i]);
	    dim+=classResults[i].length;
	    System.out.println("tot "+i+" : "+classResults[i].length);
	    for (int j=0;j<classResults[i].length;j++)
	    {
	    	s.append(classResults[i][j].id+" ");
	    	for(int k=0;k<classResults[i][j].hist.length;k++)
	    		s.append(classResults[i][j].hist[k]+" ");
	    	s.append('\n');
	    }
    }
    return s.toString();
	
}

private static double calcDistance(Hist queryHist, Hist sigHist) {
	// TODO Auto-generated method stub
	double dist=0;
	for(int i=0;i<queryHist.hist.length;i++)
	{
		if(queryHist.hist[i]<sigHist.hist[i])
			return 0;
		//dist+=queryHist.hist[i]-sigHist.hist[i];
	}
	return 1;
}

@SuppressWarnings("unchecked")


private static Integer[][] calcClosest(Hist hist, Hist[][] classResults) {
	// TODO Auto-generated method stub
	
	int[] scores=new int[classResults.length];
	int globalMin=Integer.MAX_VALUE;
	int globalID;
	for(int i=0;i<classResults.length;i++)
	{
		int min=Integer.MAX_VALUE;

		for(int j=0;j<classResults[i].length;j++)
		{
			if(classResults[i][j]==null)
				continue;
			int currSum=getScore(hist.hist,classResults[i][j]);
			if(currSum<min)
			{
				min=currSum;

			}
		}
		if(min<globalMin)
		{
			globalMin=min;
			globalID=i;
		
		}
		scores[i]=min;

	}
	Integer[][] result=new Integer[scores.length][2];
	int c=2;
	for(int i=0;i<classResults.length;i++)
	{
		
			result[i][0]=scores[i];
			result[i][1]=i;
		
	}
	
	return result;
	
}


private static Hist[] getResults(Hist[][] results) {
	// TODO Auto-generated method stub
	Vector<Hist> bufResults=new Vector();

	
	for(int i=0;i<results.length;i++)
	{
		if(results[i]==null)
			continue;
		for(int j=0;j<results[i].length;j++)
		{
			results[i][j].label=i;
			bufResults.add(results[i][j]);
		}
		
	}
	return removeCloseHists(bufResults);
}

private static int getScore(int[] hist, Hist sig) {
	// TODO Auto-generated method stub
	int score=0;
	for(int i=0;i<sig.hist.length;i++)
	{
		if(sig.hist[i]>hist[i])
			return Integer.MAX_VALUE;
		else
			score+=(hist[i]-sig.hist[i]);
	}

	return score;
}

private static Vector[] getSubset(Vector[] background, int val) {
	// TODO Auto-generated method stub
	

	HashMap m;
	String[] list;
	m=classMap[val];
	list=classList[val];

	m=new HashMap();
	for(int i=0;i<list.length;i++)
	{
		m.put(list[i].trim(), 1);
	}

	int c=0;
	Vector[] DB=new Vector[background.length];
	for(int i=0;i<background.length;i++)
	{
		if(background[i].size()==0)
		{
			continue;
		}
		Hist[] labelSet=(Hist[])background[i].toArray(new Hist[1]);
		DB[i]=new Vector<Hist>();
		for(int j=0;j<labelSet.length;j++)
		{
			//System.out.println(labelSet[j].id.trim().split("-")[0]);
			if(m.containsKey(labelSet[j].id.trim().split("-")[0]))
			{
					//System.out.println("coming");
					DB[i].add(labelSet[j]);
					c++;
			}
		}
	}
	System.out.println("DB: "+c);
	//new Scanner(System.in).next();
	return DB;

}

private static void fetchGraphIDS(String classFile, int index) {
	// TODO Auto-generated method stub

	List l=Arrays.asList(FileUtils.getFileContents(classFile).trim().replace("-","_").split("\n"));
	//Collections.shuffle(l);
	classList[index]=(String[])l.toArray(new String[1]);
}

private static void getTrainSize(double percent)
{
	int minLength=Integer.MAX_VALUE;
	//for(String[] l:classList)
		//minLength=Math.min(minLength,l.length);

	for(int i=0;i<classList.length;i++)
	{
		List l=Arrays.asList(classList[i].clone());	
		//l=l.subList(0, (int)(percent*minLength));

		classList[i]=(String[])l.toArray(new String[1]);
	}
	//return (int)(1.0*minLength);
}


}
