package grank.mine;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import joelib2.io.MoleculeIOException;
import tool.BuildGraph;
import tool.FileUtils;
import tool.RandomWalkBySet;
import grank.transform.*;
import grank.pvalue.*;
import ctree.util.*;
import grank.simpvalue.*;

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
public class SVMClassify {

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
  static HashMap<String,Boolean> testset;

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
	    	  if(count>=0 || max>=1)
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
    if (opt.args() < 3) {
      System.err.println(
          "Usage: ... [options] hist_file prob_file");
      System.err.println("  -pvalue=DOUBLE \t Maximum p-value, default=1");
      System.err.println("  -r \t restart probability in random walk");
      System.err.println("  -d \t convergence value in random walk");
      System.err.println("  -nta \t number of top atoms to count edges");
      System.err.println(
          "  -K=NUMBER \t\t Top-K significant subgraphs, default=MAX_INT");
      System.err.println("  -hZ=NUMBER \t\t Maximum sub-histogram size, default=maximum database histogram size");
      System.err.println(
          "  -hz=NUMBER \t\t Minimum sub-histogram size, default=1");
      System.err.println(
          "  -minSup=NUMBER[%] \t Minimum support, either number or percentage, default=1");
      System.err.println("  -mu0=[graph|hist] \t Use graphMu0 or histMu0 as the real support, default=graph");
      System.err.println(
          "  -sig_hist=FILE \t Significant histograms file, default=sig.hist");
      System.err.println(
          "  -eval=[yes|no] \t Whether to evaluate p-values, default=yes");
      System.err.println("  -preEval=[yes|no] \t Whether to compute the lower bound of p-value, default=yes");
      System.err.println("  -model=[complex|simple] \t default=complex");
      System.err.println("  -verbose \t display verbose messages");

      System.exit(0);
    }
    
    String tmp = opt.getString("minSup", "1");
    double ratio = Double.parseDouble(tmp)/100;
    double maxPvalue = opt.getDouble("pvalue", 0.1);// EDITED BY SAYAN
    int totalSize=0;
    System.out.println("Ratio: "+ratio);
    System.out.println("Pvalue: "+maxPvalue);
    int K = 1000;//opt.getInt("K", Integer.MAX_VALUE); EDITED BY SAYAN
    int hZ = 10;//opt.getInt("hZ", Integer.MAX_VALUE); EDITED BY SAYAN
    int hz = 4;//opt.getInt("hz", 1); EDITED BY SAYAN
    double restart=opt.getDouble("r");
    double delta=opt.getDouble("d");
    int numTopAtoms=opt.getInt("nta");
    boolean mu0Flag = true; // if true, then use graphMu0, o/w use histMu0
    if (opt.hasOpt("mu0") && opt.getString("mu0").equals("hist")) {
      mu0Flag = false;
    }
    
    int fold=opt.getInt("fold");
    
    results=new Hist[opt.args()-1][][];
    classList=new String[opt.args()-1][];
    classMap=new HashMap[opt.args()-1];
    for(int i=1;i<opt.args();i++)
	    fetchGraphIDS(opt.getArg(i),i-1);

    int trainSize=new Double(getTrainSize(1.0)).intValue();
    int testSize=trainSize/fold;
    

