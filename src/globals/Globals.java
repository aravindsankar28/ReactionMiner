package globals;
/**
 * Global variable definitions.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public final class Globals {
		
	/**
	 * File paths for the input files. 
	 * */
	public static String originalMolDirectory = "data/mol";
	public static String orgDatasetDirectory = "data/Organism_Dataset/kegg/";
	public static String molDirectory = "data/molStereo_Hadded_PH_CoA";	
	public static String equationsFile = "data/reactionDatabase/ReactionsUpdated.txt";
	public static String ignoredCompsFile ="data/comps_to_remove.txt"; 
	public static String blackListPairsFile =  "data/blacklist_pairs.txt";
	public static String blackListCompsFile = "data/blacklist_comps.txt";
	public static String atomIntegerMapFile = "data/AtomIntegerMap.txt";
	
	/**
	 * File paths for computed index files.
	 */
	public static String ruleDirectory = "data/reactionRules";
	public static String indexDirectory = "index/";	
	public static String evalDirectory = "../Evaluation_new/";
	
	/**
	 * Code specific flags. 
	 */
	public static boolean DEBUG = false;
	public static boolean INCLUDE_EDGE_LABELS = true;
	public static boolean INCLUDE_STEREO = true;
	
	// TODO - check it out. 
	
	
	
	
	public static HashMap<String, Integer> loadAtomIntMap() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				atomIntegerMapFile));
		HashMap<String, Integer> atomIntegerMap = new HashMap<String, Integer>();
		String line = "";
		while ((line = br.readLine()) != null)
			atomIntegerMap.put(line.split(" ")[0],
					Integer.parseInt(line.split(" ")[1]));
		br.close();
		return atomIntegerMap;
	}
			
	public static HashMap<Integer, String> loadIntAtomMap() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				atomIntegerMapFile));
		HashMap<Integer, String> atomIntegerMap = new HashMap<Integer, String>();
		String line = "";
		while ((line = br.readLine()) != null)
			atomIntegerMap.put(Integer.parseInt(line.split(" ")[1]),
					line.split(" ")[0]);
		br.close();
		return atomIntegerMap;
	}
}
