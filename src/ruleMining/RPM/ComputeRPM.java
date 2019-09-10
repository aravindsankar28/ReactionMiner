package ruleMining.RPM;

import graph.*;
import java.io.*;
import java.util.*;
import globals.Globals;
import ctree.lgraph.*;

/**
 * 
 * @author aravind Compute reactant product pairs according to PRM algorithm.
 *         Call the driver function. This code reads the appropriate reaction
 *         database specified and mol directory to compute the RPM for all
 *         reactions.
 */
public class ComputeRPM implements Serializable {
	private static final long serialVersionUID = 1409967185561555448L;

	/**
	 * Variables created by reading the inputs.
	 */
	ArrayList<String> molFileNames;
	// List of file names of all molecules in the database.
	ArrayList<Reaction> reactions; // List of reactions
	ArrayList<String> compoundsToRemove;
	// List of compounds that are not considered during RPM.

	/**
	 * Outputs created by the RPM algorithm.
	 */
	ArrayList<RPAIR> rpairs; // List of RPAIRS created
	ArrayList<RPAIR> matchedPairs; // ?
	private ArrayList<RPM> rpairMap; // List of RPMs created.

	public ComputeRPM() {
		rpairs = new ArrayList<RPAIR>();
		molFileNames = new ArrayList<String>();
		reactions = new ArrayList<Reaction>();
		matchedPairs = new ArrayList<RPAIR>();
		rpairMap = new ArrayList<RPM>();
		compoundsToRemove = new ArrayList<String>();
	}

	public ArrayList<Reaction> getReactions() {
		return reactions;
	}

	public ArrayList<RPM> getRpairMap() {
		return rpairMap;
	}

	public void setRpairMap(ArrayList<RPM> reactantProductPairsMapping) {
		this.rpairMap = reactantProductPairsMapping;
	}

