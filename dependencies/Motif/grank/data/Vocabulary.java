package grank.data;

import java.util.*;
import java.io.*;
import java.util.regex.*;

/**
 * Generate vocabulary from newsgroups articles
 *
 * @author Huahai He
 * @version 1.0
 */
public class Vocabulary {
  private static Hashtable<String, MyEntry> wmap = new Hashtable<String,
      MyEntry> (100000);
  static class MyEntry implements Comparable {
    public String word;
    public int cnt;
    public MyEntry(String _word) {
      word = _word;
      cnt = 1;
    }

    public int compareTo(Object o) {
      return cnt - ( (MyEntry) o).cnt;
    }

  }

  public static void parseArticle(File file) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(file));
    String line;
    while ( (line = in.readLine()) != null) {
      if (line.length() <= 0) {
        break;
      }
    }
    Scanner sc = new Scanner(in);
    sc.useDelimiter("[^a-zA-Z]+");
    while (sc.hasNext()) {
      String word = sc.next().toLowerCase();
      if (word.length() > 20 || word.length() <= 1) {
        continue;
      }
      MyEntry entry = wmap.get(word);
      if (entry == null) {
        wmap.put(word, new MyEntry(word));
      }
      else {
        entry.cnt++;
      }
    }
    sc.close();
  }

  public static void parseArticles(File file) throws IOException {
    if (file.isDirectory()) {
      System.err.printf("%s\n", file.getName());
      File[] list = file.listFiles();
      for (File f : list) {
        parseArticles(f);
      }
    }
    else {
      //System.err.printf("  %s ...", file.getName());
      parseArticle(file);
      //System.err.println();
    }
  }

  public static void outputVocabulary(String file, int minCnt, int maxK) throws
      FileNotFoundException {
    MyEntry[] array = new MyEntry[wmap.size()];
    wmap.values().toArray(array);
    Arrays.sort(array);
    PrintWriter out = new PrintWriter(file);
    int cnt=0;
    for (int i = 0; i < array.length - maxK; i++) {
      if (array[i].cnt < minCnt) {
        continue;
      }
      out.printf("%d:%d:%s\n", cnt++, array[i].cnt,array[i].word);
    }
    out.close();
    System.err.printf("\nNumber of words: %d\n\n", cnt);
  }

  public static Hashtable<String, Integer> loadVocabulary(String file) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(file));
    Hashtable<String, Integer> wmap = new Hashtable<String, Integer> ();
    String line;
    while((line=in.readLine())!=null) {
      String[] list = line.split(":");
      wmap.put(list[2],Integer.parseInt(list[0]));
    }
    return wmap;
  }
  public static void main(String[] args) throws IOException {
    if (args.length < 4) {
      System.err.println("Usage: ... dir out_file minCnt maxK");
      System.exit(0);
    }
    int minCnt = Integer.parseInt(args[2]);
    int maxK = Integer.parseInt(args[3]);

    parseArticles(new File(args[0]));
    outputVocabulary(args[1], minCnt, maxK);
  }
}
