package globals;

import graph.ConvertEdgeLabels;
import graph.GraphLabelling;
import graph.GraphDistance;

import java.io.*;
import java.util.*;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFile;
import globals.Globals;
import ruleMining.RPM.RPM;
import ruleMining.RPM.RPAIR;
import ruleMining.ClusteringRules;
import ruleMining.ReactionRule.*;

public class Index {

	/**
	 * Stores our Offline Index that has details of rules etc.
	 */
	public static ArrayList<ReactionRule> rules;
	public static HashMap<Integer, ReactionRule> uniqueRuleMap;
	// Set of unique rules indexed by id.
	public static HashMap<Integer, ReactionRule> allRuleMap;
	// Set of all rules indexed by id.
	public static HashMap<Integer, ArrayList<Integer>> clusters;
	// Indexed by a particular rule and stores all rules in that cluster.
	public static HashMap<Integer, Integer> reverseClusterMap;
	// Map from all rules to the chosen rule in a cluster.
	public static HashMap<LGraph, ArrayList<Integer>> uniqueSignaturesRuleMap;
	public static HashMap<String, LGraph> signatureCanonicalLabels;
	public static HashMap<Integer, RPM> finalPairs;
	public static HashMap<String, LGraph> compounds;
	public static boolean isLoaded = false;
	public static boolean isMolsLoaded = false;
	public static HashMap<String, String> knownMolLabels;
	// canonical label to mol name

	public static HashMap<String, LGraph> knownMolecules; // mol name to graph

	public static HashMap<String, LGraph> knownMoleculesOld;
	// mol name to graph without edge label nodes

	public static HashMap<String, ArrayList<String>> knownMolIsomorphicMols;
	// canonical label to list of mols.
	public static HashMap<String, String> knownMolCanonicalLabel;
	// mol to canonical label
	public static HashMap<String, String> knownMolNames;
	
	public static HashMap<LGraph, String> signatureCanonicalLabelsReverse;
	
	public static int support = 1;

	public static void loadAll() throws IOException, ClassNotFoundException {
		double start = System.currentTimeMillis();
		loadMolecules();
		saveknownMolLabels();
		loadKnownMolLabels();
		loadFinalPairs();
		loadRules();
		saveUniqeSignatures();
		loadUniqeSignatures();
		saveSignatureCanonicalLabels();
		loadSignatureCanonicalLabels();
		
		System.out.println("Loading time = " + (System.currentTimeMillis() - start) / 1000.0);
		isLoaded = true;
		isMolsLoaded = true;
		// removeBigDiffPairs();
		removeATPEtc();
		// Index.knownMolecules.remove("C00033");
		// Index.knownMolLabels.remove(Index.knownMolCanonicalLabel.get("C00033"));
		// Index.knownMolCanonicalLabel.remove("C00033");
	}

