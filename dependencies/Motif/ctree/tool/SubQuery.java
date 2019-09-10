package ctree.tool;

import java.util.*;


import ctree.index.*;
import ctree.graph.*;
import ctree.mapper.*;

import ctree.util.*;

import java.io.*;
import ctree.lgraph.*;


/**
 * Subgraph queries
 *
 * @author Huahai He
 * @version 1.0
 */

public class SubQuery {

    public static void usage() {
        System.err.println(
                "Usage: ... [options] ctree_file query_file");
        System.err.println(
                "  -nQ=INT \t\t number of queries, default=queries in query_file");
        System.err.println(
                "  -hist=[yes|no] \t whether to filter by histograms, default=yes");
        System.err.println(
                "  -pseudo=INT \t\t pseudo subgraph isomorphism level, default=1");
        System.err.println(
                "  -output=FILE \t\t if this option is specified, then output the answers");
        System.err.println("  -candout=FILE \t if this option is specified, then output the candidate answers");
    }

    public static void main(String[] args) throws Exception {
        Opt opt = new Opt(args);
        if (opt.args() < 2) {
            usage();
            return;
        }
        System.err.println("Load ctree " + opt.getArg(0));
        CTree ctree = CTree.load(opt.getArg(0));

        Graph[] queries = LGraphFile.loadLGraphs(opt.getArg(1));

        int nQ = opt.getInt("nQ", queries.length);
        boolean usingHist = opt.getString("hist", "yes").equals("yes");

        int pseudo_level = opt.getInt("pseudo", 1);

        String output = opt.getString("output");
        PrintWriter out = null;
        if (output != null) {
             out=new PrintWriter(output);
        }
        Vector<Graph> answers = new Vector<Graph>();

        String candout_file = opt.getString("candout");
        PrintWriter candout = null;
        if(candout_file!=null) {
            candout = new PrintWriter(candout_file);
        }
        DataSum stat = new DataSum();

        double[] avgHits1 = new double[10], avgHits2 = new double[10];
        Arrays.fill(avgHits1, 0);
        Arrays.fill(avgHits2, 0);

        System.err.println("Query");
        for (int i = 0; i < nQ; i++) {

            Arrays.fill(hits1, 0);
            Arrays.fill(hits2, 0);
            Arrays.fill(counts, 0);
            Arrays.fill(cands, 0);
            answers.clear();

            // NN ranking for distance==0
            long time0 = System.currentTimeMillis();

            //Vector cand = isomQueryByRanker(ctree, metric, queries[i]);
            Vector<Graph>
                    cand = subgraphQuery(ctree, queries[i], pseudo_level,
                                         usingHist);

            long time1 = System.currentTimeMillis() - time0;

            // check isomorphism
            int ans_size = 0;
            for (Graph g : cand) {
                if (Util.subIsomorphic(queries[i], g)) {
                    ans_size++;
                    answers.add(g);
                }
            }
            long time2 = System.currentTimeMillis() - time0;

            // Output answers to this query
            if (output != null) {
                out.println(answers.size());
                for (Graph g : answers) {
                    out.println(((LGraph) g).getId());
                }
            }

            // Output candidate answers to this query
            if(candout!=null) {
                if(cand==null) {
                    candout.println(0);
                } else {
                    candout.println(cand.size());
                    for (Graph g : cand) {
                        candout.println(((LGraph) g).getId());
                    }
                }
            }

            // statistics
            int cand_size = cand.size();
            stat.add("filter_time", time1);
            stat.add("process_time", time2);
            stat.add("cand_size", cand_size);
            stat.add("ans_size", ans_size);
            double accuracy = cand_size == 0 ? 1 :
                              (double) ans_size / cand_size;
            stat.add("accuracy", accuracy);
            stat.add("access_ratio", (double) accessCount / ctree.size());

            for (int d = 1; d < 10; d++) {
                if (counts[d] == 0) {
                    break;
                }
                avgHits1[d] += (double) hits1[d] / counts[d];
                avgHits2[d] += (double) hits2[d] / counts[d];
            }

            if ((i + 1) % 10 == 0) {
                System.err.println("Query at " + (i + 1));
            }
        } // end of query

        if (output != null) {
            out.close();
        }
        if(candout!=null){
            candout.close();
        }

        // Format: filter_time process_time cand_size ans_size accuracy access_ratio
        System.err.println(
                "Format: filter_time process_time cand_size ans_size accuracy access_ratio");
        System.out.printf("%f %f %f %f %f %f\n", stat.mean("filter_time"),
                          stat.mean("process_time"), stat.mean("cand_size"),
                          stat.mean("ans_size"), stat.mean("accuracy"),
                          stat.mean("access_ratio"));
        /*
             // hits on C-tree nodes
             for (int d = 1; d < 10; d++) {
          if (avgHits1[d] == 0) {
            break;
          }
          System.err.printf("%d: hits1=%2.2f%%, hits2=%2.2f%%\n", d,
                            100 * avgHits1[d] / nQ,
                            100 * avgHits2[d] / nQ);
             }
         */
    }

