package ctree.experiment;

import java.io.*;
import java.util.*;

import ctree.graph.*;
import ctree.mapper.*;
import ctree.lgraph.*;

import ctree.index.*;
import ctree.util.*;

/**
 * <p>Title: Closure Tree</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Huahai He
 * @version 1.0
 */
public class GraphGenerator {

    private static Random rand=null;

    public static void main(String[] args) throws IOException {
        Opt opt = new Opt(args);
        if (opt.args() < 1) {
            System.err.println("Usage: ... [options] graph_file");
            System.err.println("  -nG=INT \t number of graphs, default=10000");
            System.err.println("  -zG=INT \t size of a graph (number of vertices), default=50");
            System.err.println("  -nS=INT \t number of seeds, default=100");
            System.err.println("  -zS=INT \t size of a seed, default=10");
            System.err.println("  -nL=INT \t number of labels, default=10");
            System.err.println("  -seed=INT \t random seed, default=1");
            System.exit(1);
        }
        int nG=opt.getInt("nG",10000);
        int zG = opt.getInt("zG", 50);
        int nS=opt.getInt("nS",100);
        int zS=opt.getInt("zS",10);
        int nL=opt.getInt("nL", 10);
        int seed=opt.getInt("seed", 1);
        rand = new Random(seed);

            LGraph[] graphs = generateGraphs(nG, zG,nS,zS,nL);
            GraphsInfo.statGraphs(graphs);
            System.err.println("Save graphs");
            LGraphFile.saveLGraphs(graphs, opt.getArg(0));
            System.err.println("OK");
    }

    private static BipartiteMapper mapper = new BipartiteMapper();

    public static LGraph[] generateGraphs(int numGraphs, int sizeGraph,
                                          int numSeeds, int sizeSeed,
                                          int numLabels) throws IOException {
        // generate labels
        String[] labels = generateLabels(numLabels);

        // generate seeds
        LGraph[] seeds = generateSeeds(numSeeds, sizeSeed, true, labels);
        //String seed_file = "s" + numSeeds + "i" + sizeSeed + "l" + numLabels + ".txt";
        //saveGraphs(seeds, seed_file);

        // generate graphs
        LGraph[] graphs = new LGraph[numGraphs];
        for (int i = 0; i < graphs.length; i++) {
            int numEdges; // size of the graph in terms of number of edges
            do {
                numEdges = Util.nextPoisson(sizeGraph, rand);
            } while (numEdges == 0);

            graphs[i] = generateGraph(numEdges, seeds);
            graphs[i].setId("G" + i);
        }

        return graphs;
    }

    public static String[] generateLabels(int numLabels) {
        String[] labels = new String[numLabels];
        for (int i = 0; i < labels.length; i++) {
            int l = i;
            String s = "";
            do {
                s = (char) (l % 26 + 'A') + s;
                l /= 26;
            } while (l > 0);
            labels[i] = s;

        }
        return labels;
    }

    /**
     * Generate seeds
     * @param numSeeds number of seeds
     * @param sizeSub average size of seeds in terms of edges
     * @param labels String[]
     * @return Graph[]
     */
    public static LGraph[] generateSeeds(int numSeeds, int sizeSeeds,
                                         boolean randomSize,
                                         String[] labels) {
        LGraph[] seeds = new LGraph[numSeeds];
        for (int i = 0; i < seeds.length; i++) {
            int numEdges;
            if (randomSize == true) {
                do {
                    numEdges = Util.nextPoisson(sizeSeeds, rand);
                } while (numEdges == 0);
            } else {
                numEdges = sizeSeeds;
            }
            // |V|(|V|-1)/2 >= |E| >= |V|-1
            int maxV = numEdges + 1;
            int minV = (int) ((Math.sqrt(1 + 8 * numEdges) + 1) / 2) + 1;
            if (minV > maxV) {
                minV = maxV;
            }
            int numVertices = minV + rand.nextInt(maxV - minV + 1);
            seeds[i] = generateSeed(numVertices, numEdges, labels);
            seeds[i].setId("seed" + i);
        }
        return seeds;
    }

    /**
     * Generate a seed
     * @param numVertices int
     * @param numEdges int
     * @param labels String[]
     * @return Graph
     */
    public static LGraph generateSeed(int numVertices, int numEdges,
                                      String[] labels) {
        // generate vertices
        LVertex[] vertices = new LVertex[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new LVertex(labels[rand.nextInt(labels.length)]);
        }

        // generate edges

        UnlabeledEdge[] edges = new UnlabeledEdge[numEdges];
        int[][] adj = new int[numVertices][numVertices];
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                adj[i][j] = 0;
            }
        }
        // generate a tree
        for (int i = 0; i < numVertices - 1; i++) {
            int v1 = rand.nextInt(i + 1);
            edges[i] = new UnlabeledEdge(v1, i + 1, false);
            adj[v1][i + 1] = adj[i + 1][v1] = 1;
        }

        // generate rest edges
        for (int i = numVertices - 1; i < numEdges; i++) {
            int v1, v2;
            do {
                v1 = rand.nextInt(numVertices);
                v2 = rand.nextInt(numVertices);
            } while (v1 == v2 || adj[v1][v2] != 0);
            edges[i] = new UnlabeledEdge(v1, v2, false);
            adj[v1][v2] = adj[v2][v1] = 1;
        }
        LGraph g = new LGraph(vertices, edges, null);
        return g;
    }

    public static LGraph generateGraph(int numEdges, LGraph[] seeds) {
        LGraph g = seeds[rand.nextInt(seeds.length)];
        while (g.numE() < numEdges) {
            LGraph g2 = seeds[rand.nextInt(seeds.length)];
            int[] map = mapper.map(g2, g);
            g = LGraphFactory.mergeGraphs(g2, g, map);
        }

        // remove some edges such that |E|==numEdges
        UnlabeledEdge[] E1 = new UnlabeledEdge[numEdges];
        System.arraycopy(g.E(), 0, E1, 0, numEdges);
        LGraph g1 = new LGraph((LVertex[]) g.V(), E1, g.getId());
        return g1;
    }


}
