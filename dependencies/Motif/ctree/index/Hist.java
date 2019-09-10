package ctree.index;


import java.util.Arrays;

/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public class Hist implements java.io.Serializable {
  protected short[] hist;

  /**
   * The no-arg constructor is used only for serialization
   */
  protected Hist() {

  }

  public Hist(short[] _hist) {
    hist = _hist;
  }

  /**
   * Return the ceiling of a set of histograms
   * @param hists Hist[]
   * @return Hist
   */
  public static Hist ceiling(Hist[] hists) {
    short[] hist = new short[hists[0].hist.length];
    Arrays.fill(hist, (short) 0);
    for (Hist h : hists) {
      for (int i = 0; i < hist.length; i++) {
        if (hist[i] < h.hist[i]) {
          hist[i] = h.hist[i];
        }
      }
    }
    return new Hist(hist);
  }

  /**
   * Return the floor of a set of histograms
   * @param hists Hist[]
   * @return Hist
   */
  public static Hist floor(Hist[] hists) {
    short[] hist = new short[hists[0].hist.length];
    Arrays.fill(hist, Short.MAX_VALUE);
    for (Hist h : hists) {
      for (int i = 0; i < hist.length; i++) {
        if (hist[i] > h.hist[i]) {
          hist[i] = h.hist[i];
        }
      }
    }
    return new Hist(hist);
  }

  /**
   * Test if this feature is compatible to the given graph feature.
   * If yes, then the graph of this feature is possibly contained in the given graph.
   * @param f GraphFeature
   * @return boolean
   */
  public boolean subHist(Hist f) {
    for (int i = 0; i < hist.length; i++) {
      if (hist[i] > f.hist[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return the number of common counts, i.e., size of the floor
   * @param h Hist
   * @return int
   */
  public static int commonCounts(Hist h1, Hist h2) {
    int cnt = 0;
    for (int i = 0; i < h1.hist.length; i++) {
      cnt += Math.min(h1.hist[i], h2.hist[i]);
    }
    return cnt;
  }
}