	@SuppressWarnings("unchecked")
	public static void loadUniqeSignatures() throws ClassNotFoundException,
			IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				Globals.indexDirectory + "unique_signs_map.ser"));
		uniqueSignaturesRuleMap = (HashMap<LGraph, ArrayList<Integer>>) in
				.readObject();
		in.close();
		System.out.println("No. of unique signs. "
				+ uniqueSignaturesRuleMap.size());
		System.out.println("Unique Signatures loaded");
	}

	static void saveUniqeSignatures() throws ClassNotFoundException,
			IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				Globals.indexDirectory + "unique_signs_map.ser"));
		uniqueSignaturesRuleMap = new HashMap<LGraph, ArrayList<Integer>>();
		for (Integer id : uniqueRuleMap.keySet()) {
			LGraph g = (LGraph) uniqueRuleMap.get(id).getReactionSignatureAdded();
			if (!uniqueSignaturesRuleMap.containsKey(g)) {
				ArrayList<Integer> x = new ArrayList<Integer>();
				x.add(id);
				uniqueSignaturesRuleMap.put(g, x);
			} else
				uniqueSignaturesRuleMap.get(g).add(id);
		}
		out.writeObject(uniqueSignaturesRuleMap);
		out.close();
		System.out.println("Unique Signatures saved");
	}

	
	public static void loadKnownMolNames() throws IOException {
		knownMolNames = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader("kegg_updated_first_name.txt"));
		String line = "";
		while ((line = br.readLine()) != null) {
			String molNo = line.split("\t")[0];
			String molName = line.split("\t")[1];
			knownMolNames.put(molNo, molName);
		}
		br.close();
	}
	@SuppressWarnings("unchecked")
	public static void loadSignatureCanonicalLabels()
			throws ClassNotFoundException, IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				Globals.indexDirectory + "sign_labels.ser"));
		signatureCanonicalLabels = (HashMap<String, LGraph>) in.readObject();
		signatureCanonicalLabelsReverse = (HashMap<LGraph, String>) in
				.readObject();
		in.close();
	}

	static void saveSignatureCanonicalLabels() throws IOException {
		GraphLabelling giso = new GraphLabelling();
		signatureCanonicalLabels = new HashMap<String, LGraph>();
		signatureCanonicalLabelsReverse = new HashMap<LGraph, String>();
		for (LGraph reactionSignature : uniqueSignaturesRuleMap.keySet()) {
			String reactionSignatureLabel = giso
					.getCanonicalLabel(reactionSignature);
			signatureCanonicalLabels.put(reactionSignatureLabel,
					reactionSignature);
			signatureCanonicalLabelsReverse.put(reactionSignature,
					reactionSignatureLabel);
		}
		FileOutputStream fileOut = new FileOutputStream(Globals.indexDirectory
				+ "sign_labels.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(signatureCanonicalLabels);
		out.writeObject(signatureCanonicalLabelsReverse);
		out.close();
		fileOut.close();
	}
	
	@SuppressWarnings("unchecked")
	static void loadRules() throws FileNotFoundException, IOException, ClassNotFoundException {
		uniqueRuleMap = new HashMap<Integer, ReactionRule>();
		allRuleMap = new HashMap<Integer, ReactionRule>();
		reverseClusterMap = new HashMap<Integer, Integer>();

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(Globals.indexDirectory + "rules.ser"));
		rules = (ArrayList<ReactionRule>) in.readObject();
		in.close();
		clusters = new HashMap<Integer, ArrayList<Integer>>();
		in.close();

		for (ReactionRule r : rules)
			allRuleMap.put(r.getId(), r);

		System.out.println("Rules loaded");
		System.out.println(allRuleMap.size());
		System.out.println("Clustering rules");
		ClusteringRules cr = new ClusteringRules(rules);
		cr.preComputeLabels();
		cr.clusterHashing();

		reverseClusterMap = new HashMap<Integer, Integer>(cr.getReverseMap());
		clusters = new HashMap<Integer, ArrayList<Integer>>(cr.getClusters());

		for (int x : clusters.keySet())
			uniqueRuleMap.put(x, allRuleMap.get(x));
		System.out.println("Size of unique rule map " + uniqueRuleMap.size());
		System.out.println("Size of clusters " + clusters.size());
		System.out.println("Size of reverse cluster map " + reverseClusterMap.size());
	}

	@SuppressWarnings("unchecked")
	static void loadFinalPairs() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(Globals.indexDirectory + "finalPairs.ser"));
		finalPairs = (HashMap<Integer, RPM>) in.readObject();
		in.close();
		System.out.println("Pairs loaded");
	}

	@SuppressWarnings("unchecked")
	public static void loadKnownMolLabels() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(
				new FileInputStream(Globals.indexDirectory + "known_mol_labels.ser"));
		knownMolLabels = (HashMap<String, String>) in.readObject();
		knownMolIsomorphicMols = (HashMap<String, ArrayList<String>>) in.readObject();
		knownMolCanonicalLabel = (HashMap<String, String>) in.readObject();
		in.close();
