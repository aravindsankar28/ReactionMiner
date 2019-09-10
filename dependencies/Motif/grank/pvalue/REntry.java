package grank.pvalue;

/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class REntry {
  public double pvalue;
  public double mean;
   public int mu0;
  public REntry(double _pvalue, double _mean, int _mu0) {
    pvalue = _pvalue;
    mean=_mean;
    mu0 = _mu0;
  }
}
