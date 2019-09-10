package grank.old;

import java.util.*;


/**
 * Compare by upper bound of p-value then support
 * @author Huahai He
 * @version 1.0
 */
public class UpperComparator implements Comparator<Candidate> {
  public int compare(Candidate o1, Candidate o2) {
    double tmp = o1.uPvalue - o2.uPvalue;
    if (tmp < 0) {
      return -1;
    }
    else if (tmp > 0) {
      return 1;
    }
    else {
      return o2.sup - o1.sup;
    }
    //return o2.sup-o1.sup;
  }

}
