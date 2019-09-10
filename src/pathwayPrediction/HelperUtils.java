package pathwayPrediction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;

import globals.Index;
import pathwayPrediction.Heuristic.PQNode;
import ruleMining.RPM.Reaction;

public class HelperUtils {

	static 	boolean helperReactantCheck(String reactantName, int ruleId, String productMol) {
		// Make sure product is not one of the helper molecules.
		for (int rid : Index.clusters.get(ruleId)) {
			if(! Index.allRuleMap.containsKey(rid))
				continue;
			ArrayList<String> helperReactants = Index.allRuleMap.get(rid).getHelperReactants();
			if (!helperReactants.contains(productMol))
				return true;
		}
		return false;
	}

	static boolean sanityCheck(String reactantName, String productMol) {
		// Make sure we don't come across the product itself in reverse
		// direction.
		if (reactantName.contentEquals(productMol))
			return false;
		if(! Index.knownMolCanonicalLabel.containsKey(reactantName))
			return true;
		if (Index.knownMolCanonicalLabel.get(reactantName).contentEquals(Index.knownMolCanonicalLabel.get(productMol)))
			return false;
		return true;
	}
	
	static void printFullPathway(PQNode node) {
		ArrayList<Reaction> rxns = new ArrayList<>();
		for (int i = 0; i < node.path.size(); i++) {
			int ruleId = node.path.get(i);
			ArrayList<String> reactants = new ArrayList(Index.uniqueRuleMap.get(ruleId).getHelperReactants());
			ArrayList<String> products =  new ArrayList(Index.uniqueRuleMap.get(ruleId).getHelperProducts());
			products.add(node.pathway.get(i));
			reactants.add(node.pathway.get(i + 1));
			Reaction r = new Reaction();
			r.setId(Index.finalPairs.get(ruleId).getRpair().getReaction().getId());
			r.setReactants(reactants);
			r.setProducts(products);
			rxns.add(r);
			System.out.println("ruleid :"+ruleId+" "+Index.clusters.get(ruleId));
			for(int x: Index.clusters.get(ruleId))
				System.out.print(Index.finalPairs.get(x).getRpair().getReaction().getId()+" ");
			System.out.println();
		}
		Collections.reverse(rxns);
		for (Reaction r : rxns)
			System.out.println(r);
		System.out.println();
	}

	static public LinkedHashMap<String, Double> sortHashMapByValues(
	        HashMap<String, Double> passedMap) {
	    List<String> mapKeys = new ArrayList<>(passedMap.keySet());
	    List<Double> mapValues = new ArrayList<>(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);

	    LinkedHashMap<String, Double> sortedMap =
	        new LinkedHashMap<>();

	    Iterator<Double> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Double val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();

	        while (keyIt.hasNext()) {
	            String key = keyIt.next();
	            Double comp1 = passedMap.get(key);
	            Double comp2 = val;

	            if (comp1.equals(comp2)) {
	                keyIt.remove();
	                sortedMap.put(key, val);
	                break;
	            }
	        }
	    }
	    return sortedMap;
	}
	static void printPQ(PriorityQueue<PQNode> PQ) {
		for (PQNode x : PQ)
			System.out.print(x.nodeId + " (" + x.pathway + " " + x.dist + " ) ");
		System.out.println();
	}
}