    private static int accessCount;

    /**
     * Query using NNRanker
     * @param ctree CTree
     * @param metric GraphMetric
     * @param query Graph
     * @return Vector
     * @deprecated
     */
    public static Vector<Graph> subgraphQueryByRanker(CTree ctree,
            GraphMapper mapper,
            GraphDistance graphDist,
            Graph query) {
        DistanceRanker ranker = new DistanceRanker(ctree, mapper, graphDist,
                query, true);
        accessCount = 0;
        RankerEntry entry;
        Vector<Graph> cand = new Vector(); // candidate set
        while ((entry = ranker.nextNN()) != null && entry.getDist() == 0) {
            cand.addElement(entry.getGraph());
        }
        accessCount = ranker.getAccessCount();
        return cand;
    }

    /**
     * Query by visiting nodes recursively.
     * @param ctree CTree
     * @param query Graph
     * @param mapper GraphMapper
     * @param pseudo_level int
     * @return Vector
     */
    public static Vector<Graph> subgraphQuery(CTree ctree, Graph query,
                                              int pseudo_level,
                                              boolean usingHist) {
        Vector<Graph> cand = new Vector();
        CTreeNode root = ctree.getRoot();
        accessCount = 1;
        depth = 0;

        Hist queryFeature = ctree.factory.toHist(query);
        visitNode(root, query, queryFeature, pseudo_level, cand, usingHist);
        return cand;
    }

    /**
     * Visit nodes recursively.
     * @param mapper GraphMapper
     * @param node CTreeNode
     * @param query Graph
     * @param pseudo_level int
     * @param cand Vector
     */
    private static int[] hits1 = new int[10];
    private static int[] hits2 = new int[10];
    private static int[] counts = new int[10];
    private static int[] cands = new int[10]; //candidates at depth i
    private static int depth = 0;
    private static void visitNode(CTreeNode node,
                                  Graph query, Hist queryHist,
                                  int pseudo_level, Vector<Graph> cand,
            boolean usingHist) {
        Vector entries = node.getEntries();
        int n = entries.size();
        depth++;
        counts[depth] += n;
        for (int i = 0; i < n; i++) {
            if (usingHist && !queryHist.subHist(node.histAt(i))) {
                continue;
            }
            hits1[depth]++;
            if (!node.isLeaf()) {
                Graph g = node.childGraphAt(i);
                if (Util.pseudoSubIsomorphic(query, g, pseudo_level)) {
                    hits2[depth]++;
                    visitNode((CTreeNode) entries.elementAt(i), query,
                              queryHist, pseudo_level, cand, usingHist);
                }
            } else {
                Graph g = (Graph) entries.elementAt(i);
                if (Util.pseudoSubIsomorphic(query, g, pseudo_level)) {
                    hits2[depth]++;
                    cand.addElement(g);
                }
            }
            accessCount++;
        } // end of for i
        cands[depth] = cand.size();
        depth--;
    }
}
