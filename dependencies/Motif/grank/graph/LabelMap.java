package grank.graph;

import java.io.*;
import java.util.*;
import ctree.util.*;
import static grank.graph.GraphFile.*;

/**
 * Map labels into integers or vice versa while loading / saving graphs.
 * @author Huahai He
 * @version 1.0
 */
public class LabelMap {
  public HashMap<String, Integer> vmap; // label map of vertices
  public HashMap<String, Integer> emap; // label map of edges

  public String[] vlab;   // labels of vertices
  public String[] elab;   // labels of edges

  /**
   * Load a map file.
   * Format:
   * # of vertex labels
   * 0:label0
   * ...
   * # of edge labels
   * 0:label0
   * ...
   *
   * @param map_file String
   * @throws IOException
   */
  public LabelMap(String map_file) throws IOException {
    // Load vmap and emap
    vmap = new HashMap<String, Integer> ();
    emap = new HashMap<String, Integer> ();
    Scanner sc = new Scanner(new File(map_file));
    int vcnt = sc.nextInt();
    vlab = new String[vcnt];
    for (int i = 0; i < vcnt; i++) {
      int idx = sc.nextInt();
      String label=sc.next();
      vmap.put(label,idx);
      vlab[idx]=label;
    }
    int ecnt = sc.nextInt();
    elab = new String[ecnt];
    for (int i = 0; i < ecnt; i++) {
      int idx=sc.nextInt();
      String label = sc.next();
      emap.put(label,idx);
      elab[idx]=label;
    }
    sc.close();
  }

  /**
   * Generate label maps from a LGraph file.
   * @param fsgfile String
   * @param map_file String
   * @throws IOException
   */
  public static void genLabelMap(String graph_file, String map_file) throws
      IOException {
    HashMap<String, Integer> vmap = new HashMap<String, Integer> ();
    HashMap<String, Integer> emap = new HashMap<String, Integer> ();
    BufferedReader in = new BufferedReader(new FileReader(graph_file));
    int vcnt = 0;
    int ecnt = 0;
    String line;
    while (true) {
      // ID
      line = readLine(in, false);
      if (line == null) {
        break;
      }
      assert (line.startsWith("#"));

      // V
      int n = Integer.parseInt(readLine(in, false));
      for (int i = 0; i < n; i++) {
        String label = readLine(in, false);
        Integer v = vmap.get(label);
        if (v == null) {
          vmap.put(label, vcnt);
          vcnt++;
        }
      }

      // E
      int m = Integer.parseInt(readLine(in, false));
      for (int i = 0; i < m; i++) {
        String[] list = readLine(in, false).split(" ");
        assert (list.length == 3);
        int v1 = Integer.parseInt(list[0]);
        int v2 = Integer.parseInt(list[1]);
        Integer e = emap.get(list[2]);
        if (e == null) {
          emap.put(list[2], ecnt);
          ecnt++;
        }
      }
    }
    in.close();

    // output label maps
    PrintStream out = new PrintStream(map_file);

    // vertex label map
    int Lv = vmap.size();
    String[]  LV= new String[Lv];
    for (Map.Entry<String, Integer> e : vmap.entrySet()) {
      LV[e.getValue()]= e.getKey();
    }
    out.println(Lv);
    for(int i=0;i<Lv;i++) {
    out.printf("%d %s\n", i,LV[i]);
    }

    // Edge label map
    int Le = emap.size();
    String[] LE = new String[Le];
    for (Map.Entry<String, Integer> e : emap.entrySet()) {
      LE[e.getValue()] = e.getKey();
    }
    out.println(Le);
    for(int i=0;i<Le;i++) {
      out.printf("%d %s\n", i,LE[i]);
    }
    out.close();
  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if(opt.args()<1) {
      System.err.println("Generate map file.");
      System.err.println("Usage: ... [-option] graph_file");
      System.err.println("\t -map_file=FILE \t default=label.map");
      System.exit(0);
    }
    String map_file = opt.getString("map_file","label.map");
    String graph_file = opt.getArg(0);
    genLabelMap(graph_file,map_file);
  }
}