//		System.out.println(knownMolIsomorphicMols.get(knownMolCanonicalLabel.get("METHYL_NICOTINATE")));
//		System.out.println(knownMolIsomorphicMols.get(knownMolCanonicalLabel.get("METHYL_PHENYLACETATE")));
//		System.out.println(knownMolIsomorphicMols.get(knownMolCanonicalLabel.get("METHYL_4-METHOXYBENZOATE")));
		System.out.println("Known mol labels loaded");
	}

	public static ArrayList<String> readMolFileNames(String molDirectory) {
		File folder = new File(molDirectory);
		File[] listOfFiles = folder.listFiles();
		ArrayList<String> molFileNames = new ArrayList<>();
		for (int i = 0; i < listOfFiles.length; i++)
			molFileNames.add(listOfFiles[i].getName().split("\\.")[0]);
		return molFileNames;
	}

	public static void loadMolecules() throws IOException {
		knownMolecules = new HashMap<String, LGraph>();
		knownMoleculesOld = new HashMap<String, LGraph>();

		ArrayList<String> fileNames = readMolFileNames(Globals.molDirectory);

		ConvertEdgeLabels cel = new ConvertEdgeLabels();
		for (String name : fileNames) {
			LGraph[] g = LGraphFile.loadLGraphs(Globals.molDirectory + "/" + name + ".mol");
			g[0].setId(name);
			knownMoleculesOld.put(name, g[0]);
			if (Globals.INCLUDE_EDGE_LABELS)
				g[0] = cel.addEdgeLabelNodes(g[0]);
			g[0].setId(name);
			knownMolecules.put(name, g[0]);
		}
		System.out.println("Mols loaded");
	}

	public static void saveknownMolLabels() throws IOException {
		GraphLabelling giso = new GraphLabelling();
		knownMolLabels = new HashMap<String, String>();
		knownMolIsomorphicMols = new HashMap<String, ArrayList<String>>();
		// From label to list of mols.
		knownMolCanonicalLabel = new HashMap<String, String>();
		for (String mol : knownMolecules.keySet()) {
			String canonLabel = giso.getCanonicalLabel(knownMolecules.get(mol));
			if (!knownMolIsomorphicMols.containsKey(canonLabel))
				knownMolIsomorphicMols.put(canonLabel, new ArrayList<String>());
			knownMolIsomorphicMols.get(canonLabel).add(mol);
			knownMolCanonicalLabel.put(mol, canonLabel);

			if (!knownMolLabels.containsKey(canonLabel) || knownMolLabels.get(canonLabel).compareTo(mol) > 0)
				knownMolLabels.put(canonLabel, mol);
		}

		// System.out.println(knownMolIsomorphicMols.get(Index.knownMolCanonicalLabel.get("C00448")));
		// System.out.println(knownMolIsomorphicMols.get(Index.knownMolCanonicalLabel.get("C16689")));

		FileOutputStream fileOut = new FileOutputStream(Globals.indexDirectory + "known_mol_labels.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(knownMolLabels);
		out.writeObject(knownMolIsomorphicMols);
		out.writeObject(knownMolCanonicalLabel);
		out.close();
		fileOut.close();
	}

	// This is to get unique label maps.
	static void printIntAtomMap() {
		Set<String> keys = new HashSet<String>();
		for (LGraph g : Index.knownMolecules.values()) {
			ctree.lgraph.LabelMap l = new ctree.lgraph.LabelMap();
			l.importGraph(g);
			keys.addAll(l.getMap().keySet());
		}
		ArrayList<String> x = (new ArrayList<String>(keys));
		Collections.sort(x);
		int i = 0;
		for (String string : x) {
			System.out.println(string + " " + i);
			i++;
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		loadAll();
		// removeBigDiffPairs();
		System.exit(0);
		// print rules for imp. nodes.

		for (RPM pm : finalPairs.values())
			System.out.println(pm.getRpair().getReaction().getId() + " " + pm.getRpair().getReactant() + " "
					+ pm.getRpair().getProduct());
		System.exit(0);
		ClusteringRules cr = new ClusteringRules(rules);
		cr.preComputeLabels();
		cr.clusterHashing();

		BufferedReader br = new BufferedReader(new FileReader("/home/aravind/Desktop/Gephi/pr.txt"));
		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.length() < 2)
				break;
			int id = Integer.parseInt(line);
			System.out.println(Index.finalPairs.get(id).getRpair());
		}
		br.close();
	}

	static void removeATPEtc() {
		ArrayList<Integer> idsToRemove = new ArrayList<Integer>();
		for (Integer x : Index.finalPairs.keySet()) {
			RPAIR rp = Index.finalPairs.get(x).getRpair();
			if (rp.getProduct().contentEquals("C00002") || rp.getProduct().contentEquals("C00020")
					|| rp.getProduct().contentEquals("C00008"))
				idsToRemove.add(x);
			if (rp.getReactant().contentEquals("C00002") || rp.getReactant().contentEquals("C00020")
					|| rp.getReactant().contentEquals("C00008"))
				idsToRemove.add(x);
			if (rp.getReaction().getId().contentEquals("R01068"))
				idsToRemove.add(x);
		}

		for (Integer x : idsToRemove) {
			Index.finalPairs.remove(x);
			Index.allRuleMap.remove(x);
			if (Index.uniqueRuleMap.containsKey(x))
				Index.uniqueRuleMap.remove(x);
			if (Index.reverseClusterMap.containsKey(x))
				Index.reverseClusterMap.remove(x);
		}
		System.out.println(Index.finalPairs.size());
	}

	// This is a function that can be modified to remove some spurious pairs
	// generated by including reverse pairs.
	static void removeBigDiffPairs() {
		GraphDistance sed = new GraphDistance();
		ArrayList<Integer> idsToRemove = new ArrayList<Integer>();
		for (Integer x : Index.finalPairs.keySet()) {
			RPAIR rp = Index.finalPairs.get(x).getRpair();
			// if (rp.getReactant().contentEquals("C14857")
			// || rp.getProduct().contentEquals("C14857"))
			// idsToRemove.add(x);

			LGraph reactant = Index.knownMolecules.get(rp.getReactant());
			LGraph product = Index.knownMolecules.get(rp.getProduct());
			// if(rp.getReactant().contentEquals("C00111"))
			// idsToRemove.add(x);
			if (sed.getEditDistance(reactant, product, false) > 130
					|| 1.0 * Math.abs(product.numE() - reactant.numE()) / Math.min(product.numE(), reactant.numE()) > 8)
				idsToRemove.add(x);

		}
		// Index.knownMolecules.remove("C16689");
		for (Integer x : idsToRemove) {
			// System.out
			// .println(Index.finalPairs.get(x).getReactantProductPair());
			Index.finalPairs.remove(x);
			Index.allRuleMap.remove(x);
			Index.rules.remove(x);
		}
		System.out.println("Removed " + idsToRemove.size());
	}

}
