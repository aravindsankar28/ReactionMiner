package grank.mine;

import java.util.*;
import grank.graph.*;

/**
 * Parameters environment
 * @author Huahai He
 * @version 1.0
 */
public class Environment {
  public int m;       // dimensions
  public double[] p;  // background probability
  public double[][] simP;

  public int maxZ;
  public int[][] HD;  // histogram database

  public int nG;      // number of database graphs
  public int[] dbZ;
  public int[] dbN;
  public double maxPvalue;
  public int minSup;
  public int K;
  public int hZ;
  public boolean toEval;
  public PriorityQueue<Answer> ans;

  public boolean[] fbin; // frequent bins

  public boolean preEval;  // whether to compute the lower bound of p-value before accurate p-value
  public boolean verbose;
  public Environment(int _m, double[] _p, double[][] _simP, int _maxZ, int[][] _HD, int _nG,
                     int[] _dbZ, int[] _dbN, double _maxPvalue, int _minSup,
                     int _K, int _hZ, PriorityQueue<Answer> _ans,
      boolean[] _fbin, boolean _toEval, boolean _preEval,boolean _verbose) {
    m = _m;
    p = _p;
    simP=_simP;
    maxZ=_maxZ;
    HD=_HD;
    nG=_nG;
    dbZ=_dbZ;
    dbN=_dbN;
    maxPvalue=_maxPvalue;
    minSup=_minSup;
    K=_K;
    hZ=_hZ;
    ans=_ans;
    fbin=_fbin;
    toEval=_toEval;
    preEval=_preEval;
    verbose = _verbose;
  }
  public Environment(int _m, double[] _p, double[][] _simP, int _maxZ, int[][] _H, int _nG,
                     int[] _dbZ, int[] _dbN, double _maxPvalue, int _minSup,
                     int _K, int _hZ, PriorityQueue<Answer> _ans,
      boolean[] _fbin) {
    this(_m,_p,_simP, _maxZ,_H,_nG,_dbZ,_dbN,_maxPvalue,_minSup,_K,_hZ,_ans,_fbin,true,true,true);
  }
}