    argumentsDictionary.set(opt.getArg(0),opt.getArg(0),restart,delta,numTopAtoms);
    long time0 = System.currentTimeMillis();
    if(!opt.getString("FG").equals("none"))
    	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.getString("FG"),opt.hasOpt("relabelAtoms"));
    else
    	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.hasOpt("relabelAtoms"));
    /*
    int[] trainSize=getTrainSize(background);
    int[] testSize=new int[2];
    trainSize[0]=1500;
    trainSize[1]=1500;
    testSize[0]=150/fold;
    testSize[1]=5000/fold;*/
    System.out.println("Random Walk Time: "+(System.currentTimeMillis()-time0));
    
    for(int f=0;f<fold;f++)
    {
    	/*if (f>0)
    	{
    		RandomWalkBySet r=new RandomWalkBySet(argumentsDictionary.restart);
    		background= r.getHistsBySet(BuildGraph.graphdb);
    	}*/
	    //System.out.println("Background Size: "+background.length);
	    mu0Flag=false;//EDITED BY SAYAN
	    int totPos=0;
	    int totNeg=0;
	    Vector[] DB;
	    testset=new HashMap<String,Boolean>();
	    for(int classify=0;classify<opt.args()-1;classify++)
	    {
		    double[][] simProb=null;  
		    argumentsDictionary.set(opt.getArg(0),opt.getArg(1+classify),restart,delta,numTopAtoms);

		    System.err.println("Train Size: "+trainSize);
		    System.err.println("Test Size: "+testSize);

		   // System.err.println("Train Size: "+trainSize[classify]);
		   // System.err.println("Test Size: "+testSize[classify]);

		    DB= getSubset(background,classify,testSize,f,trainSize);
		    Vector[] Model=getOppositeSet(background,classify,testSize,f,trainSize);

		    
		   // DB= getSubset(background,classify,testSize[classify],f,trainSize[classify]);
		    //Vector[] Model=getOppositeSet(background,classify,testSize[classify],f,trainSize[classify]);
		    results[classify]=new Hist[DB.length][];
		    //for(int dbiter=0;dbiter<DB.length;dbiter++)
		   // {
			
		   // }
		    for(int dbiter=0;dbiter<DB.length;dbiter++)
		    {
		    	if(DB[dbiter]==null || DB[dbiter].size()==0)
		    		continue;
		    	Hist[] dbhist=(Hist[])DB[dbiter].toArray(new Hist[1]);
	    	    simProb = SimBasisProb.genBasisProb((Hist[])Model[dbiter].toArray(new Hist[0]),11);
		        
	 
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
			    //System.err.printf("# of closed sub-vectors: %d, # of accurate pvalue: %d, # of top-K updates: %d\n",cntEval, cntAccurate, cntUpdate);
				    //System.err.printf("# of future: %d\n", cntFuture);
				
				    // Format: minSupport% numFreqBin runtime cntEval cntAccurate cntUpdate cntResults
				   // System.out.printf("%.1f %d %d %d %d %d\n", 100.0 * minSup / dbhist.length, numFreqBin, cntEval, cntAccurate,cntUpdate, results.length);
			    
		    }
	    }

		SVM(f);

	    System.err.println("Total time: "+(System.currentTimeMillis()-time0));
	    //new Scanner(System.in).next();
    }
   
    
  }



private static StringBuffer stringRep(double[] fv) {
	// TODO Auto-generated method stub
	StringBuffer rep=new StringBuffer("");
	for(int i=0;i<fv.length;i++)
	{
		rep.append((i+1));
		rep.append(":");
		rep.append(fv[i]);
		rep.append(" ");
	}
	rep.append("\n");
	return rep;
}

