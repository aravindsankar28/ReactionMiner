package grank.pvalue;

/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class REntry2 implements Comparable<REntry2> {
  public String id;
  public double pvalue;
  public int histMu0;
  public int graphMu0;
  public double mean;
  public int hsize;  // histogram size

  public REntry2(String _id, double _pvalue, int _histMu0, int _graphMu0,
                 double _mean, int _hsize) {
    id = _id;
    pvalue = _pvalue;
    histMu0 = _histMu0;
    graphMu0 = _graphMu0;
    mean = _mean;
    hsize=_hsize;
  }

  public int compareTo(REntry2 o) {
    if (pvalue < o.pvalue) {
      return -1;
    }
    else if (pvalue == o.pvalue) {
      return 0;
    }
    else {
      return 1;
    }
  }

}
