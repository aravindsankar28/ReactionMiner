package grank.old;

import grank.mine.*;

public class InterAnswer extends Answer {
  int pos;   // position of the current search bin
  public InterAnswer(int _pos, int[] _hist, int _sup, double _pvalue) {
    super(_hist,_sup,_pvalue);
    pos=_pos;
  }
}