	/*
	 * Read list of known molecule ids from the mol directory. For mol files
	 * named in kegg format - C00001.mol and uses the string before the "."
	 * (C00001) as the id of the molecule.
	 */
	public void readMolFileNames(String molDirectory) {
		File folder = new File(molDirectory);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++)
			molFileNames.add(listOfFiles[i].getName().split("\\.")[0]);
	}

	/*
	 * Get all reactant-product pairings (all combinations) from a reaction r.
	 */
	public ArrayList<RPAIR> getPairs(Reaction r) {
		ArrayList<RPAIR> rpairs = new ArrayList<RPAIR>();
		for (String product : r.products)
			for (String reactant : r.reactants)
				rpairs.add(new RPAIR(reactant, product, r));
		return rpairs;
	}

	/*
	 * Read a list of compounds that we do not consider for mapping. Typically
	 * very small molecules that are useless for rule mining.
	 */
	public void readCompoundsToRemove() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(Globals.ignoredCompsFile));
		String line = "";
		while ((line = br.readLine()) != null) {
			String mol = line.split(" ")[0];
			compoundsToRemove.add(mol);
		}
		br.close();
	}

	/*
	 * Read reactions from the file (along with their ids) and create reaction
	 * objects. Note there that a reaction is skipped if there is no valid
	 * reactant or no valid product.
	 * 
	 */
	public void parseReactionsWithIds(String filename) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filename));
		int skippedReactionsCounter = 0;
		int totalReactionsCounter = 0;
		String fullLine;
		while ((fullLine = br.readLine()) != null) {
			String line = fullLine.split("\t")[0];
			String rid = fullLine.split("\t")[1]; // Reaction id.
			totalReactionsCounter++;
			// File format check - we assume the file format where reactants and
			// products are separated by <=, => , <=>
			if (!line.contains("<=>") && !line.contains("<=") && !line.contains("=>")) {
				System.err.println("Invalid reaction");
				skippedReactionsCounter++;
				continue;
			}
			// Assume that reactions are not reversible by default.
			boolean reversible = false;
			if (line.contains("<=>"))
				reversible = true;
			String reactantsString = "", productsString = "";
			// Identify reactants string
			if (line.contains("<"))
				reactantsString = line.substring(0, line.indexOf('<'));
			else
				reactantsString = line.substring(0, line.indexOf('='));
			// Identify products string
			if (line.contains(">"))
				productsString = line.substring(line.indexOf('>'), line.length());
			else
				productsString = line.substring(line.indexOf('='), line.length());

			// We expect at least one product.
			if (!productsString.contains("C")) {
				System.err.println("No valid mol in product side");
				skippedReactionsCounter++;
				continue;
			}

			// Process the string to obtain the reactants and products.
			Reaction r = new Reaction();
			r.id = rid;
			// Process reactants
			String[] reactantSplitStrings = reactantsString.split(" ");
			boolean skipped = false;
			int plusCount = 0;
			int skipCount = 0;
			for (String str : reactantSplitStrings) {
				if (str.contentEquals("+"))
					plusCount++;
				if (str.contains("G")) {
					System.err.println("Reactant" + str + " is not an organic molecule");
					skipped = true;
					break;
				}
				if (str.startsWith("C"))
					r.reactants.add(str.substring(0, 6));
			}

			if (r.reactants.size() != plusCount + 1)
				skipped = true;

			// Validate reaction format.
			if (skipped || skipCount == r.reactants.size()) {
				skippedReactionsCounter++;
				continue;
			}
			// Process products
			String[] productSplitStrings = productsString.split(" ");
			skipped = false;
			plusCount = 0;
			skipCount = 0;
			for (String str : productSplitStrings) {
				if (str.contentEquals("+"))
					plusCount++;
				if (str.contains("G")) {
					System.err.println("Product" + str + " is not an organic molecule");
					skipped = true;
					break;
				}
				if (str.startsWith("C"))
					r.products.add(str.substring(0, 6));
			}
			// Validate the reaction format.
			if (r.products.size() != plusCount + 1)
				skipped = true;
			if (skipped || skipCount == r.products.size()) {
				skippedReactionsCounter++;
				continue;
			}
			reactions.add(r);
			rpairs.addAll(getPairs(r)); // Add all possible rpairs for this
										// reaction.

			if (reversible) {
				Reaction reversedReaction = new Reaction();
				reversedReaction.id = rid;
				reversedReaction.reactants.addAll(r.products);
				reversedReaction.products.addAll(r.reactants);
				reactions.add(reversedReaction);
				rpairs.addAll(getPairs(reversedReaction));
			}
		}
		System.out.println("Total reactions : " + totalReactionsCounter);
		System.out.println("Skipped reactions : " + skippedReactionsCounter);
		System.out.println("Total reactant product pairs for consideration : " + rpairs.size());
		br.close();
	}

	/*
	 * Compute graph distance (subgraph edit or graph edit) for each reactant
	 * product pair. Find number of useful (no too small) reactants and products
	 * in a reaction. If that is same, use graph edit distance else use subgraph
	 * edit distance. Shown to work reasonably well.
	 */
	void computeGraphDistances() throws IOException {
		GraphDistance gd = new GraphDistance();
		for (RPAIR reactantProductPair : rpairs) {
			// First load the reactant and product molecules.
			String reactant_file = Globals.molDirectory.concat("/").concat(reactantProductPair.reactant).concat(".mol");
			String product_file = Globals.molDirectory.concat("/").concat(reactantProductPair.product).concat(".mol");
			if (!new File(reactant_file).exists())
				continue;
			if (!new File(product_file).exists())
				continue;

			LGraph[] reactant = LGraphFile.loadLGraphs(reactant_file);
			LGraph[] product = LGraphFile.loadLGraphs(product_file);

			boolean sub = false;
			ArrayList<LGraph> reactants = new ArrayList<LGraph>();
			ArrayList<LGraph> products = new ArrayList<LGraph>();
			int rcount = 0;
			int pcount = 0;
			// Load all reactants - Candidates include only compounds with > 3
			// nodes.

			for (String r : reactantProductPair.getReaction().reactants) {
				// We assume the mol format for each compound.
				String str = Globals.molDirectory + "/" + r + ".mol";
				if (!new File(str).exists())
					continue;
				LGraph x = LGraphFile.loadLGraphs(str)[0];
				reactants.add(x);
				if ((x.numV() > 3 || graphContainsCoA(x)) && !compoundsToRemove.contains(r))
					rcount++;
			}

			for (String p : reactantProductPair.getReaction().products) {
				String str = Globals.molDirectory + "/" + p + ".mol";
				if (!new File(str).exists())
					continue;
				LGraph x = LGraphFile.loadLGraphs(Globals.molDirectory + "/" + p + ".mol")[0];
				products.add(x);
				if ((x.numV() > 3 || graphContainsCoA(x)) && !compoundsToRemove.contains(p))
					pcount++;
			}
			// Here, rcount and pcount = # valid reactants and products.
			// TODO : Heuristic here : Use GED (Graph Edit Distance) when rcount
			// == pcount and SED (Subgraph Edit Distance) otherwise.
			if (rcount == pcount)
				sub = false;
			else
				sub = true;

			reactantProductPair.distance = gd.getEditDistance(reactant[0], product[0], sub);
			reactantProductPair.reactantSize = reactant[0].numV() + reactant[0].numE();
			reactantProductPair.productSize = product[0].numV() + product[0].numE();
		}
	}

	/*
	 * Essentially neighbor biased mapping between product and reactant. Compute
	 * it for all reactant product pairs.
	 */
	void computeMappings() throws IOException {
		NeighborBiasedMapping nbm = new NeighborBiasedMapping();
		int counter = 0;
		for (RPAIR reactantProductPair : matchedPairs) {
			LGraph reactant = LGraphFile
					.loadLGraphs(Globals.molDirectory + "/" + reactantProductPair.reactant + ".mol")[0];
			LGraph product = LGraphFile
					.loadLGraphs(Globals.molDirectory + "/" + reactantProductPair.product + ".mol")[0];
			ArrayList<Integer> map = nbm.getMapping(reactant, product);
			RPM pm = new RPM(reactantProductPair);
			pm.mapping = map;
			/*
			 * The mapping contains an entry (id in reactant) // for each node
			 * in the product : -1 if // unmapped.
			 */
			pm.id = counter;
			getRpairMap().add(pm);
			counter++;
		}
	}

	void matchPairs() {
		int index = 0;
		for (Reaction reaction : reactions) {
			// For each reaction, find all same product pairs.
			for (int i = 0; i < reaction.products.size(); i++) {
				/*
				 * If this compound is to be ignored, then don't create a pair
				 */
				if (compoundsToRemove.contains(reaction.products.get(i))) {
					index += reaction.reactants.size();
					continue;
				}
				ArrayList<RPAIR> sameProductPairs = new ArrayList<RPAIR>();
				for (int j = 0; j < reaction.reactants.size(); j++) {
					sameProductPairs.add(rpairs.get(index));
					index++;
				}
				if (sameProductPairs.size() != 0) {
					// Among all pairs with same product, choose best reactant.
					RPAIR bestPair = chooseBestPair(sameProductPairs);
					if (bestPair != null && molFileNames.contains(bestPair.product)
							&& molFileNames.contains(bestPair.reactant))
						matchedPairs.add(bestPair);
				}
			}
		}
		/*
		 * TODO: Additional code for add reverse pairs of all generated rpairs.
		 * This may be needed to improve performance and is valid in case of
		 * reversible reactions.
		 */

		// Here, add all pairs - reversible pairs to prevent issues in
		// decomposition reactions.
		ArrayList<RPAIR> matchedPairsNew = new ArrayList<RPAIR>(matchedPairs);
		ArrayList<String> mps = new ArrayList<String>();
		for (RPAIR p : matchedPairs)
			mps.add(p.reactant + " " + p.product + " " + p.reaction.id);

		for (RPAIR p : matchedPairs) {
			Reaction r = new Reaction();
			r.id = p.reaction.id;
			r.reactants.addAll(p.reaction.products);
			r.products.addAll(p.reaction.reactants);
			RPAIR rev = new RPAIR(p.product, p.reactant, r);
			String str = p.product + " " + p.reactant + " " + r.id;
			if (!mps.contains(str)) {
				matchedPairsNew.add(rev);
				mps.add(str);
			}
		}
		System.out.println("Old matched pairs size " + matchedPairs.size());
		System.out.println("New matched pairs size " + matchedPairsNew.size());
		// TODO : Comment this to revert to original setting
		matchedPairs = new ArrayList<RPAIR>(matchedPairsNew);
	}

	/*
	 * Given a list of same product pairs, return the best pair based on least
	 * distance (and closest size in case of tie).
	 */
	RPAIR chooseBestPair(ArrayList<RPAIR> sameProductPairs) {
		ArrayList<RPAIR> chosenPairs = new ArrayList<RPAIR>();
		double minDist = Double.MAX_VALUE;
		for (RPAIR rpair : sameProductPairs) {
			if (compoundsToRemove.contains(rpair.reactant))
				continue;
			if (rpair.distance < minDist)
				minDist = rpair.distance;
		}

		// No match- return null.
		if (minDist == Double.MAX_VALUE)
			return null;

		for (RPAIR rpair : sameProductPairs)
			if (rpair.distance == minDist && !compoundsToRemove.contains(rpair.reactant))
				chosenPairs.add(rpair);

		if (chosenPairs.size() == 1)
			return chosenPairs.get(0);
		else {
			// If multiple pairs are found with same graph distance, then use
			// tie-breaking based on closest size of molecules.
			int minSize = Integer.MAX_VALUE;
			RPAIR chosenPair = new RPAIR();
			for (RPAIR rpair : chosenPairs) {
				if (Math.abs(rpair.reactantSize - rpair.productSize) < minSize) {
					minSize = Math.abs(rpair.reactantSize - rpair.productSize);
					chosenPair = rpair;
				}
			}
			return chosenPair;
		}
	}

	// Helper function to check if the molecule contains CoA
	boolean graphContainsCoA(LGraph x) {
		for (ctree.graph.Vertex v : x.V()) {
			if (v.toString().contentEquals("CoA"))
				return true;
		}
		return false;
	}

	/**
	 * Helper function to write all generated matched rpairs.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	void dumpMatchedPairs(String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		for (RPAIR reactantProductPair : matchedPairs) {
			fw.write(reactantProductPair.reactant + ";" + reactantProductPair.product + ";");
			int i = 0;
			for (String reactant : reactantProductPair.reaction.reactants) {

				if (i != 0)
					fw.write(" + " + reactant);
				else
					fw.write(reactant);
				i++;
			}
			fw.write(" <=> ");
			i = 0;
			for (String product : reactantProductPair.reaction.products) {

				if (i != 0)
					fw.write(" + " + product);
				else
					fw.write(product);
				i++;
			}
			fw.write("\n");
		}
		fw.close();
	}

	/*
	 * Driver function used to create all reactant product pairs from a given
	 * reaction file. The reaction file name and mol directory are given in the
	 * globals file./
	 */
	public void driver() throws IOException {
		readMolFileNames(Globals.molDirectory);
		readCompoundsToRemove();
		parseReactionsWithIds(Globals.equationsFile);
		computeGraphDistances();
		matchPairs();
		computeMappings();
		System.out.println("Final pairs :" + matchedPairs.size());

		// Write all reactant product pairs - used for rule mining.
		FileOutputStream fileOut = new FileOutputStream(Globals.indexDirectory + "pairs.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();

		// Write unique reactions alone - dunno where it's used - probably
		// somewhere.
		fileOut = new FileOutputStream(Globals.indexDirectory + "reactions.ser");
		out = new ObjectOutputStream(fileOut);
		ArrayList<Reaction> outputReactions = new ArrayList<Reaction>();
		for (RPAIR pair : matchedPairs)
			outputReactions.add(pair.reaction);
		Set<Reaction> s = new LinkedHashSet<Reaction>(outputReactions);
		outputReactions = new ArrayList<Reaction>(s);
		System.out.println("No. of unique reactions processed :" + outputReactions.size());
		out.writeObject(outputReactions);
		out.close();
		fileOut.close();
	}

	public static void main(String[] args) throws IOException {
		ComputeRPM rpp = new ComputeRPM();
		rpp.driver();
	}
}
