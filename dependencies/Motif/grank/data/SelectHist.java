package grank.data;

import java.io.*;

import grank.transform.*;
/**
 * Select a subset of histograms
 *
 * @author Huahai He
 * @version 1.0
 */
public class SelectHist {
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Usage: ... input_file N output_file");
      System.exit(0);
    }
    Hist[] H = Hist.loadHists(args[0]);
    int N = Integer.parseInt(args[1]);
    Hist[] H2 = new Hist[N];
    int cnt = 0;
    for (int i = 0; i < H.length; i++) {
      if (useful(H[i])) {
        H2[cnt++] = H[i];
        if (cnt >= N) {
          break;
        }
      }
    }
    System.err.printf("%d histograms selected\n", cnt);
    /*for(int i=0;i<cnt;i++) {
      System.out.println(H2[i].size());
    }*/
    Hist.saveHists(H2, cnt, args[2]);
  }

  public static boolean useful(Hist h) {
    int cnt = 0;
    for (int i : h.hist) {
      if (i > 0) {
        cnt++;
      }
    }
    //if (cnt >= 2 && h.hist[1]>0) {
    if(cnt>=5) {
      return true;
    }
    else {
      return false;
    }
  }
}
