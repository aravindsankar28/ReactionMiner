package ctree.mapper;

import java.util.*;
import ctree.graph.*;
import ctree.index.*;
import ctree.alg.*;

/**
 * Neighbor Biased Mapping (NBM)
 *
 * @author Huahai He
 * @version 1.0
 */

public class NeighborBiasedMapper implements GraphMapper {

    private WeightMatrix wmatrix;
    private double bonus;

    protected NeighborBiasedMapper() {
    }

    public NeighborBiasedMapper(WeightMatrix _wmatrix) {
        this(_wmatrix, 10);
    }

    public NeighborBiasedMapper(WeightMatrix _wmatrix, double _bonus) {
        wmatrix = _wmatrix;
        bonus = _bonus;
    }

    public int[] map(Graph g1, Graph g2) {

        // Computer the initial weight matrix W
        double[][] W = wmatrix.weightMatrix(g1, g2);

        // Prepare other data
        int n1 = g1.numV();
        int n2 = g2.numV();
        int[][] alist1 = g1.adjList();
        int[][] alist2 = g2.adjList();
        int[][] bilist = Util.getBipartiteList(g1, g2);

        int[] map = NeighborBiasedMapping.mapGraphs(n1, n2, W, alist1, alist2,
                bilist, bonus);
        return map;
    }

}
