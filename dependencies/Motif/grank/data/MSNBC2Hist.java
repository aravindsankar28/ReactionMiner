package grank.data;

import java.util.*;
import java.io.*;
import grank.transform.*;

/**
 * Transform MSNBC sequences to histograms
 *
 * @author Huahai He
 * @version 1.0
 */
public class MSNBC2Hist {
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Usage: ... seq_file hist_file");
      System.exit(0);
    }
    BufferedReader in = new BufferedReader(new FileReader(args[0]));
    PrintWriter out = new PrintWriter(args[1]);

    int m = 17; // dimensions
    String line;
    int cnt = 0;
    while ( (line = in.readLine()) != null) {
      String[] list = line.split(" ");
      int[] hist = new int[m];
      Arrays.fill(hist, 0);
      int size = 0;
      for (String s : list) {
        int i = Integer.parseInt(s);
        hist[i - 1]++;
        size++;
      }
      if (size > 100) {
        //continue;
      }
      out.println(cnt);
      for(int i=0;i<m-1;i++) {
        out.print(hist[i]+" ");
      }
      out.println(hist[m-1]);
      cnt++;
    }
    in.close();
    out.close();
    System.err.printf("Cnt = %d\n", cnt);
  }
}
