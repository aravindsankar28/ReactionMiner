package ctree.tool;

import java.util.*;

import ctree.index.*;
import ctree.graph.*;
import ctree.mapper.*;

import ctree.util.*;

import ctree.lgraph.*;
import java.io.PrintWriter;

/**
 *
 * @author Huahai He
 * @version 1.0
 */

public class SimQuery {
    /*
     static private double confidence;
     static private double validity;
     static private double precision;
         static private double confidence_L;
     */

    private static void usage() {
        System.err.println(
                "Usage: ... [options] ctree_file query_file");
        System.err.println("  -knn=INT \t\t K-NN query");
        System.err.println("  -range=DOUBLE \t range query");
        System.err.println(
                "  -nQ=INT \t\t number of queries, default=queries in query_file");
        System.err.println(
                "  -strict=[yes|no] \t strict ranking, default=yes");
        System.err.println(
                "  -output=FILE \t\t if this option is present, then output the answers");
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

        boolean knn;
        int k = 0;
        double range = 0;
        if (opt.hasOpt("knn")) {
            k = opt.getInt("knn");
            knn = true;
        } else if (opt.hasOpt("range")) {
            range = opt.getDouble("range");
            knn = false;
        } else {
            usage();
            return;
        }

        int nQ = opt.getInt("nQ", queries.length);
        boolean strict = opt.getString("strict", "yes").equals("yes");

        String output = opt.getString("output");
        PrintWriter out = null;
        if (output != null) {
            out = new PrintWriter(output);
        }

        // By strict ranking, the similarity between a ctree node and the query
        // is computed by upper bound.

        GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
        GraphSim graphSim = new LGraphSim();
        DataSum stat = new DataSum();

        System.err.println("Query");
        for (int i = 0; i < nQ; i++) {
            long query_time = System.currentTimeMillis();

            Vector<RankerEntry> ans;
            if (knn) {
                ans = kNNQuery(ctree, mapper, graphSim, queries[i], k, strict);
            } else {
                ans = rangeQuery(ctree, mapper, graphSim, queries[i], -range,
                                 strict);
            }
            query_time = System.currentTimeMillis() - query_time;

            // Output answers to this query
            if (output != null) {
                out.println(ans.size());
                for (RankerEntry e : ans) {
                    Graph g = e.getGraph();
                    out.println(((LGraph) g).getId());
                }
            }

            // statistics
            stat.add("query_time", query_time);
            stat.add("ans_size", ans.size());
            stat.add("access_ratio", (double) accessCount / ctree.size());
            //stat.append("confidence", confidence);
            //stat.append("validity", validity);
            //stat.append("precision", precision);
            //stat.append("confidence_L", confidence_L);

            double sim = 0, simUp = 0, rate = 0;
            int size = ans.size();
            for (int j = 0; j < size; j++) {
                RankerEntry entry = (RankerEntry) ans.elementAt(j);
                double temp1, temp2;
                sim += temp1 = -entry.getDist();
                simUp += temp2 = graphSim.simUpper(queries[i], entry.getGraph());
                //ratio += (double) accessCount / ctree.size();
                rate += temp1 / temp2;
            }
            stat.add("sim", sim / size);
            stat.add("simUp", simUp / size);
            stat.add("rate", rate / size); //sim/simUp
            stat.add("norm", graphSim.norm(queries[i]));

            if ((i + 1) % 10 == 0) {
                System.err.println("Query at " + (i + 1));
            }
        } // for queries

        if (output != null) {
            out.close();
        }

        //format: query_time(ms) ans_size access_ratio confidence validity precision confidence_L
        System.err.println("format: query_time(ms) ans_size access_ratio");
        System.out.printf("%f %f %f\n",
                          stat.mean("query_time"), stat.mean("ans_size"),
                          stat.mean("access_ratio"));

        /*
         double[][] M = stat.report("norm", "sim", "simUp", "access_ratio", "rate");
             for (double[] row : M) {
         System.err.printf("%d %f %f %f %f\n", (int) row[0], row[1], row[2], row[3],
                            row[4]);
             }*/

        /*
         // Report by merging rows with identical graph norms
         double[][] H = stat.reportOnKey("norm", "sim", "simUp", "access_ratio",
                                        "rate");
             int cols = H[0].length;
             for (double[] row : H) {
          if (row[cols - 1] == 0) {
            continue;
          }
          // format: norm sim simUp access_ratio rate count
         System.out.printf("%d %f %f %f %f %d\n", (int) row[0], row[1], row[2],
                            row[3], row[4], (int) row[5]);
             }*/
    }

    private static int accessCount;

    /**
     * Query using NNRanker
     * @param ctree CTree
     * @param mapper GraphMapper
     * @param graphSim GraphSim
     * @param query Graph
     * @param k int
     * @param preciseRanking boolean
     * @return Vector
     */
    public static Vector<RankerEntry> kNNQuery(CTree ctree, GraphMapper mapper,
                                               GraphSim graphSim,
                                               Graph query, int k,
                                               boolean strictRanking) {
        /*SimRanker ranker = new SimRanker(ctree, mapper, query, preciseRanking);
             RankerEntry entry;
             Vector ans = new Vector(k); // answer set
             while ( (entry = ranker.nextNN()) != null && ans.size() < k) {
          ans.addElement(entry);
             }
             accessCount = ranker.getAccessCount();
             ranker.clear();
             return ans;
         */

        SimRanker ranker = new SimRanker(ctree, mapper, graphSim, query,
                                         strictRanking);
        Vector<RankerEntry> ans = ranker.optimizedKNNQuery(k);
        accessCount = ranker.getAccessCount();

        /*
                 // compute confidence, validity and precision
                 double simAtK = -ans.elementAt(k - 1).getDist();
                 Vector<RankerEntry> ansUp = ranker.upperRangeQuery(simAtK);

                 if (ansUp.size() <= k) {
            confidence = 1;
            validity = 1;
            precision = 1;
            confidence_L = k - 1;
                 } else {
            confidence = (double) (k - 1) / (ansUp.size() - 1); // omit the answer which is the query
            double simUpAtK = -ansUp.elementAt(k - 1).getDist();
            int j;
            for (j = 0; j < k; j++) {
                if ( -ans.elementAt(j).getDist() < simUpAtK) {
                    break;
                }
            }
            validity = (double) (j - 1) / (k - 1);
            precision = simAtK / simUpAtK;
            confidence_L = ansUp.size() - 1;
                 }*/
        return ans;
    }

    /**
     * Range query
     * @param ctree CTree
     * @param mapper GraphMapper
     * @param graphSim GraphSim
     * @param query Graph
     * @param range double
     * @param preciseRanking boolean
     * @return Vector
     */
    public static Vector<RankerEntry> rangeQuery(CTree ctree,
                                                 GraphMapper mapper,
                                                 GraphSim graphSim,
                                                 Graph query, double range,
                                                 boolean preciseRanking) {
        SimRanker ranker = new SimRanker(ctree, mapper, graphSim, query,
                                         preciseRanking);
        RankerEntry entry;
        Vector<RankerEntry> ans = new Vector(); // answer set
        while ((entry = ranker.nextNN()) != null && entry.getDist() <= range) {
            ans.addElement(entry);
        }
        accessCount = ranker.getAccessCount();
        ranker.clear();
        return ans;
    }

}
