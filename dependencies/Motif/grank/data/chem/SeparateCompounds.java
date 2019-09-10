package grank.data.chem;

import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;

/**
 * Categorize NCI/NIH DTP AIDS antiviral screen data into three categories:
 * confirmed active, confirmed moderate active and confirmed inactive files.
 *
 * @author Huahai He
 * @version 1.0
 */

public class SeparateCompounds {
  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args()<2) {
      System.err.println("Usage: ... [options] compounds_file category_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(1);
    }

    String map_file=opt.getString("map_file","label.map");
    String comp_file=opt.getArg(0);
    String cat_file=opt.getArg(1);
    System.err.println("Load graphs");
    LGraph[] graphs = GraphFile.loadGraphs(comp_file,map_file);

    HashMap<String, Integer> map = loadCategoryFile(cat_file);

    Vector ca = new Vector(2000);
    Vector cm = new Vector(2000);
    Vector ci = new Vector(45000);

    System.err.println("Separating...");
    for (LGraph g : graphs) {
      Integer category = map.get(g.id);
      if (category == null) {
        continue;
      }
      switch (category) {
        case 0:
          ca.addElement(g);
          break;
        case 1:
          cm.addElement(g);
          break;
        case 2:
          ci.addElement(g);
          break;

      }
    }

    System.err.println("Save to ca.txt, cm.txt, ci.txt chem.txt");
    LGraph[] array_ca = new LGraph[ca.size()];
    ca.toArray(array_ca);
    GraphFile.saveGraphs(array_ca, "ca.txt", map_file);
    LGraph[] array_cm = new LGraph[cm.size()];
    cm.toArray(array_cm);
    GraphFile.saveGraphs(array_cm, "cm.txt", map_file);
    LGraph[] array_ci = new LGraph[ci.size()];
    ci.toArray(array_ci);
    GraphFile.saveGraphs(array_ci, "ci.txt", map_file);

    ca.addAll(cm);
    ca.addAll(ci);
    LGraph[] all = new LGraph[ca.size()];
    ca.toArray(all);
    GraphFile.saveGraphs(all, "chem.txt", map_file);

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
