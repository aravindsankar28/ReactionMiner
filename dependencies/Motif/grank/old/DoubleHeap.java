package grank.old;

import java.util.*;

/**
 * Double heap:
 * 1. Maximum size is fixed;
 * 2. Insert an item to a full heap, the K^th item is replaced
 * 3. Remove the first item from a heap
 *
 * Use two heaps to maintain k items, the other is in reverse order.
 * Items are double linked.
 * At any time, rlink[link[i]]=i for 0<= i <size;
 *
 * Indexes start at 0. At position p,
 * 1. parent = (p-1)/2;
 * 2. left child = p*2+1;
 * 3. right child = p*2+2;
 *
 *
 * Derived from java.util.PriorityQueue.
 *
 * @author Huahai He
 * @version 1.0
 */
public class DoubleHeap<E> {
  private Object[] queue; // heap in proper order
  private Object[] rqueue;
  private int[] link;
  private int[] rlink;
  private int maxSize;
  private int size;
  private Comparator<? super E> comparator;

  public DoubleHeap(int _maxSize) {
    this(_maxSize, null);
  }

  public DoubleHeap(int _maxSize, Comparator<? super E> _comparator) {
    maxSize = _maxSize;
    queue = new Object[maxSize];
    rqueue = new Object[maxSize];
    link = new int[maxSize];
    rlink = new int[maxSize];
    size = 0;
    comparator = _comparator;
  }

  public int size() {
    return size;
  }

  // Swap two items in the heap
  private void swap(int p1, int p2) {
    Object tmp = queue[p1]; // swap
    queue[p1] = queue[p2];
    queue[p2] = tmp;

    int idx = link[p1]; // swap links
    link[p1] = link[p2];
    link[p2] = idx;

    rlink[link[p1]] = p1;
    rlink[link[p2]] = p2;
  }

  // Swap two items in the reverse heap
  private void rswap(int p1, int p2) {
    Object tmp = rqueue[p1]; // swap
    rqueue[p1] = rqueue[p2];
    rqueue[p2] = tmp;

    int idx = rlink[p1]; // swap links
    rlink[p1] = rlink[p2];
    rlink[p2] = idx;

    link[rlink[p1]] = p1;
    link[rlink[p2]] = p2;
  }

  private int compare(Object o1, Object o2) {
    if (comparator == null) {
      return ( (Comparable<E>) o1).compareTo( (E) o2);
    }
    else {
      return comparator.compare( (E) o1, (E) o2);
    }
  }

  // Fix up the head
  private void fixUp(int pos)
  // Current lastIndex position is empty
  // Inserts item into the tree and ensures shape and order properties
  {
    while (pos > 0) {
      int parent = (pos - 1) >> 1; // parent node position
      if (compare(queue[parent], queue[pos]) <= 0) {
        break;
      }

      swap(parent, pos); // swap
      pos = parent;
    }
  }

  // Fix up the reverse heap
  private void fixUpRheap(int pos) {
    // Reverse heap
    while (pos > 0) {
      int parent = (pos - 1) >> 1; // parent node position
      if (compare(rqueue[parent], (E) rqueue[pos]) >= 0) {
        break;
      }
      rswap(parent, pos);
      pos = parent;
    }
  }

  public void insert(E item)
  // Adds item to this priority queue.
  // Throws PriQOverflowException if priority queue already full
  {
    if (size < maxSize) {
      queue[size] = item;
      rqueue[size] = item;
      link[size] = size; // Double link in the two heaps
      rlink[size] = size;
      fixUp(size);
      fixUpRheap(size);
      size++;
    }
    else {
      // replace the K^th item
      if (compare(item, (E) rqueue[0]) >= 0) {
        return; // if the new item >= K^th item, then discard the new item
      }

      int idx = rlink[0];
      assert (link[idx] == 0);
      queue[rlink[0]] = item;
      rqueue[0] = item;
      if (idx == 0 || compare(queue[ (idx - 1) >> 1], queue[idx]) < 0) {
        fixDown(idx);
      }
      else {
        fixUp(idx);
      }
      fixDownRheap(0);
    }
  }

  /**
   * The top item
   * @return E
   */
  public E first() {
    if (size > 0) {
      return (E) queue[0];
    }
    else {
      return null;
    }
  }

  /**
   * The last item
   * @return E
   */
  public E last() {
    if (size > 0) {
      return (E) rqueue[0];
    }
    else {
      return null;
    }
  }

  // Fix down the heap
  private void fixDown(int pos) {
    int left; // left child
    while ( (left = (pos << 1) + 1) < size) {
      if (left < size - 1 && compare(queue[left], queue[left + 1]) > 0) {
        left++; // left indexes smallest child
      }

      if (compare(queue[pos], queue[left]) <= 0) {
        break;
      }
      swap(left, pos);
      pos = left;
    }
  }

  // Fix down the reverse heap
  private void fixDownRheap(int pos) {
    int left;
    while ( (left = (pos << 1) + 1) < size) {
      if (left < size - 1 &&
          compare(rqueue[left], rqueue[left + 1]) < 0) {
        left++; // left indexes largest child
      }

      if (compare(rqueue[pos], rqueue[left]) >= 0) {
        break;
      }
      rswap(left, pos);
      pos = left;
    }
  }

  public E remove() {
    if (size == 0) {
      return null;
    }
    else {
      E result = (E) queue[0];
      queue[0] = queue[size - 1]; // Move the last item to the root
      queue[size - 1] = null; // Drop extra ref to prevent memory leak

      // Update reverse queue and links
      int idx = link[0];
      rqueue[idx] = rqueue[size - 1]; // Swap the removed item with the last item
      rqueue[size - 1] = null;
      link[0] = link[size - 1];
      rlink[idx] = rlink[size - 1];
      rlink[link[size - 1]] = 0;

      size--;
      if (size > 0) {
        fixDown(0);
        if (idx == 0 || compare(rqueue[ (idx - 1) >> 1], rqueue[idx]) > 0) {
          fixDownRheap(idx);
        }
        else {
          fixUpRheap(idx);
        }
      }
      return result;
    }
  }

  public static void main(String[] args) {
    int[] A = {1, 3, 8, 5, 4, 3};
    DoubleHeap<Integer> PQ = new DoubleHeap<Integer> (3);
    for (int z = 3; z <= A.length; z++) {
      for (int i = 0; i < z; i++) {
        PQ.insert(A[i]);
      }
      while (PQ.size() > 0) {
        int v = PQ.remove();
        System.out.printf("%3d", v);
      }
      System.out.println();
    }
  }

}
