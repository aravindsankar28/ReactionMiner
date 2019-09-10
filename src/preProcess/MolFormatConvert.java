package preProcess;

import java.io.*;
import java.util.*;

import ctree.graph.Vertex;
import ctree.lgraph.*;
import graph.SubgraphMapping;
import tool.FileUtils;

import joelib2.io.*;
import joelib2.molecule.*;
import joelib2.util.iterator.*;

// TODO: Add reverse format change - remove H atom and remove functional group. 
public class MolFormatConvert {
	/**
	 * TODO : Add a helper class to add a new molecule to the database given
	 * just the molecule file so that it can be used directly for pathway
	 * prediction.
	 */
	/**
	 * @param args
	 * @throws IOException
	 * @throws MoleculeIOException
	 */
	static boolean addHAtom = true;
	static boolean replacePhosphate = true;
	static boolean replaceCoA = true;

	LGraph phosphateMol;
	LGraph CoA;

	StringBuffer parseMolecule(String filePath) throws IOException {
		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SDF");
		Molecule mol = new BasicConformerMolecule(inType, inType);
		BasicReader reader = new BasicReader(filePath);
		int numMol = 0;
		Vector<Integer> failed = new Vector<Integer>();
		StringBuffer s = new StringBuffer("");
		boolean more = true;
		while (more) {
			try {
				if (reader.readNext(mol)) {
					numMol++;
					HashSet<Integer> hatoms = new HashSet<Integer>();
					int numAtoms = 0;
					int numEdges = 0;
					StringBuffer atomsRep = new StringBuffer("");
					StringBuffer edgeRep = new StringBuffer("");
					AtomIterator at = mol.atomIterator();
					BondIterator bt = mol.bondIterator();
					while (at.hasNext()) {
						Atom a = at.nextAtom();
						numAtoms++;
						atomsRep.append(a.toString() + "\n");
					}

					while (bt.hasNext()) {
						Bond b = bt.nextBond();
						int bi = b.getBeginIndex() - 1;
						int ei = b.getEndIndex() - 1;
						int bo = b.getBondOrder();
						if (b.isWedge())
							edgeRep.append(ei + " " + bi + " " + bo + " " + "WD" + "\n");
						else if (b.isHash())
							edgeRep.append(ei + " " + bi + " " + bo + " " + "HS" + "\n");
						else
							edgeRep.append(ei + " " + bi + " " + bo + " " + "0" + "\n");
						numEdges++;
					}
					s.append("#" + mol.getTitle() + "\n" + (numAtoms - hatoms.size()) + "\n" + atomsRep.toString()
							+ numEdges + "\n" + edgeRep.toString() + "\n");
				} else
					more = false;

			} catch (Exception e) {
				failed.add(numMol);
			}
		}
		return s;
	}

	// Helper function to convert the format for a single query compound.
	void convertMolFormatQuery(String queryName, String queryPath, String outputDir) throws IOException {
		String outputFilePath = outputDir + queryName;
		String filePath = queryPath;
		StringBuffer s = parseMolecule(filePath);
		FileUtils.writeToFile("temp.txt", s.toString());
		LGraph g = LGraphFile.loadLGraphs("temp.txt")[0];
		if (addHAtom)
			g = addHAtoms(g);
		if (replacePhosphate)
			g = replaceFunctionalGroups(phosphateMol, g, "Phosphate", 0);
		if (replaceCoA)
			g = replaceFunctionalGroups(CoA, g, "CoA", 42);
		FileUtils.writeToFile(outputFilePath.replace(".sdf", ".txt"), g.toString());
	}

	public static LGraph removeHatoms(LGraph g) {
		LVertex[] vertices = new LVertex[g.numV()];
		int i = 0;
		for (Vertex v : g.V()) {
			LVertex vprime = new LVertex(v.toString());
			String vlabel = v.toString();

			if (vlabel.length() > 1 && (vlabel.contains("H") || vlabel.contains("h")) && vlabel.length() < 4)
				vprime = new LVertex(vlabel.substring(0, 1));
			vertices[i] = vprime;
			i++;
		}
		UnlabeledEdge[] edges = (UnlabeledEdge[]) g.E();
		return new LGraph(vertices, edges, g.getId());
	}

	// Given the input directory of molecules in the raw format, this function
	// converts into the format needed by the code.

	void convertMolFormatDirectory(String inputDir, String outputDir) throws IOException {
		File dir = new File(inputDir);
		String[] molFileNames = dir.list();
		for (String fileName : molFileNames) {
			System.out.println("hello " + fileName);
			String outputFilePath = outputDir + fileName;
			String filePath = inputDir + fileName;
			if (fileName.startsWith("."))
				continue;
			StringBuffer s = parseMolecule(filePath);
			System.out.println("hi " + s.length());
			if (s.length() == 0)
				continue;
			FileUtils.writeToFile("temp.txt", s.toString());
			LGraph g = LGraphFile.loadLGraphs("temp.txt")[0];
			if (addHAtom)
				g = addHAtoms(g);
			// System.out.println(g);
			if (replacePhosphate)
				g = replaceFunctionalGroups(phosphateMol, g, "Phosphate", 0);
			if (replaceCoA)
				g = replaceFunctionalGroups(CoA, g, "CoA", 42);
			FileUtils.writeToFile(outputFilePath.replace(".sdf", ".txt"), g.toString());
		}
	}