private static void SVM(int fold) throws FileNotFoundException {
	// TODO Auto-generated method stub
    Hist[][] classResults=new Hist[classList.length][];
    int dim=0;
    for(int i=0;i<classList.length;i++)
    {
	    classResults[i]=getResults(results[i]);
	    dim+=classResults[i].length;
            System.out.println("tot "+i+" : "+classResults[i].length);
    }

    Hist[][] labelSet=new Hist[background.length][];
	for(int b=0;b<background.length;b++)
	{
		if(background[b].size()==0)
			labelSet[b]=null;
		else
			labelSet[b]=(Hist[])background[b].toArray(new Hist[1]);
	}
	long time=System.currentTimeMillis();
    HashMap<String,double[]> scores=new HashMap();
    double[] def =new double[dim];
    //Arrays.fill(def, 0.0);
    int index=0;
    for(int i=0;i<classResults.length;i++)
    {
    	for(int j=0;j<classResults[i].length;j++)
    	{
    		for(int b=0;b<labelSet.length;b++)
    		{
    			if(labelSet[b]==null)
    				continue;
    			
    			for(int l=0;l<labelSet[b].length;l++)
    			{
    				String id=labelSet[b][l].id.trim().split("-")[0];
    				double dist=calcDistance(labelSet[b][l],classResults[i][j]);
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
    }
    
    

    StringBuffer testfile=new StringBuffer("");
    StringBuffer trainfile=new StringBuffer("");

    for(int i=0;i<classList.length;i++)
    {
    	for(int j=0;j<classList[i].length;j++)
    	{
    		if(scores.get(classList[i][j].trim())==null)
    			continue;
    		String id=classList[i][j].trim();
    		if(testset.containsKey(id))
    		{
	    		testfile.append(i+" ");
	    		testfile.append(stringRep((double[])scores.get(id)));
    		}
    		else
    		{
	    		trainfile.append(i+" ");
	    		trainfile.append(stringRep((double[])scores.get(id)));
    			
    		}
    	}
    }
    FileUtils.writeToFile("train"+fold+".txt", trainfile.toString());
    FileUtils.writeToFile("test"+fold+".txt", testfile.toString());

	System.out.println(System.currentTimeMillis()-time);
}

private static double calcDistance(Hist queryHist, Hist sigHist) {
	// TODO Auto-generated method stub
	double dist=0;
	for(int i=0;i<queryHist.hist.length;i++)
	{
		if(queryHist.hist[i]<sigHist.hist[i])
			return 0;
		dist+=queryHist.hist[i]-sigHist.hist[i];
	}
	return dist;
}




private static int[][] mode(PriorityQueue[] p,int k) {
	// TODO Auto-generated method stub
	int posCount=0;
	int negCount=0;
	int[] count=new int[p.length];
	for(int n=0;n<p.length;n++)
	{
		for(int i=0;i<k && i<p[n].size();i++)
		{
			Cand cand=(Cand)p[n].poll();

			if(cand.c[0]==Integer.MAX_VALUE)
				cand.c[0]=10;

			count[n]+=(10-cand.c[0]);

		}
	}
	//System.out.println("Distance to Positive: "+dist);
	int min=0;
	int id=-1;
	for(int i=0;i<count.length;i++)
	{
			if(count[i]>=min)
			{
				min=count[i];
				id=i;
			}
	}
	int[][] mode=new int[p.length][2];
	mode[0][0]=min;
	mode[0][1]=id;
	int c=1;
	//System.out.println(mode[0][1]);
	for(int i=0;i<count.length;i++)
	{
		if(i!=mode[0][1])
		{

			mode[c][0]=count[0];
			//System.out.println(c);
			mode[c][1]=i;
			//System.out.println(c);
			c++;
		}	
	}
	return mode;
}

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

private static double calcScore(int[] hist, Hist[] posResults, Hist[] negResults) {
	// TODO Auto-generated method stub
	
	int minPos=Integer.MAX_VALUE;
	double pPos=1;

	for(int i=0;i<posResults.length;i++)
	{
		if(posResults[i]==null)
			continue;

		int currSum=getScore(hist,posResults[i]);
		if(currSum<minPos)
		{
			minPos=currSum;
			pPos=new Double(posResults[i].id.split(",")[2]);
		}
	
	}
	int minNeg=Integer.MAX_VALUE;
	double pNeg=1;
	for(int i=0;i<negResults.length;i++)
	{
		if(negResults[i]==null)
			continue;

		int currSum=getScore(hist,negResults[i]);
		if(currSum<minNeg)
		{
			minNeg=currSum;
			pNeg=new Double(negResults[i].id.split(",")[2]);
		}
		
	}	

	if(minPos>minNeg)
	{
		//System.out.println("returning negative score");
		return (-1.0/(minNeg*pNeg+0.00000001));
	}
	else
		return(1.0/(minPos*pPos+0.00000001));
	
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

private static Vector[] getOppositeSet(Vector[] background, int val,int size, int fold, int totSize) {
	// TODO Auto-generated method stub
	

	
	
	HashMap m;
	String[] list;
	list=classList[val];

	m=new HashMap();
	for(int i=0;i<list.length && i<totSize;i++)
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
			if(m.containsKey(labelSet[j].id.trim().split("-")[0]))
			{
					//System.out.println(i);
					DB[i].add(labelSet[j]);
			}
		}
	}
	return DB;

}

private static Vector[] getSubset(Vector[] background, int val,int size, int fold, int totSize) {
	// TODO Auto-generated method stub
	HashMap m;
	String[] list;
	m=classMap[val];
	list=classList[val];
	fold++;
	m=new HashMap();
	for(int i=0;i<size*fold;i++)
	{
		m.put(list[i].trim(), 1);
	}
	System.err.println("Range: [0,"+(size*fold)+"]");
	for(int i=size*fold;i<list.length && i<size*(fold+1)-1;i++)
	{
		testset.put(list[i], true);
	}
	for(int i=size*(fold+1);i<totSize;i++)
	{
		m.put(list[i].trim(), 1);
	}
	System.err.println("Range: ["+(size*(fold+1))+","+totSize+"]");

	Vector[] DB=new Vector[background.length];
	int count=0;
	for(int i=0;i<background.length;i++)
	{
		if(background[i].size()==0)
			continue;
		Hist[] labelSet=(Hist[])background[i].toArray(new Hist[1]);
		DB[i]=new Vector<Hist>();
		for(int j=0;j<labelSet.length;j++)
		{
			if(m.containsKey(labelSet[j].id.trim().split("-")[0]))
			{
					//System.out.println(i);
					DB[i].add(labelSet[j]);
					count++;
			}
		}
	}
	System.out.println("Count "+count);
	//new Scanner(System.in).next();
	
	return DB;

}

private static void fetchGraphIDS(String classFile, int index) {
	// TODO Auto-generated method stub

	List l=Arrays.asList(FileUtils.getFileContents(classFile).trim().split("\n"));
	//Collections.shuffle(l);
	classList[index]=(String[])l.toArray(new String[1]);
}

private static int getTrainSize(double percent)
{
	int minLength=Integer.MAX_VALUE;
	for(String[] l:classList)
		minLength=Math.min(minLength,l.length);

	for(int i=0;i<classList.length;i++)
	{
		List l=Arrays.asList(classList[i].clone());	
		//l=l.subList(0, (int)(percent*minLength));
		//Collections.shuffle(l);
		classList[i]=(String[])l.toArray(new String[1]);
	}
	return (int)(1.0*minLength);
}

private static int[] getTrainSize(Vector[] background)
{
	int[] minLength=new int[classList.length];
	HashMap<String, Boolean> m=new HashMap<String, Boolean>();
	for(int i=0;i<background.length;i++)
	{
		if(background[i].size()==0)
			continue;
		Hist[] labelSet=(Hist[])background[i].toArray(new Hist[1]);
		
		for(int j=0;j<labelSet.length;j++)
		{
			m.put(labelSet[j].id.trim().split("-")[0],true);

		}
	}
	for(int i=0;i<classList.length;i++)
	{
		ArrayList<String> l=new ArrayList(Arrays.asList(classList[i].clone()));	
		for (int j=0;j<l.size();j++)
		{
			String s=l.get(j);
			if (!m.containsKey(s))
			{
				l.remove(j);
				j--;
			}
		}
			
		Collections.shuffle(l);
		classList[i]=(String[])l.toArray(new String[1]);
		minLength[i]=classList[i].length;
	}
	return minLength;
}

}
