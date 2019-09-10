package grank.old;

public class Link {
  int edge;
  int end;
  public Link(int _edge, int _end) {
    edge=_edge;
    end=_end;
  }
  public int hashCode() {
    return end<<16+edge;
  }
  public boolean equals(Object o) {
    Link ex=(Link) o;
    return edge==ex.edge && end==ex.end;
  }
}