	LGraph addHAtoms(LGraph g) {
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
		int[] nodeDegrees = new int[g.numV()];
		for (int e = 0; e < g.numE(); e++) {
			int v1 = g.E()[e].v1();
			int v2 = g.E()[e].v2();
			int w = g.E()[e].w();
			if (!g.V()[v1].toString().contentEquals("H") && !g.V()[v2].toString().contentEquals("H")) {
				nodeDegrees[v1] = nodeDegrees[v1] + w;
				nodeDegrees[v2] = nodeDegrees[v2] + w;
			}
		}
		boolean[] isValidVertices = new boolean[g.numV()];
		for (int v = 0; v < g.numV(); v++)
			isValidVertices[v] = true;

		for (int n = 0; n < g.numV(); n++) {
			String nodeLabel = g.V()[n].toString();
			int nodeDegree = nodeDegrees[n];
			if (nodeLabel.contentEquals("C") && nodeDegree < 4) {
				if (nodeDegree == 3)
					verticesList.add(new LVertex("CH"));
				else
					verticesList.add(new LVertex("CH" + (4 - nodeDegree)));
			} else if (nodeLabel.contentEquals("N") && nodeDegree < 3) {
				if (nodeDegree == 2)
					verticesList.add(new LVertex("NH"));
				else
					verticesList.add(new LVertex("NH" + (3 - nodeDegree)));
			} else if (nodeLabel.contentEquals("O") && nodeDegree == 1)
				verticesList.add(new LVertex("OH"));
			else if (nodeLabel.contentEquals("H") == false) 
				verticesList.add(new LVertex(nodeLabel));
			else
			{
				int[] neighbors = g.adjList()[n];
				assert (neighbors.length ==1);
				String label = g.V()[neighbors[0]].toString();
				if (label.contentEquals("O") || label.contentEquals("N") || label.contentEquals("C"))
					isValidVertices[n] = false;
				else
					verticesList.add(new LVertex(nodeLabel));
			}
		}
		
		int nodeRemoveCount[] = new int[isValidVertices.length];

		for (int i = 0; i < g.numV(); i++) {
			if (!isValidVertices[i] && i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1] + 1;
			else if (!isValidVertices[i] && i == 0)
				nodeRemoveCount[i] = 1;
			else if (i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1];
			else
				nodeRemoveCount[i] = 0;
		}
		
		boolean[] isValidEdges = new boolean[g.numE()];
		// Create valid edges here.
		for (int e = 0; e < g.numE(); e++) {
			isValidEdges[e] = true;
			int v1 = g.E()[e].v1();
			int v2 = g.E()[e].v2();
			if(isValidVertices[v1] && isValidVertices[v2])
				isValidEdges[e] = true;
			else
				isValidEdges[e] = false;
		}
		
		ArrayList<UnlabeledEdge> edgesList = new ArrayList<UnlabeledEdge>();
		for (int i = 0; i < g.numE(); i++) {
			if (isValidEdges[i]) {
				int v1 = g.E()[i].v1();
				int v2 = g.E()[i].v2();
				UnlabeledEdge edge = new UnlabeledEdge(v1 - nodeRemoveCount[v1], v2 - nodeRemoveCount[v2],
						g.E()[i].w(), g.E()[i].stereo(), false);
				edgesList.add(edge);
			}
		}
		
		LVertex[] vertices = verticesList.toArray(new LVertex[verticesList.size()]);
		UnlabeledEdge[] edges = edgesList.toArray(new UnlabeledEdge[edgesList.size()]);
		LGraph new_g = new LGraph(vertices, edges, "");
		return new_g;
	}

