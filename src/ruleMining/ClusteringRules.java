package ruleMining;

import graph.*;
import java.io.*;
import java.util.*;

import ruleMining.ReactionRule.*;

import ctree.graph.Edge;
import ctree.lgraph.LGraph;
import globals.Globals;

/**
 * @author aravind
 * Cluster reaction rules based on same reaction signatures and subgraph removed. 
 * This works by hashing using canonical labels.
 * Note that our reaction signature here includes the subgraph added portion.
 */

public class ClusteringRules {

	static boolean DEBUG;
	ArrayList<ReactionRule> rules;
	HashMap<Integer, Integer> reverseMap;
	public ArrayList<Integer> uniqueRuleIDs;
	HashMap<Integer, ArrayList<Integer>> clusters; // 
		
	public ArrayList<ReactionRule> getRules() {
		return rules;
	}

	public void setRules(ArrayList<ReactionRule> rules) {
		this.rules = rules;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getClusters() {
		return clusters;
	}
	
	HashMap<Integer, String> signatureCanonicalLabels;
	HashMap<Integer, String> drpCanonicalLabels;
	
	public ClusteringRules(ArrayList<ReactionRule> rulesToConsider) {
		rules = new ArrayList<ReactionRule>(rulesToConsider);
		uniqueRuleIDs = new ArrayList<Integer>();		
		ClusteringRules.DEBUG = Globals.DEBUG;
		clusters = new HashMap<Integer, ArrayList<Integer>>();
		signatureCanonicalLabels = new HashMap<Integer, String>();
		drpCanonicalLabels = new HashMap<Integer, String>();
		reverseMap = new HashMap<Integer, Integer>();
	}

	public void preComputeLabels() throws IOException {
		// Pre compute canonical labels for reaction signatures.
		GraphLabelling giso = new GraphLabelling();

		for (ReactionRule rule : rules) {
			signatureCanonicalLabels.put(rule.getId(), giso
					.getCanonicalLabel((LGraph) rule.getReactionSignatureAdded()));
			drpCanonicalLabels.put(rule.getId(), giso
					.getCanonicalLabel((LGraph) rule.getSubgraphRemoved()));
		}
		System.out.println("Pre computation over");
	}

	public void clusterHashing() throws IOException {
		preComputeLabels();
		ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> labelClusterMap = new HashMap<String, ArrayList<Integer>>();

		for (int i = 0; i < rules.size(); i++) {
			ReactionRule rule_i = rules.get(i);
			int id_i = rule_i.getId();
			if (DEBUG)
				System.out.println("Processing rule number " + i + " id "
						+ id_i);
			String label = "";
			label += signatureCanonicalLabels.get(id_i);
			label += "\t" + drpCanonicalLabels.get(id_i);
			label += "\t" + rule_i.getSubgraphAdded().size();
			label += "\t" + rule_i.getMetaData().getConnectingEdges().size();
			label += "\t" + rule_i.getMetaData().getInterRCEdges().size();

			if (!labelClusterMap.containsKey(label))
				labelClusterMap.put(label, new ArrayList<Integer>());

			labelClusterMap.get(label).add(id_i);
		}

		for (String str : labelClusterMap.keySet())
			clusters.add(labelClusterMap.get(str));
		
		System.out.println("No. of clusters " + labelClusterMap.size());

		for (ArrayList<Integer> cluster : clusters) {
			uniqueRuleIDs.add(cluster.get(cluster.size() - 1));
			this.clusters.put(cluster.get(cluster.size() - 1), cluster);
			for (Integer x : cluster)
				this.reverseMap.put(x, cluster.get(cluster.size() - 1));
		}
	}
	
	public HashMap<Integer, Integer> getReverseMap() {
		return reverseMap;
	}
	
	boolean isEqualStringLists(ArrayList<String> l1, ArrayList<String> l2) {
		if (l1.size() != l2.size())
			return false;

		Collections.sort(l1);
		Collections.sort(l2);
		for (int i = 0; i < l1.size(); i++) {
			if (l1.get(i).compareTo(l2.get(i)) != 0)
				return false;
		}

		return true;
	}

	boolean isEqualLists(ArrayList<Integer> l1, ArrayList<Integer> l2) {
		if (l1.size() != l2.size())
			return false;
		Collections.sort(l1);
		Collections.sort(l2);
		for (int i = 0; i < l1.size(); i++) {
			if (l1.get(i).compareTo(l2.get(i)) != 0)
				return false;
		}

		return true;
	}

	boolean isEqualBonds(ArrayList<Edge> l1, ArrayList<Edge> l2) {
		if (l1.size() != l2.size())
			return false;
		Comparator<Edge> comparator = new Comparator<Edge>() {

			public int compare(Edge o1, Edge o2) {
				if (o1.v1() < o2.v1())
					return -1;
				else if (o1.v1() > o2.v1())
					return 1;
				else {
					if (o1.v2() < o2.v2())
						return -1;
					else
						return 1;
				}
			}

		};

		// sort edges
		Collections.sort(l1, comparator);
		Collections.sort(l2, comparator);
		for (int i = 0; i < l1.size(); i++) {
			{
				if (l1.get(i).v1() != l2.get(i).v1()
						|| l1.get(i).v2() != l2.get(i).v2())
					return false;
			}
		}

		return true;
	}

	boolean isEqualInterRCBonds(ArrayList<InterRCEdge> l1,
			ArrayList<InterRCEdge> l2) {
		if (l1.size() != l2.size())
			return false;
		Comparator<InterRCEdge> comparator = new Comparator<InterRCEdge>() {

			public int compare(InterRCEdge o1, InterRCEdge o2) {
				if (o1.getV1() < o2.getV1())
					return -1;
				else if (o1.getV1() > o2.getV1())
					return 1;
				else {
					if (o1.getV2() < o2.getV2())
						return -1;
					else
						return 1;
				}
			}

		};

		// sort edges
		Collections.sort(l1, comparator);
		Collections.sort(l2, comparator);
		for (int i = 0; i < l1.size(); i++) {
			{
				if (l1.get(i).getV1() != l2.get(i).getV1()
						|| l1.get(i).getV2() != l2.get(i).getV2()
						|| l1.get(i).getType() != l2.get(i).getType())
					return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException,
			IOException, ClassNotFoundException {
				
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				Globals.indexDirectory + "rules.ser"));
		
		ClusteringRules cr = new ClusteringRules((ArrayList<ReactionRule>) in.readObject());		
		cr.clusterHashing();
		in.close();
				
		System.exit(0);

		FileOutputStream fileOut = new FileOutputStream(Globals.evalDirectory
				+ "rules_clustered_final.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);

		ArrayList<Integer> l = cr.uniqueRuleIDs;				
		Collections.sort(l);
		System.out.println(l);
		System.out.println(l.size());		
		out.writeObject(l);
		out.writeObject(cr.clusters);
		out.close();
		fileOut.close();	
	}
}
