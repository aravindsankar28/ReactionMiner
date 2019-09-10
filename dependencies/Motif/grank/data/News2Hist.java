package grank.data;

import java.io.*;
import java.util.*;
import grank.transform.*;

/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class News2Hist {
  private static Hashtable<String, Integer> wmap;

  public static Hist parseArticle(File file, Hashtable<String, Integer> wmap) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(file));
    int[] hist = new int[wmap.size()];
    Arrays.fill(hist, 0);
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
      Integer idx = wmap.get(word);
      if (idx != null) {
        hist[idx]++;
      }
    }
    sc.close();
    return new Hist(file.getName(), hist);
  }

  public static Vector<Hist> parseArticles(File file, Hashtable<String,
                                           Integer> wmap) throws
      IOException {
    Vector<Hist> vect = new Vector<Hist> ();
    if (file.isDirectory()) {
      System.err.printf("%s\n", file.getName());
      File[] list = file.listFiles();
      for (File f : list) {
        Vector<Hist> temp = parseArticles(f, wmap);
        vect.addAll(temp);
      }
      outputHist(file.getName(), vect);
    }
    else {
      Hist hist = parseArticle(file, wmap);
      vect.add(hist);
    }
    return vect;
  }

  public static void outputHist(String file, Vector<Hist> vect) throws
      IOException {
    Hist[] array = new Hist[vect.size()];
    vect.toArray(array);
    Hist.saveHists(array, file + ".hist");
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Usage: ... dir vocabulary_file");
      System.exit(0);
    }

    Hashtable<String, Integer> wmap = Vocabulary.loadVocabulary(args[1]);
    parseArticles(new File(args[0]), wmap);
  }
}
