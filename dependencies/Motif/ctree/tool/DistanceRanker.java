package ctree.tool;

import java.util.*;

import ctree.graph.*;
import ctree.mapper.*;
import ctree.index.*;

/**
 * This class implements the incremental ranking in terms of distance. [HS95, HS99]
 */
public class DistanceRanker {

  private CTree ctree;
  private GraphMapper mapper;
  private GraphDistance graphDist;
  private Graph query;
  private boolean sub;
  private PriorityQueue<RankerEntry> pqueue;
  private int acessCount = 0; // the number of accessed entries

  public DistanceRanker(CTree _ctree, GraphMapper _mapper,
                        GraphDistance _graphDist, Graph _query,
                        boolean _sub) {
    ctree = _ctree;
    mapper = _mapper;
    graphDist = _graphDist;
    query = _query;
    sub = _sub;
    pqueue = new PriorityQueue();
    RankerEntry element = new RankerEntry(0, ctree.getRoot());
    pqueue.add(element);
  }

  /**
   * Return the next Nearest Neighbor
   * @return Data and its feature distance to the query point
   */
  public RankerEntry nextNN() {
    while (!pqueue.isEmpty()) {
      RankerEntry entry = pqueue.poll();
      Object obj = entry.getObject();
      if (obj instanceof Graph) { // object
        return entry;
      }
      else { // index node or leaf node
        CTreeNode node = (CTreeNode) obj;
        //insert all children into pqueue
        for (int i = 0; i < node.getEntries().size(); i++) {
          Object child = node.childAt(i);
          Graph g = node.childGraphAt(i);
          int[] map = mapper.map(query, g);
          double dist = graphDist.d(query, g, map, sub);
          RankerEntry entry2 = new RankerEntry(dist, child);
          pqueue.add(entry2);
          acessCount++;
        }
      }
    }
    return null;
  }

  public int getAccessCount() {
    return acessCount;
  }

}
