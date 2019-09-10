package graph;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import globals.Globals;
import ctree.graph.Vertex;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFile;
import ctree.lgraph.LVertex;
import ctree.lgraph.UnlabeledEdge;
import grank.graph.GraphFile;
import grank.graph.LEdge;
import grank.graph.LabelMap;

public class GraphVisualize {

	/**
	 * @param args
	 * @throws IOException
	 * Helper function to create a dot file given a graph as input. Takes 2 command line parameters as input.
	 * createDotFilePath is the main interface function to use.
	 */
	void createDotFile(String mol, String outputFile) throws IOException {
		LGraph graph = LGraphFile.loadLGraphs(Globals.molDirectory + "/" + mol
				+ ".mol")[0];
		createDotFile(graph, outputFile);
	}
	void createDotFilePath(String path, String outputFile) throws IOException
	{
		LGraph graph = LGraphFile.loadLGraphs(path)[0];
		createDotFile(graph, outputFile);
	}
	
	// Helper function to remove hydrogen atoms from the labels of Ch3 etc. This will be 
	public static LGraph removeHatoms(LGraph g)
	{
		LVertex[] vertices = new LVertex[g.numV()];
		int i = 0;
		for (Vertex v : g.V()) {
			LVertex vprime  = new LVertex(v.toString());
			String vlabel = v.toString();
			
			if(vlabel.length() > 1 && (vlabel.contains("H") || vlabel.contains("h")) &&  vlabel.length() < 4)
				vprime = new LVertex(vlabel.substring(0,1));			
			vertices[i] = vprime;
			i++;
		}	
		UnlabeledEdge[] edges = (UnlabeledEdge[]) g.E();		
		return new LGraph(vertices, edges,g.getId());		
	}
	 
	public void createDotFile(String g1, String g2, String outputFile2,
			ArrayList<Integer> mapping) throws IOException {
		LGraph g1s = LGraphFile.loadLGraphs(Globals.molDirectory + "/" + g1
				+ ".mol")[0];
		LGraph g2s = LGraphFile.loadLGraphs(Globals.molDirectory + "/" + g2
				+ ".mol")[0];
		createDotFile(g1s, g2s, outputFile2, mapping);
	}

	void graphToDot(grank.graph.LGraph g, LabelMap map, String outFile,
			ArrayList<Integer> nodesToColour) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(outFile);
		out.println("graph \"" + g.id + "\" {");
		for (int i = 0; i < g.V.length; i++) {
			if (nodesToColour.contains(i)) {
				out.printf(
						"%d [fillcolor = yellow style = filled label=%s];\n",
						i, map.vlab[g.V[i]]);
			} else
				out.printf("%d [label=%s];\n", i, map.vlab[g.V[i]]);
		}
		for (int i = 0; i < g.E.length; i++) {
			LEdge e = (LEdge) g.E[i];
			out.printf("%d -- %d [label=%s];\n", e.v1, e.v2, map.elab[e.label]);
		}
		out.println("}\n");
		out.close();
	}

	void graphToDot(grank.graph.LGraph g, LabelMap map, String outFile)
			throws FileNotFoundException {
		PrintWriter out = new PrintWriter(outFile);
		out.println("graph \"" + g.id + "\" {");
		for (int i = 0; i < g.V.length; i++) {
			out.printf("%d [label=%s];\n", i, map.vlab[g.V[i]]);
		}

		for (int i = 0; i < g.E.length; i++) {
			LEdge e = (LEdge) g.E[i];
			out.printf("%d -- %d [label=%s];\n", e.v1, e.v2, map.elab[e.label]);
		}
		out.println("}\n");
		out.close();
	}

	public void createDotFile(LGraph g1, LGraph g2, String outputFile2,
			ArrayList<Integer> mapping) throws FileNotFoundException,
			IOException {
		// This mapping assumes g1 sub of g2.
		FileWriter fw = new FileWriter("map");
		Vertex[] vlabels = g2.V();
		fw.write(vlabels.length + "\n");
		int j = 0;
		for (Vertex vertex : vlabels) {
			fw.write(j + " " + vertex.toString() + "\n");
			j++;
		}
		fw.write(3 + "\n");
		fw.write(0 + " " + 1 + "\n");
		fw.write(1 + " " + 2 + "\n");
		fw.write(2 + " " + 3 + "\n");
		fw.close();
		LabelMap map2 = new LabelMap("map");
		LGraph[] graphs = new LGraph[1];
		graphs[0] = g2;
		LGraphFile.saveLGraphs(graphs, "g");
		graphToDot(GraphFile.loadGraphs("g", map2)[0], map2, outputFile2,
				mapping);
	}

	public void createDotFile(LGraph x, String outputFile) throws IOException {			
		FileWriter fw = new FileWriter("map");
		Vertex[] vlabels = x.V();
		fw.write(vlabels.length + "\n");
		int j = 0;
		for (Vertex vertex : vlabels) {
			fw.write(j + " " + vertex.toString() + "\n");
			j++;
		}
		fw.write(3 + "\n");
		fw.write(0 + " " + 1 + "\n");
		fw.write(1 + " " + 2 + "\n");
		fw.write(2 + " " + 3 + "\n");
		fw.close();
		LabelMap map = new LabelMap("map");
		LGraph[] graphs = new LGraph[1];
		graphs[0] = x;
		LGraphFile.saveLGraphs(graphs, "g");
		graphToDot(GraphFile.loadGraphs("g", map)[0], map, outputFile);
	}

	public static void main(String[] args) throws IOException {
		if(args.length <2)
			{
				System.err.println("Usage - [Graph file] [Output dot file]");
				System.exit(0);
			}
		String file1 = args[0];
		System.out.println(file1);
		String file2 = args[1];
		GraphVisualize gv = new GraphVisualize();
		gv.createDotFilePath(file1, file2);
	}
}
