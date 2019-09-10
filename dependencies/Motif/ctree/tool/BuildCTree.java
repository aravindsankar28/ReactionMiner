package ctree.tool;

import java.io.*;
import java.util.*;

import ctree.graph.*;
import ctree.mapper.*;

import ctree.lgraph.*;
import ctree.index.*;

import ctree.util.*;

/**
 * Build CTree by heuristic clustering.
 * @author Huahai He
 * @version 1.0
 */

public class BuildCTree {
    public static void usage() {
        System.err.println("Usage: ... [options] graph_file");
        System.err.println("  -m=INT \t minimum number of fan-outs, default=20");
        System.err.println(
                "  -M=INT \t maximum number of fan-outs, default=2*m-1");
        System.err.println(
                "  -mapper=[nbm|bi|wtbi|ss] \t graph mapper, default=nbm\n"
                + "\t\t nbm: Neighbor Biased\n"
                + "\t\t bi: Bipartite\n"
                + "\t\t wtbi: Weighted Bipartite\n"
                + "\t\t ss: State Search");
        System.err.println(
                "  -ctree=FILE \t C-Tree file, default=graph_file with suffix replaced by .ctr");
        System.err.println(
                "  -dim1=INT \t number of dimensions for summarizing vertices, default=97,\n"
                + "\t\t or shinked to # of distinct vertices");
        System.err.println(
                "  -dim2=INT \t number of dimensions for summarizing edges, default=97,\n"
                + "\t\t or shinked to # of distinct edges");
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        Opt opt = new Opt(args);
        if (opt.args() < 1) {
            usage();
            return;
        }

        int m = opt.getInt("m", 20);
        int M = opt.getInt("M", m * 2 - 1);

        String graph_file = opt.getArg(0);

        String ctree_file = opt.getString("ctree");
        if (ctree_file == null) {
            int idx = graph_file.lastIndexOf('.');
            ctree_file = idx >= 0 ? graph_file.substring(0, idx) : graph_file;
            ctree_file += ".ctr";
        }

        GraphMapper mapper = null;
        String tag = opt.getString("mapper", "nbm");
        if (tag.equals("nbm")) {
            mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
        } else if (tag.equals("bi")) {
            mapper = new BipartiteMapper();
        } else if (tag.equals("wtbi")) {
            mapper = new WeightedBipartiteMapper(new LGraphWeightMatrix());
        } else if (tag.equals("ss")) {
            mapper = new StateSearchMapper();
        } else {
            usage();
            return;
        }

        System.err.println("Load graphs");
        LGraph[] graphs = LGraphFile.loadLGraphs(graph_file);

        LabelMap labelMap = new LabelMap(graphs);

        // Dimensions for summarizing graphs
        int dim1 = opt.getInt("dim1", 97);
        int dim2 = opt.getInt("dim2", 97);

        int L = labelMap.size();
        if (dim1 > L) {
            dim1 = L;
        }
        if (dim2 > L * L) {
            dim2 = L * L;
        }

        GraphFactory factory = new LGraphFactory(labelMap, dim1, dim2);
        GraphSim graphSim = new LGraphSim();

        long time0 = System.currentTimeMillis();
        System.err.println("Build ctree");
        CTree ctree = buildCTree(graphs, m, M, mapper, graphSim, labelMap,
                                 factory);

        System.err.printf("Max depth = %d, Min depth = %d\n", ctree.maxDepth(),
                          ctree.minDepth());
        System.err.println("Save to " + ctree_file);
        ctree.saveTo(ctree_file);
        long time = System.currentTimeMillis() - time0;

        System.out.println("Build time: " + time / 1000.0);
    }

    /**
     * Summarize graphs. Each distinct label forms a dimension, count the number
     * of vertices of a graph on each label. Thus obtain a L-dimension vector
     * @param graphs Graph[]
     * @param L the number of distinct labels
     * @return Feature matrix
     */
    static int[][] summarizeGraphs(Graph[] graphs, LabelMap labelMap) {

        int n = graphs.length;
        int nLabels = labelMap.size();
        int[][] F = new int[n][nLabels];
        for (int i = 0; i < n; i++) {
            Arrays.fill(F[i], 0);
            Vertex[] vertices = graphs[i].V();
            for (int j = 0; j < vertices.length; j++) {
                int index = labelMap.indexOf(vertices[j]);
                F[i][index]++;
            }

        }
        /*int[][] F = new int[graphs.length][];
             for(int i =0;i<graphs.length;i++) {
          short[] temp = new GraphFeature(graphs[i]).getFeature();
          F[i] = new int[temp.length];
          for(int j  =0;j<temp.length;j++) {
            F[i][j] = temp[j];
          }
             }*/
        return F;
    }

    /**
     *
     * @param graphs Graph[]
     * @param L number of distinct labels
     * @param m int
     * @param M int
     * @param mapper GraphMapper
     * @return CTree
     */
    public static CTree buildCTree(Graph[] graphs, int m, int M,
                                   GraphMapper mapper, GraphSim graphSim,
                                   LabelMap labelMap, GraphFactory factory) throws
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        // summarize graphs
        int[][] F = summarizeGraphs(graphs, labelMap); // Feature matrix

