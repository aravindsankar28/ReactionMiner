package grank.old;

import java.util.*;
import grank.graph.*;
import grank.mine.*;

public class Environment5 extends Environment {
  // for graph enumeration
  public LGraph[] graphs;
  public int zB;
  public HashMap<LGraph, Integer> pcMap;

  public Environment5(int _m, double[] _p, int _maxZ, int[][] _H, int _nG,
                      int[] _dbZ, int[] _dbN, double _maxPvalue, int _minSup,
                      int _K, int _hZ, PriorityQueue _ans, boolean[] _fbin,
                      LGraph[] _graphs, int _zB,
                      HashMap<LGraph, Integer>      _feaMap) {
    super(_m, _p, null, _maxZ, _H, _nG, _dbZ, _dbN, _maxPvalue, _minSup, _K, _hZ,
          _ans, _fbin);
    graphs=_graphs;
    zB=_zB;
    pcMap=_feaMap;
  }
}
