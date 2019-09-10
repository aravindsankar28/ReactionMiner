package ctree.util;

import java.util.*;

/**
 * <p>Title: Closure Tree</p>
 *
 * Collect data and report aggregate functions, such as min, mean, etc.
 *
 * @author Huahai He
 * @version 1.0
 */
public class DataSum {
  private HashMap<String, Vector<Double>> data = new HashMap();
  public DataSum() {
  }

  /**
   * Add an entry to the vector with the given name. If the vector does not
   * exist then create one.
   * @param name String
   * @param value double
   */
  public void add(String name, double value) {
    Vector<Double> vect = data.get(name);
    if (vect == null) {
      vect = new Vector<Double> ();
      data.put(name, vect);
    }
    vect.addElement(value);
  }

  /**
   * Mean of the vector with the given name.
   * @param name String
   * @return double
   */
  public double mean(String name) {
    Vector<Double> vect = data.get(name);
    if (vect == null) {
      throw new RuntimeException("Statistics: unknow name " + name);
    }
    double avg = 0;
    for (double d : vect) {
      avg += d;
    }
    avg /= vect.size();
    return avg;
  }

  /**
   * Sum of the vector with the given name.
   * @param name String
   * @return double
   */
  public double sum(String name) {
    Vector<Double> vect = data.get(name);
    if (vect == null) {
      throw new RuntimeException("Statistics: unknow name " + name);
    }
    double sum = 0;
    for (double d : vect) {
      sum += d;
    }
    return sum;
  }

  /**
   * Minimum value of the vector with the given name.
   * @param name String
   * @return double
   */
  public double min(String name) {
    Vector<Double> vect = data.get(name);
    double min = Double.POSITIVE_INFINITY;
    for (double d : vect) {
      if (d < min) {
        min = d;
      }
    }
    return min;
  }

  /**
   * Maximum value of the vector with the given name.
   * @param name String
   * @return double
   */
  public double max(String name) {
    Vector<Double> vect = data.get(name);
    double max = Double.NEGATIVE_INFINITY;
    for (double d : vect) {
      if (d > max) {
        max = d;
      }
    }
    return max;
  }

  /**
   * Report a matrix where the names are columns
   * @param names column names
   * @return A matrix M[rows][cols]
   */
  public double[][] report(String ... names) {
    int cols = names.length;
    Vector<Double>[] vects = new Vector[cols];
    for(int i = 0;i<cols;i++) {
      vects[i] = data.get(names[i]);
    }
    int rows=vects[0].size();
    double[][] matrix = new double[rows][cols];
    for(int i =0;i<rows;i++) {
      for(int j = 0;j<cols;j++) {
        matrix[i][j] = vects[j].elementAt(i);
      }
    }
    return matrix;
  }

  /**
   * Report a matrix with primary key on the first column. The first column
   * is the primary key; the last column is the count of the key; the rest
   * are means on the key.
   * @param names String[]
   * @return A matrix H[rows][cols], cols=names.length+1. H[][0] is the key,
   * H[][cols-1] is the count of the key.
   */
  public double[][] reportOnKey(String ... names) {
    double[][] M = report(names);
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for(int i = 0;i<M.length;i++) {
      if(M[i][0]<min) min=(int)M[i][0];
      if(M[i][0]>max) max=(int)M[i][0];
    }
    int rows = max-min+1;
    int cols = M[0].length+1;
    double[][] H = new double[rows][cols];
    for(int i = 0;i<rows;i++) {
      H[i][0] = min+i;
      for(int j = 1;j<cols;j++) H[i][j]=0;
    }
    for(double[] row:M) {
      int key = (int)row[0]-min;
      H[key][cols-1]++;    // counts on the key
      for(int j = 1;j<row.length;j++) {
        H[key][j] += row[j];
      }
    }

    // average
    for(double[] row:H) {
      double count = row[cols - 1];
      if(count==0)continue;
      for(int j =1;j<row.length-1;j++) {
        row[j] /= count;
      }
    }
    return H;
  }

  public boolean containsKey(String name) {
    return data.containsKey(name);
  }

}
