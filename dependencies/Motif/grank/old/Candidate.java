package grank.old;

/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class Candidate implements Comparable<Candidate> {
  int[] hist; // histogram of this candidate at current level, passed down
  int[] seg; // segment of histogram at next level, passed up
  double[] pseg; // lower bound of P(X,N) for the segment, passed up
  double lPvalue; // lower bound of p-value
  double uPvalue; // upper bound of p-value
  int sup;
  public Candidate(int[] _hist, int[] _seg, double[] _plow, double _lPvalue,
                   int _sup) {
    hist = _hist;
    seg = _seg;
    pseg = _plow;
    lPvalue = _lPvalue;
    sup = _sup;
  }

  public String toString() {
    String s = "" + seg[0];
    for (int i = 1; i < seg.length; i++) {
      s += " " + seg[i];
    }
    s += "; " + lPvalue + "; " + sup;
    return s;
  }

  /**
   * Compare by lower bound of p-value then support
   * @param o Candidate
   * @return int
   */
  public int compareTo(Candidate o) {
    double tmp = lPvalue - o.lPvalue;
    if (tmp < 0) {
      return -1;
    }
    else if (tmp > 0) {
      return 1;
    }
    else {
      return o.sup-sup;
    }
  }

}