	// "Phosphate", "CoA"
	LGraph replaceFunctionalGroups(LGraph functionalGroup, LGraph g, String newName, int terminalNode) {
		SubgraphMapping sgm = new SubgraphMapping();
		ArrayList<Integer> mapping = sgm.getMapping(functionalGroup, g);
		if (mapping == null)
			return g;
		else {
			boolean[] isValidVertices = new boolean[g.numV()];
			for (int v = 0; v < g.numV(); v++)
				isValidVertices[v] = true;

			for (int i = 0; i < mapping.size(); i++)
				if (i != terminalNode)
					isValidVertices[mapping.get(i)] = false;

			ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
			int nodeRemoveCount[] = new int[isValidVertices.length];

			for (int i = 0; i < g.numV(); i++) {
				if (!isValidVertices[i] && i != 0)
					nodeRemoveCount[i] = nodeRemoveCount[i - 1] + 1;
				else if (!isValidVertices[i] && i == 0)
					nodeRemoveCount[i] = 1;
				else if (i != 0)
					nodeRemoveCount[i] = nodeRemoveCount[i - 1];
				else
					nodeRemoveCount[i] = 0;
			}

			for (int i = 0; i < g.numV(); i++) {
				if (i == mapping.get(terminalNode))
					verticesList.add(new LVertex(newName));
				else if (isValidVertices[i])
					verticesList.add((LVertex) g.V()[i]);
			}

			boolean[] isValidEdges = new boolean[g.numE()];
			// Create valid edges here.
			for (int e = 0; e < g.numE(); e++) {
				isValidEdges[e] = true;
				int v1 = g.E()[e].v1();
				int v2 = g.E()[e].v2();
				if (mapping.contains(v1) && mapping.contains(v2))
					isValidEdges[e] = false;
			}
			ArrayList<UnlabeledEdge> edgesList = new ArrayList<UnlabeledEdge>();
			for (int i = 0; i < g.numE(); i++) {
				if (isValidEdges[i]) {
					int v1 = g.E()[i].v1();
					int v2 = g.E()[i].v2();
					UnlabeledEdge edge = new UnlabeledEdge(v1 - nodeRemoveCount[v1], v2 - nodeRemoveCount[v2],
							g.E()[i].w(), g.E()[i].stereo(), false);
					edgesList.add(edge);
				}
			}
			LVertex[] vertices = verticesList.toArray(new LVertex[verticesList.size()]);
			UnlabeledEdge[] edges = edgesList.toArray(new UnlabeledEdge[edgesList.size()]);
			LGraph new_g = new LGraph(vertices, edges, "");
			return new_g;
		}
	}

	// Removes stereo edge label nodes. Reverse of what we usually do.
	public static LGraph removeStereoNodes(LGraph g) {
		ArrayList<UnlabeledEdge> edgeList = new ArrayList<UnlabeledEdge>();
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
		int[] nodeCounter = new int[g.numV()];
		int i = 0;
		for (LVertex vertex : (LVertex[]) g.V()) {
			// For now check only 2 and 3. Later can be generalized to a number.
			if (vertex.toString().contentEquals("WD") || vertex.toString().contentEquals("HS")) {
				if (i >= 1)
					nodeCounter[i] = nodeCounter[i - 1] + 1;
				else
					nodeCounter[i] = 1;
			} else {
				verticesList.add(vertex);
				if (i >= 1)
					nodeCounter[i] = nodeCounter[i - 1];
				else
					nodeCounter[i] = 0;
			}
			i++;
		}

		boolean[] processedEdges = new boolean[g.numE()];
		for (i = 0; i < g.numE(); i++) {

			if (processedEdges[i])
				continue;

			UnlabeledEdge e_i = (UnlabeledEdge) g.E()[i];

			if (g.V()[e_i.v2()].toString().contentEquals("WD") || g.V()[e_i.v2()].toString().contentEquals("HS")
					|| g.V()[e_i.v1()].toString().contentEquals("WD")
					|| g.V()[e_i.v1()].toString().contentEquals("HS")) {

				int dummy;
				int v1;
				if (g.V()[e_i.v2()].toString().contentEquals("WD") || g.V()[e_i.v2()].toString().contentEquals("HS")) {
					dummy = e_i.v2();
					v1 = e_i.v1();
				} else {
					dummy = e_i.v1();
					v1 = e_i.v2();
				}

				int v2 = -1;
				for (int j = 0; j < g.numE(); j++) {
					UnlabeledEdge e_j = (UnlabeledEdge) g.E()[j];
					if (e_j.v1() == dummy && j != i) {
						v2 = e_j.v2();
						processedEdges[j] = true;
						break;
					}
					if (e_j.v2() == dummy && j != i) {
						v2 = e_j.v1();
						processedEdges[j] = true;
						break;
					}
				}
				String stereo = g.V()[dummy].toString();
				edgeList.add(new UnlabeledEdge(v1 - nodeCounter[v1], v2 - nodeCounter[v2], 1, stereo, false));
				processedEdges[i] = true;

				continue;
			}
			edgeList.add(new UnlabeledEdge(e_i.v1() - nodeCounter[e_i.v1()], e_i.v2() - nodeCounter[e_i.v2()], e_i.w(),
					"0", false));
		}

		LVertex[] vertices_arr = verticesList.toArray(new LVertex[verticesList.size()]);

		UnlabeledEdge[] edges_arr = edgeList.toArray(new UnlabeledEdge[edgeList.size()]);
		LGraph new_g = new LGraph(vertices_arr, edges_arr, "");
		return new_g;
	}

	public static void main(String[] args) throws IOException, MoleculeIOException {
		MolFormatConvert mfc = new MolFormatConvert();
		mfc.phosphateMol = LGraphFile.loadLGraphs("data/FunctionalGroupMols/Phosphate.mol")[0];
		mfc.CoA = LGraphFile.loadLGraphs("data/FunctionalGroupMols/CoA.mol")[0];
		mfc.convertMolFormatDirectory("/Users/aravind/Desktop/tmp/", "/Users/aravind/Desktop/tmp_new/");
		// mfc.convertMolFormatDirectory("/Users/aravind/Final_Project/ddp/mols_all_details/",
		// "/Users/aravind/Desktop/tmp/");
	}

}
