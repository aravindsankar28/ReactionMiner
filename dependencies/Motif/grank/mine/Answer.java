package grank.mine;

/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class Answer implements Comparable<Answer> {
  public int[] hist;
  public int sup;
  public double pvalue;
  public Answer(int[] _hist, int _sup, double _pvalue) {
    hist = _hist;
    sup = _sup;
    pvalue = _pvalue;
  }

  public int compareTo(Answer a) { // reverse order for priority queue
    double tmp = pvalue - a.pvalue;
    if (tmp < 0) {
      return 1;
    }
    else if (tmp > 0) {
      return -1;
    }
    else {
      return sup - a.sup;
    }

  }

}
