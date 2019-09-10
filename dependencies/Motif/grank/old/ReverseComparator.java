package grank.old;

import java.util.*;
import grank.mine.*;

public class ReverseComparator implements Comparator<Answer> {
  public ReverseComparator() {
  }

  public int compare(Answer o1, Answer o2) {
    return -o1.compareTo(o2);
  }

}
