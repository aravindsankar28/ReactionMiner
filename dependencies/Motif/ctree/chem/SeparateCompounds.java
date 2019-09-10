package ctree.chem;

import java.io.*;
import java.util.*;

import ctree.lgraph.*;

/**
 * Categorize NCI/NIH AIDS antiviral screen data into three categories:
 * confirmed active, confirmed moderate active and confirmed inactive files.
 *
 * @author Huahai He
 * @version 1.0
 */

public class SeparateCompounds {
  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("Usage: ... compounds_file category_file");
      System.exit(1);
    }
    System.err.println("Load graphs");
    LGraph[] graphs = LGraphFile.loadLGraphs(args[0]);

    HashMap<String, Integer> map = loadCategoryFile(args[1]);

    Vector ca = new Vector(2000);
    Vector cm = new Vector(2000);
    Vector ci = new Vector(45000);

    System.err.println("Categorize");
    for (int i = 0; i < graphs.length; i++) {
      int category = map.get(graphs[i].getId());
      switch (category) {
        case 0:
          ca.addElement(graphs[i]);
          break;
        case 1:
          cm.addElement(graphs[i]);
          break;
        case 2:
          ci.addElement(graphs[i]);
          break;

      }
    }

    System.err.println("Save to ca.txt, cm.txt, ci.txt chem.txt");
    LGraph[] array_ca = new LGraph[ca.size()];
    ca.toArray(array_ca);
    LGraphFile.saveLGraphs(array_ca, "ca.txt");
    LGraph[] array_cm = new LGraph[cm.size()];
    cm.toArray(array_cm);
    LGraphFile.saveLGraphs(array_cm, "cm.txt");
    LGraph[] array_ci = new LGraph[ci.size()];
    ci.toArray(array_ci);
    LGraphFile.saveLGraphs(array_ci, "ci.txt");

    ca.addAll(cm);
    ca.addAll(ci);
    LGraph[] all = new LGraph[ca.size()];
    ca.toArray(all);
    LGraphFile.saveLGraphs(all, "chem.txt");

  }

  public static HashMap<String, Integer> loadCategoryFile(String file) throws
      FileNotFoundException, IOException {
    // read the conclusion file
    BufferedReader in = new BufferedReader(new FileReader(file));
    HashMap map = new HashMap(45000);
    String line = in.readLine();
    while ( (line = in.readLine()) != null) {
      String[] pair = line.split(",");
      int index = line.indexOf(',');
      String id = pair[0].trim();
      String category = pair[1].trim();
      int type;
      if (category.equals("CA")) {
        type = 0;
      }
      else if (category.equals("CM")) {
        type = 1;
      }
      else if (category.equals("CI")) {
        type = 2;
      }
      else {
        throw new RuntimeException("Unknown category");
      }
      map.put(id, type);
    }
    in.close();
    return map;
  }

}
