package grank.mine;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import joelib2.io.MoleculeIOException;
import tool.BuildGraph;
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
public class MultiClassify {

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
    Vector[] DB;
    int fold=opt.getInt("fold");
    
    results=new Hist[opt.args()-1][][];
    classList=new String[opt.args()-1][];
    classMap=new HashMap[opt.args()-1];
    for(int i=1;i<opt.args();i++)
	    fetchGraphIDS(opt.getArg(i),i-1);

    int trainSize=new Double(getTrainSize(0.3)).intValue();
    int testSize=(fold==1)?0:trainSize/fold;
    System.err.println("Train Size: "+trainSize);
    System.err.println("Test Size: "+testSize);
    argumentsDictionary.set(opt.getArg(0),opt.getArg(0),restart,delta,numTopAtoms);
    long time0 = System.currentTimeMillis();
    if(!opt.getString("FG").equals("none"))
    	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.getString("FG"),opt.hasOpt("relabelAtoms"));
    else
    	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.hasOpt("relabelAtoms"));
    /*
    if(!opt.getString("FG").equals("none"))
    	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph,opt.getString("FG"));
    else
    	background=Hist.loadHistsFromRandomWalkAsSet(argumentsDictionary.graph);
    */
    System.out.println("Random Walk Time: "+(System.currentTimeMillis()-time0));

	
	
    //new Scanner(System.in).next();
     PriorityQueue[] k=new PriorityQueue[classList.length];
     PriorityQueue[] precision=new PriorityQueue[classList.length];

     for(int i=0;i<k.length;i++)
	{
		k[i]=new PriorityQueue();
		precision[i]=new PriorityQueue();
	}
    for(int f=0;f<fold;f++)
    {
	    //System.out.println("Background Size: "+background.length);
	    mu0Flag=false;//EDITED BY SAYAN
	    int totPos=0;
	    int totNeg=0;
	    for(int classify=0;classify<opt.args()-1;classify++)
	    {
		    double[][] simProb=null;  
		    argumentsDictionary.set(opt.getArg(0),opt.getArg(1+classify),restart,delta,numTopAtoms);
		    
		    DB = getSubset(background,classify,testSize,f,trainSize);
		    results[classify]=new Hist[DB.length][];
		    //for(int dbiter=0;dbiter<DB.length;dbiter++)
		   // {
			
		   // }
		    for(int dbiter=0;dbiter<DB.length;dbiter++)
		    {
		    	if(DB[dbiter]==null || DB[dbiter].size()==0)
		    		continue;
		    	Hist[] dbhist=(Hist[])DB[dbiter].toArray(new Hist[1]);
	    	    simProb = SimBasisProb.genBasisProb((Hist[])background[dbiter].toArray(new Hist[0]),11);
		        
	 
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
	    int classify=opt.getInt("mode");
	    if(classify==1)
	    {
	    	System.out.println("Add Classify");
	    	addClassify(opt.getArg(1),opt.getArg(2));
	    }
	    else
		    if (classify==2)
		    {
		    	System.out.println("NN Classify");
		    	double[] auc=NNClassify(opt.getInt("k",3),testSize,f);
				for(int i=0;i<auc.length/2;i++)
				{
					k[i].add(-auc[i]/testSize);
					precision[i].add(-(auc[i]/auc[i+classList.length]));
				}
		    }
		    else if (classify==3)
		    {
		    	SVM();
		    	return;
		    }

	    System.err.println("Total time: "+(System.currentTimeMillis()-time0));
	    //new Scanner(System.in).next();
    }
   
    for(int j=0;j<k.length;j++)
    {
	    double avg=0;
	    double preAVG=0;
	    for (int i=0;i<3;i++)
	    {
	    	Double d=(-1*(Double)k[j].poll());
	    	Double p=(-1*(Double)precision[j].poll());

	    	avg+=d/3;
		preAVG+=p/3;
	    	//System.err.print(d+" ");
	    }
	    System.err.println("\nAverage Recall "+j+": "+avg);
	    System.err.println("\nAverage Precision "+j+": "+preAVG);
    }
	    
  }


private static Vector<Hist> getOtherClasses(Vector<Hist> background, String[] ids) {
	// TODO Auto-generated method stub
	Vector<Hist> subset=new Vector<Hist>();
	for(Hist h:background)
	{
		boolean found=false;
		for(String id:ids)
			if(h.id.contains(id))
				found=true;
		if(!found) 
			subset.add(h);
	}
	//System.out.println(subset.size());
	return subset;
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

private static void SVM() throws FileNotFoundException {
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
    
    

    StringBuffer file=new StringBuffer("");

    for(int i=0;i<classList.length;i++)
    {
    	for(int j=0;j<classList[i].length;j++)
    	{
    		if(scores.get(classList[i][j].trim())==null)
    			continue;
    		file.append(i+" ");
    		file.append(stringRep((double[])scores.get(classList[i][j].trim())));
    	}
    }
    FileUtils.writeToFile("train.txt", file.toString());
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

@SuppressWarnings("unchecked")
private static double[] NNClassify(int k,int size, int fold) {
	// TODO Auto-generated method stub
    Hist[][] classResults=new Hist[classList.length][];
    for(int i=0;i<classList.length;i++)
    {
	    classResults[i]=getResults(results[i]);
            System.out.println("tot "+i+" : "+classResults[i].length);
    }
    int[][][] AUCresults=new int[classList.length][k*10+1][2];
    int removed=0;    
   /* 
    for(int v=0;v<classResults.length;v++)
    {
	for(int i=0;i<classResults[v].length;i++)
	{
		for(int n=0;n<classResults.length;n++)
		{
		    if (n==v)
			continue;
		    	Hist h1=classResults[v][i];
		    	boolean closeh1=false;
		    	for(int j=0;j<classResults[n].length;j++)
		    	{
		    		boolean closeh2=true;
		    		Hist h2=classResults[n][j];
		    		for(int m=0;m<h1.hist.length;m++)
		    			if((h1.hist[m]!=0 && h2.hist[m]==0) || (h1.hist[m]==0 && h2.hist[m]!=0) || Math.abs(h1.hist[m]-h2.hist[m])>=1)
		    				closeh2=false;
		    		if(closeh2)
		    		{
		    			Arrays.fill(h2.hist,0);
		    			closeh1=true;
		    			removed++;
		    		
		    		}
		    	}
		    	if(closeh1)
		    	{
		    		Arrays.fill(h1.hist,Integer.MAX_VALUE);
		    		removed++;
		    	}
		    }
	}	
    }*/
    System.out.println("removed: "+removed);
    for(int m=0;m<classResults.length;m++)
    	System.out.println("tot: "+classResults[m].length);
    HashMap scores=new HashMap();
    HashMap posscores=new HashMap();

    TreeMap dist=new TreeMap();
    for(int i=0;i<background.length;i++)
    {
		if(background[i].size()==0)
			continue;
		Hist[] labelSet=(Hist[])background[i].toArray(new Hist[1]);

		for(int j=0;j<labelSet.length;j++)
		{
			String id=labelSet[j].id.trim().split("-")[0];
			PriorityQueue[] currScore=(PriorityQueue[]) scores.get(id);
			
			Integer[][] nodeScore=calcClosest(labelSet[j],classResults);

			if(currScore!=null)
			{
				for(int m=0;m<currScore.length;m++)
					currScore[m].add(new Cand(id,nodeScore[m]));
				//if(nodeScore[0]<currScore[0])
				scores.put(id,currScore);

			}
			else
			{
				PriorityQueue[] p =new PriorityQueue[nodeScore.length];
				for(int m=0;m<p.length;m++)
				{
					p[m]=new PriorityQueue<Cand>(k,new CandComparator());
					p[m].add(new Cand(id,nodeScore[m]));
				}
				scores.put(id,p);

			}
			
		}
    }
    //new Scanner(System.in).next();
    int[] classSize=new int[classList.length];
    int[] classCount=new int[classList.length];
    int [] classTot=new int[classList.length];
    for(int m=0;m<classList.length;m++)
    {
   
	    String[] list=classList[m].clone();


	    //System.err.println("PosRange: ["+(size*fold)+","+((size*(fold+1))<list.length?size*(fold+1):list.length)+"]");
	    for(int i=fold*size;i<size*(fold+1)&& i<list.length;i++)//list.length;i++)
	    {
	    	if(scores.get(list[i].trim())==null)
	    		continue;
	    	classSize[m]++;
	    	//Integer[] cand=((Integer[])scores.get(list[i].trim()));
	    	PriorityQueue[] p=((PriorityQueue[])scores.get(list[i].trim()));

	    	int[][] cl=mode(p,k);
		classTot[cl[0][1]]++;
		//System.out.println(Arrays.toString(cl[0]));
		if(cl[0][1]==m)
	    	{
	  		classCount[m]++;
		}

		for(int j=0;j<cl.length;j++)
		{
		    	if(cl[j][1]==m)
		    	{
		    		//classCount[m]++;

		    		AUCresults[cl[j][1]][cl[j][0]][0]++;
		    	}
		    	else
		    	{
		    		AUCresults[cl[j][1]][cl[j][0]][1]++;
		    	}
		}
		//System.out.println(); 
	    	
	    }
	    //System.out.println("-----------------------------");
    }

    System.out.println("--------------------------------------------");
    
   
    /*for(int i=0;i<AUCresults.length;i++)
    {
	for(int j=0;j<AUCresults[i].length;j++)
		if(AUCresults[i][j][0]!=0 || AUCresults[i][j][1]!=0)
		System.out.println(Arrays.toString(AUCresults[i][j]));
        System.out.println("================================================");
    }*/
    String x="";
    String y="";
    /*for(int i=0;i<results.length;i++)
    {
    	System.out.println(i+": "+results[i][0]+"\t"+results[i][1]);
    }*/
    //System.out.println("--------------------------");
    Integer[] keyset=(Integer[])dist.keySet().toArray(new Integer[1]);
    
    /*for(int i=0;i<keyset.length;i++)
    {
    	double spec=0,sens=0;
    	Integer[] d=(Integer[])dist.get(keyset[i]);
    	tp+=d[0];
    	fp+=d[1];
    	tn+=d[2];
    	fn+=d[3];
    	if(tn!=0)
    		spec=tn*1.0/totPosSize;//(tn+fp);
    	if(tp!=0)
    		sens=tp*1.0/totPosSize;//(tp+fn);
    	System.out.println(keyset[i]+" : "+sens+"\t"+spec+"\t"+tp+"\t"+tn);
    }*/

    System.out.println(Arrays.toString(classCount));
    System.out.println(Arrays.toString(classSize));
    double[] allAUC=new double[AUCresults.length*2];
    for(int m=0;m<AUCresults.length;m++)
    {
	  int tp=0,fp=0,tn=0,fn=0;
	  int poscount=classCount[m];
	  int totPosSize=classSize[m];
	  int totNegSize=0;
	  int negcount=0;
	  for(int i=0;i<classCount.length;i++)
	  {
		if(i!=m)
		{
			negcount+=classCount[i];
			totNegSize+=classSize[i];
		}
	  }
	  double auc=0,oldsens=0,oldspec=0;
  	  for(int i=AUCresults[m].length-1;i>=0;i--)
 	  {
	    	double spec=0,sens=0;
	    	
	    	tp+=AUCresults[m][i][0];
	    	tn+=AUCresults[m][i][1];

	    	sens=tp*1.0/totPosSize;
	    	spec=tn*1.0/totNegSize;
	    	x+=sens+"\t";
	    	y+=spec+"\t";
	    	//System.out.println(i+" : "+sens+"\t"+spec+"\t"+tp+"\t"+tn);
	    	if(i==(AUCresults[m].length-1))
	    		allAUC[m]+=0.5*sens*spec;
	    	else
	    		allAUC[m]+=((sens-oldsens)*(spec-oldspec)*0.5+oldsens*(spec-oldspec));
	    	oldsens=sens;
	    	oldspec=spec;
	    }
	    //System.out.println(x);
	    //System.out.println(y);
	    System.out.println("AUC: "+allAUC[m]);
	    System.out.println("Recall/Sensitivity: "+poscount*1.0/totPosSize);
	    System.out.println("Precision: "+poscount*1.0/(poscount+totNegSize-negcount));
	    System.out.println("Specificity: "+negcount*1.0/totNegSize);
	    System.out.println("totPosSize: "+totPosSize);
	    System.out.println("totNegSize: "+totNegSize);
	    System.out.println("posCount: "+poscount);
	    System.out.println("negCount: "+negcount);
	    
	    System.out.println();
	    
    }	
    for(int i=0;i<classCount.length;i++)
    {
	allAUC[i]=classCount[i];
        allAUC[i+classCount.length]=classTot[i];
    }
    return allAUC;
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

private static void addClassify(String posFile, String negFile) {
	// TODO Auto-generated method stub
    Hist[] posResults=getResults(results[0]);
    Hist[] negResults=getResults(results[1]);

    //System.out.println("totPos: "+posResults.length);
    //System.out.println("totNeg: "+negResults.length);
    HashMap scores=new HashMap();
    int poscount=0;
    int negcount=0;
    for(int i=0;i<background.length;i++)
    {
		if(background[i].size()==0)
			continue;
		Hist[] labelSet=(Hist[])background[i].toArray(new Hist[1]);

		for(int j=0;j<labelSet.length;j++)
		{
			String id=labelSet[j].id.trim().split("-")[0];
			Double currScore=(Double) scores.get(id);
			double nodeScore=calcScore(labelSet[j].hist,posResults,negResults);
			
			if(currScore!=null)
			{
				scores.put(id, currScore+nodeScore);
			}
			else
				scores.put(id,nodeScore);

		}
    }
    //new Scanner(System.in).next();
    String[] list=FileUtils.getFileContents(posFile).trim().split("\n");
    //System.out.println("("+posFile+")");
    //System.out.println("--------------------------------------------");
   int totSize=0;
 
    for(int i=0;i<600;i++)//list.length;i++)
    {
    	if(scores.get(list[i].trim())==null)
    		continue;
    	totSize++;
    	//System.out.println("("+list[i]+"): " +(Double)scores.get(list[i].trim()));
    	if((Double)scores.get(list[i].trim())>0)
    		poscount++;
    	
    }
    System.out.println("Recall: "+poscount*1.0/totSize);
    totSize=0;
    list=FileUtils.getFileContents(negFile).trim().split("\n");
    //System.out.println("("+negFile+")");
    //System.out.println("--------------------------------------------");
   
    for(int i=0;i<600;i++)//list.length;i++)
    {
    	if(scores.get(list[i].trim())==null)
    		continue;
    	totSize++;
    	//if((Double)scores.get(list[i].trim())<=0)
    	
    	//System.out.println("("+list[i]+")");
    		//System.out.println("("+list[i]+"): " +(Double)scores.get(list[i].trim()));
    	if((Double)scores.get(list[i].trim())<=0)
    		negcount++;
    	
    }
    System.out.println("Precision: "+poscount*1.0/(totSize-negcount+poscount));
    System.out.println();	
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

private static Vector[] getSubset(Vector[] background, int val,int size, int fold, int totSize) {
	// TODO Auto-generated method stub
	

	HashMap m;
	String[] list;
	m=classMap[val];
	list=classList[val];

	m=new HashMap();
	for(int i=0;i<size*fold;i++)
	{
		m.put(list[i].trim(), 1);
	}
	//System.err.println("Range: [0,"+(size*fold)+"]");
	for(int i=size*(fold+1);i<totSize;i++)
	{
		m.put(list[i].trim(), 1);
	}
	//System.err.println("Range: ["+(size*(fold+1))+","+totSize+"]");
	int c=0;
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
					c++;
			}
		}
	}
	//System.out.println(c);
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
		l=l.subList(0, (int)(percent*minLength));

		classList[i]=(String[])l.toArray(new String[1]);
	}
	return (int)(0.3*minLength);
}


}
