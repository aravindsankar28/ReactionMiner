package grank.mine;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;

import tool.FileUtils;
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
public class MotifHist {

  private static long cntEval = 0; // count of eval (lower bound pvalue)
  private static long cntAccurate = 0; // count of accurate pvalue computations
  private static long cntUpdate = 0; // count of updating top-K
  private static int numFreqBin = 0; // number of frequent bins
  //private static long cntFuture = 0; // count of pruning by future
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
    System.err.printf("\nFrequent bins: %d\n", fcnt);

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
	    	  if(count>=2 || max>=4)
	    	  {
			      int i1 = anssize - i - 1;
			      String id = size + "-" + i1 + "," + a.sup + "," + a.pvalue;
			      Hist h = new Hist(id, a.hist);
			      buf.add(h);
	    	  }
	      }
	    }
	    boolean skip=false;
	    
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
	    				if((h1.hist[k]!=0&& h2.hist[k]==0) || (h1.hist[k]==0&& h2.hist[k]!=0) || Math.abs(h1.hist[k]-h2.hist[k])>1)
	    					close=false;
	    			}
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
	    Hist[] results=new Hist[buf.size()];
	    buf.toArray(results);   
	    System.out.println("am i coming here22222");
	    return results;
}

public static void main(String[] args) throws IOException, MathException {
    Opt opt = new Opt(args);
    if (opt.args() < 1) {
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

    double maxPvalue = 0.1;//opt.getDouble("pvalue", 1); EDITED BY SAYAN
   
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
    //String model = opt.getString("model", "complex");
    mu0Flag=false;//EDITED BY SAYAN
    System.err.println("Load histograms");
    //Hist[] DB = Hist.loadHists(opt.getArg(0)); // database histograms
    Hist[] DB;
    double[][] simProb;
    if(opt.args()==2)
    {
	    argumentsDictionary.set(opt.getArg(0),opt.getArg(1),restart,delta,numTopAtoms);
	    Hist[] background=Hist.loadHistsFromRandomWalk(argumentsDictionary.graph);
	    DB = getSubset(background,argumentsDictionary.ids);
	    simProb = SimBasisProb.genBasisProb(background,11);
    }
    else
    {
	    argumentsDictionary.set(opt.getArg(0),opt.getArg(0),restart,delta,numTopAtoms);
	    DB=Hist.loadHistsFromRandomWalk(argumentsDictionary.graph);
	    simProb = SimBasisProb.genBasisProb(DB,11);
    }
    //DB=filter(DB,100, 3);
    int[][] H = new int[DB.length][DB[0].hist.length];
    for (int i = 0; i < H.length; i++) {
      H[i] = DB[i].hist;
    }

    // minimum support, either integer or percentage
    String tmp = opt.getString("minSup", "1");
    int minSup;
    if (tmp.endsWith("%")) {
      double ratio = Double.parseDouble(tmp.substring(0, tmp.length() - 1));
      minSup = (int) Math.ceil(DB.length * ratio / 100);
    }
    else {
      minSup = Integer.parseInt(tmp);
    }
    minSup=(int)(0.001*DB.length);//edited by Sayan
    int maxZ = Hist.maxSize(DB); // maximum histogram size
    if (hZ > maxZ) {
      hZ = maxZ;
    }

    double[] prob = null;//model.equals("complex") ? BasisProb.loadProb(opt.getArg(1)) : null; // feature probabilities
    //double[][] simProb = null;//model.equals("simple") ? SimBasisProb.loadProb(opt.getArg(1)) : null;
    //double[] prob=vertexDictionary.getProb();
    
   // for(int i=0;i<DB.length;i++)
    //	simProb[i]=prob.clone();
    /*
     for (int i = 0; i < p.length - 1; i++) {
      if (p[i] < p[i + 1]) {
        throw new RuntimeException("Assertion error: prob. are not sorted");
      }
         }
     */

    boolean toEval = opt.getString("eval", "yes").equalsIgnoreCase("yes");
    boolean preEval = opt.getString("preEval", "yes").equalsIgnoreCase("yes");
    boolean verbose = opt.hasOpt("verbose");

    long time0 = System.currentTimeMillis();
    System.err.println("Start mining ...");
    Hist[] results = mainProcess(maxPvalue, K, minSup, H, prob, simProb, hZ,
                                 toEval, preEval, verbose);

    String sig_hist = opt.getString("sig_hist", "sig.hist");
    System.out.println(System.currentTimeMillis()-time0);
    Hist.saveContainers(results,DB);
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    double runtime = (time1 - time0) / 1000.0;
    System.err.printf("Time: %.2f sec\n", runtime);
    System.err.printf(
        "# of closed sub-vectors: %d, # of accurate pvalue: %d, # of top-K updates: %d\n",
        cntEval, cntAccurate, cntUpdate);
    //System.err.printf("# of future: %d\n", cntFuture);

    // Format: minSupport% numFreqBin runtime cntEval cntAccurate cntUpdate cntResults
    System.out.printf("%.1f %d %.2f %d %d %d %d\n", 100.0 * minSup / DB.length,
                      numFreqBin, runtime, cntEval, cntAccurate,
                      cntUpdate, results.length);
  }

private static Hist[] getSubset(Hist[] background, String ids) {
	// TODO Auto-generated method stub
	String[] list=FileUtils.getFileContents(ids).trim().split("\n");
	Vector<Hist> DB=new Vector<Hist>();
	HashMap m=new HashMap();
	for(int i=0;i<list.length;i++)
	{
		m.put(list[i].trim(), 1);
	}
	for(int i=0;i<background.length;i++)
	{
		if(m.containsKey(background[i].id.trim().split("-")[0]))
		{
				//System.out.println(i);
				DB.add(background[i]);
		}
	}
	return (Hist[]) DB.toArray(new Hist[0]);
}



}
