//package jbliss;

import java.util.*;
import fi.tkk.ics.jbliss.Graph;

/**
 * A simple example of how to use jbliss.
 * Generates all the graphs with N vertices up to isomorphism.
 */
public class TestEnumerateROM
{
    protected static class Stats {
	public long nof_graphs;
	public void Stats() {nof_graphs = 0; }
    }

    public static void main(String args[])
    {
        System.out.println(System.getProperty("java.library.path"));
	int N = 7;
	Stats stats = new Stats();
	Graph<String> g = new Graph<String>();
	traverse(g, N, new TreeSet<Graph<Integer>>(), stats);
	System.out.println("There are "+stats.nof_graphs+" graphs with "+
			   N+" vertices");
    }

    protected static void traverse(Graph<String> g,
				   int N,
				   Set<Graph<Integer>> visited,
				   Stats stats)
    {
	assert N <= 30;

	// Get the canonical fom of g
	Graph<Integer> g_canform = g.relabel(g.canonical_labeling());

	// Already visited?
	if(visited.add(g_canform) == false)
	    return;

	if(g.nof_vertices() == N) {
	    // Found a new graph with N vertices
	    //g.write_dot(System.out);
	    stats.nof_graphs++;
	    return;
	}

	// Construct and recursively traverse all the children
	int i = g.nof_vertices();
	String v = "v"+i;
	for(int k = 0; k < Math.pow(2,i); k++) {
	    g.add_vertex(v);
	    int m = k;
	    for(int j = 0; j < i; j++) {
		if((m & 0x01) == 1)
		    g.add_edge(v, "v"+j);
		m = m / 2;
	    }
	    traverse(g, N, visited, stats);
	    g.del_vertex(v);
	}
    }
}
