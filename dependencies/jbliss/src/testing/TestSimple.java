//package jbliss;
package testing;

import java.util.*;
import fi.tkk.ics.jbliss.Graph;
import fi.tkk.ics.jbliss.DefaultReporter;
import fi.tkk.ics.jbliss.Utils;

/**
 * A simple example of how to use jbliss.
 * Generates a small graph and finds its automorphisms and canonical form.
 */
public class TestSimple
{
    public static void main(String args[])
    {
	DefaultReporter reporter = new DefaultReporter();
	/* Create the graph */
	/*Graph<String> g = new Graph<String>();
	g.add_vertex("v1");
	g.add_vertex("v2");
	g.add_vertex("v3");
	g.add_vertex("v4");
	g.add_edge("v1","v2");
	g.add_edge("v2","v3");
	g.add_edge("v3","v1");
	g.add_edge("v2","v4");
	*/
	
	Graph<Integer> g = new Graph<Integer>();
	
	g.add_vertex(1,1);
	g.add_vertex(0,1);
	g.add_vertex(2,1);
	g.add_vertex(3,1);
	
	g.add_edge(0, 1);
	g.add_edge(1, 2);
	g.add_edge(2, 0);
	g.add_edge(1, 3);
	
	//g.write_dot(System.out);
	
	
	
	/* Print the graph */
	System.out.println("The graph is:");
	g.write_dot(System.out);
	/* Find (a generating set for) the automorphism group of the graph */
	g.find_automorphisms(reporter, null);
	/* Compute the canonical labeling */
	Map<Integer,Integer> canlab = g.canonical_labeling();
	/* Print the canonical labeling */
	System.out.print("A canonical labeling for the graph is: ");
	Utils.print_labeling(System.out, canlab);
	System.out.println("");
	/* Compute the canonical form */
	Graph<Integer> g_canform = g.relabel(canlab);
	/* Print the canonical form of the graph */
	System.out.println("The canonical form of the graph is:");
	g_canform.write_dot(System.out);
    }
}
