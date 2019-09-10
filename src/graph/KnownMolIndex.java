package graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import globals.Globals;

import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFile;

public class KnownMolIndex {

	HashMap<String, String> index; 
	HashMap<String, LGraph> knownMolecules;

	KnownMolIndex() {
		index = new HashMap<String, String>();
		knownMolecules = new HashMap<String, LGraph>();
	}

	public ArrayList<String> readMolFileNames(String molDirectory) {
		ArrayList<String> molFileNames = new ArrayList<String>();
		File folder = new File(molDirectory);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			molFileNames.add(listOfFiles[i].getName().split("\\.")[0]);
		}
		return molFileNames;
	}

	void loadMolecules() throws IOException {

		ArrayList<String> fileNames = readMolFileNames(Globals.molDirectory);

		ConvertEdgeLabels cel = new ConvertEdgeLabels();
		for (String name : fileNames) {
			LGraph[] g = LGraphFile.loadLGraphs(Globals.molDirectory + "/"
					+ name + ".mol");
			if (Globals.INCLUDE_EDGE_LABELS)
				g[0] = cel.addEdgeLabelNodes(g[0]);

			knownMolecules.put(name, g[0]);
		}
		System.out.println("Mols loaded");
	}

	HashMap<String, String> createHashIndex() throws IOException {
		GraphLabelling giso = new GraphLabelling();
		
		for (String mol : knownMolecules.keySet()) {
			LGraph g = knownMolecules.get(mol);
			g.setId(mol);
			String label = giso.getCanonicalLabel(g);
			System.out.println(g.getId());
			//System.out.println(label);
			index.put(label, mol);
		}
		return index;

	}

	public static void main(String[] args) throws FileNotFoundException,
			ClassNotFoundException, IOException {

		KnownMolIndex kmi = new KnownMolIndex();
		kmi.loadMolecules();
		kmi.createHashIndex();
		
		FileOutputStream fileOut = new FileOutputStream("known_mol_labels.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(kmi.index);
		out.close();
		fileOut.close();

		
		System.out.println("Index created");
	}
}