        int dimensions = F[0].length;
        // range of each dimension
        int[] low = new int[dimensions];
        int[] high = new int[dimensions];
        Arrays.fill(low, Integer.MAX_VALUE);
        Arrays.fill(high, 0);

        Vector cluster0 = new Vector(F.length); //initial cluster
        for (int i = 0; i < F.length; i++) {
            for (int dim = 0; dim < low.length; dim++) {
                if (F[i][dim] < low[dim]) {
                    low[dim] = F[i][dim];
                }
                if (F[i][dim] > high[dim]) {
                    high[dim] = F[i][dim];
                }
            }
            cluster0.addElement(new Integer(i));
        }

        CTree ctree = new CTree(m, M, mapper, graphSim, factory);
        Vector entries = partition(cluster0, low, high, F, graphs, ctree);
        if (entries.size() == 1) {
            ctree.setRoot((CTreeNode) entries.elementAt(0));
        } else {
            assert (entries.size() <= ctree.get_M());
            ctree.setRoot(new CTreeNode(ctree, null, entries, false, true));
        }
        ctree.setSize(graphs.length);
        return ctree;

    }

    /**
     * Hierarchical clustering.
     * @param cluster Vector
     * @param low int[]
     * @param high int[]
     * @param M int[][]
     * @param graphs Graph[]
     * @param ctree CTree
     * @return a vector of CTreeNodes not more than M
     */
    public static Vector partition(Vector cluster, int[] low, int[] high,
                                   int[][] F, Graph[] graphs, CTree ctree) {
        int size = cluster.size();
        if (size <= ctree.get_M()) {
            CTreeNode leaf = makeLeaf(cluster, graphs, ctree);
            Vector v = new Vector();
            v.addElement(leaf);
            return v;
        }

        // find a dimension of maximum range
        int dim = 0;
        int max_range = 0;
        for (int i = 0; i < low.length; i++) {
            if (high[i] - low[i] > max_range) {
                max_range = high[i] - low[i];
                dim = i;
            }
        }
        if (max_range == 0) { // graphs are in one cell
            Vector sub1 = new Vector(size / 2);
            Vector sub2 = new Vector(size / 2);
            for (int i = 0; i < size / 2; i++) {
                sub1.addElement(cluster.elementAt(i));
            }
            for (int i = size / 2; i < size; i++) {
                sub2.addElement(cluster.elementAt(i));
            }
            Vector v1 = partition(sub1, low, high, F, graphs, ctree);
            Vector v2 = partition(sub2, low, high, F, graphs, ctree);
            return merge(v1, v2, ctree);
        }

        // find the median at the dimension
        int[] histogram = new int[high[dim] + 1];
        for (int i = 0; i < size; i++) {
            int index = ((Integer) cluster.elementAt(i)).intValue();
            histogram[F[index][dim]]++;
        }
        int sum = 0;
        int median = 0;
        for (median = 0; median < histogram.length; median++) {
            sum += histogram[median];
            if (sum >= size / 2) {
                break;
            }
        }

        // assign objects to two sub-clusters
        Vector sub1 = new Vector(size / 2 + 1);
        Vector sub2 = new Vector(size / 2 + 1);
        for (int i = 0; i < size; i++) {
            Object o = cluster.elementAt(i);
            int index = ((Integer) o).intValue();
            if (F[index][dim] < median ||
                (F[index][dim] == median && sub1.size() < size / 2)) {
                sub1.addElement(o);
            } else {
                sub2.addElement(o);
            }
        }

        // partition into sub-clusters
        int temp = high[dim];
        high[dim] = median;
        Vector v1 = partition(sub1, low, high, F, graphs, ctree);
        high[dim] = temp;
        temp = low[dim];
        low[dim] = median + 1;
        Vector v2 = partition(sub2, low, high, F, graphs, ctree);
        low[dim] = temp;
        return merge(v1, v2, ctree);

    }

    /**
     * Merge two nodes
     * @param node CTreeNode
     */
    private static Vector merge(Vector sub1, Vector sub2,
                                CTree ctree) {
        assert (sub1.size() > 0 && sub2.size() > 0);
        if (sub1.size() + sub2.size() <= ctree.get_M()) {
            sub1.addAll(sub2);
            return sub1;
        } else {
            assert (sub1.size() + sub2.size() <= 2 * ctree.get_M());
            System.out.println("coming here merge");
            CTreeNode node1 = new CTreeNode(ctree, null, sub1, false, false);
            CTreeNode node2 = new CTreeNode(ctree, null, sub2, false, false);
            Vector v = new Vector();
            v.addElement(node1);
            v.addElement(node2);
            return v;
        }
    }

    private static CTreeNode makeLeaf(Vector cluster, Graph[] graphs,
                                      CTree ctree) {
        assert (cluster.size() <= ctree.get_M());
        Vector entries = new Vector(ctree.get_M());
        for (int i = 0; i < cluster.size(); i++) {
            int index = ((Integer) cluster.elementAt(i)).intValue();
            entries.addElement(graphs[index]);
        }
        return new CTreeNode(ctree, null, entries, true, false);
    }
}
