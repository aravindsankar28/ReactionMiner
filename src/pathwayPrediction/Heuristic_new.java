package pathwayPrediction;

import reactionRuleNetwork.RRN;
import ruleMining.RuleApply;

import java.io.*;
import java.util.*;

import ctree.lgraph.*;
import ctree.mapper.*;
import globals.Index;
import openBabel.Graph2Smiles;
import preProcess.MolFormatConvert;

public class Heuristic_new {
	boolean restrictTrainDatabase = false;

	// Given two molecules as input - we assume they exist in the database.
	String reactantName;
	String productName;
	GraphMapper stateSearchMapper = new StateSearchMapper();
	LGraph reactantGraph;
	LGraph productGraph;

	// The components that are used for prediction - RRN, rule application code
	// and mapper used in Used in our heuristic computation.
	RRN rrn;
	RuleApply ruleApply;
	GraphMapper neighborBiasedMapper;
	LGraphDistance gdis;

	// General parameters.
	int maxLen = 10;

	Heuristic_new() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		rrn = new RRN();
		neighborBiasedMapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
		ruleApply = new RuleApply();
		gdis = new LGraphDistance();
	}

	double graphDistanceWrapper(LGraph a, LGraph b) {
		// int[] map = stateSearchMapper.map(a, b);
		int[] map = neighborBiasedMapper.map(a, b);
		double dist = gdis.d(a, b, map, false);
		return dist;
	}
    
    
        class PQNode {
		int nodeId;
		ArrayList<Integer> path;
		ArrayList<String> pathway;
		// String reactant;
		LGraph reactant;
		String reactantName;

		LGraph previousReactant;

		double dist;
		// This works according to previous code.
		// Compute the score for "this reactant" as candidate to add.

		void computeScore(double previousScore) {
			LGraph a = reactantGraph;
			// LGraph b = Index.knownMolecules.get(reactantName);
			// System.out.println("Print graph "+reactantName);
			LGraph b = reactant;
			// System.out.println(b);
			double dist = graphDistanceWrapper(a, b);
			this.dist = previousScore + dist;
		}

		void computeScore2(double previousScore, LGraph previousReactant) {
			// Compute as sum of all scores.
			// LGraph currentReactant = Index.knownMolecules.get(reactantName);

			LGraph currentReactant = reactant;
			double dist = graphDistanceWrapper(reactantGraph, currentReactant);
			double scoreToSub = graphDistanceWrapper(previousReactant, reactantGraph);
			double scoreToAdd = graphDistanceWrapper(previousReactant, currentReactant);
			// double scoreToSub =
			// graphDistanceWrapper(Index.knownMolecules.get(pathway.get(pathway.size()
			// - 1)),
			// reactantGraph);
			// double scoreToAdd =
			// graphDistanceWrapper(Index.knownMolecules.get(pathway.get(pathway.size()
			// - 1)),
			// currentReactant);
			this.dist = previousScore - scoreToSub + scoreToAdd + dist;
			// System.out.println(this.dist);
		}
		void computeScore3(double previousScore, LGraph previousReactant){
			this.dist=previousScore + graphDistanceWrapper(previousReactant, reactant);
		}
	}

	class PQNodeComparator implements Comparator<PQNode> {
		@Override
		public int compare(PQNode n1, PQNode n2) {
			if (n1.dist > n2.dist)
				return 1;
			else if (n1.dist == n2.dist)
				return (n1.path.size() - n2.path.size());
			else
				return -1;
		}
	}

	// PQ Comparator for implementing a max heap.
	class MaxHeapComparator implements Comparator<PQNode> {
		@Override
		public int compare(PQNode n1, PQNode n2) {
			if (n1.dist > n2.dist)
				return -1;
			else if (n1.dist == n2.dist)
				return (n2.path.size() - n1.path.size());
			else
				return 1;
		}
	}

	// Using a triangle inequality for pruning, we stop when score of any node >
	// top of max heap.

	void topkPaths(int K) throws ClassNotFoundException, IOException {
		/*
		 * First, identify the source nodes -> Rules that are applicable on the
		 * product molecule. Identify the applicable signatures and use the
		 * signature to rules mapping to get the nodes.
		 */

		PriorityQueue<PQNode> answerSet = new PriorityQueue<PQNode>(1, new MaxHeapComparator());
		Set<Integer> sourceNodes = new TreeSet<Integer>();
		for (String reactionSignatureString : rrn.getApplicableReactionSignatures().get(productName)) {
			//System.out.println("Sig: "+reactionSignatureString);
			if (rrn.getReactionSignatureRuleMapping().containsKey(reactionSignatureString))
				sourceNodes.addAll(rrn.getReactionSignatureRuleMapping().get(reactionSignatureString));
		}
		//System.out.println(sourceNodes.toString());
		// Not much use in finding destination nodes.
		PriorityQueue<PQNode> PQ = new PriorityQueue<PQNode>(1, new PQNodeComparator());

		// Initialize PQ with source nodes.
		for (int v : sourceNodes) {
			LGraph reactant = ruleApply.applyReactionRule(productGraph, Index.uniqueRuleMap.get(v));
			String currentReactantName = ruleApply.verifyValidity3(reactant);
			if (restrictTrainDatabase == false
					&& (currentReactantName == null || currentReactantName.contentEquals("null"))) {
				currentReactantName = Graph2Smiles.getSmilesString(MolFormatConvert.removeStereoNodes(MolFormatConvert.removeHatoms(reactant)));
			}
            //System.out.println(currentReactantName);
            //System.out.println(reactantGraph+"\n"+productGraph);
			if (currentReactantName != null && !currentReactantName.contentEquals("null")) {
				ArrayList<Integer> path = new ArrayList<>();
				path.add(v);
				ArrayList<String> pathway = new ArrayList<>();
				pathway.add(productName);
				PQNode L = new PQNode();
				L.path = new ArrayList<Integer>(path);
				L.dist = graphDistanceWrapper(productGraph, reactantGraph);
				L.pathway = new ArrayList<String>(pathway);
				// Pathway is initially just the product mol.
				// L.reactant = Index.knownMolecules.get(currentReactantName);
				L.reactant = reactant;
				L.reactantName = new String(currentReactantName);
				// System.out.println("react. name " + currentReactantName);
				// L.previousReactant = productGraph;
				L.computeScore2(L.dist, productGraph); // L.dist is updated here
				pathway.add(currentReactantName);
				L.pathway = new ArrayList<String>(pathway);
				L.nodeId = v;
				PQ.add(L);
			}
		}
		// HelperUtils.printPQ(PQ);
		// Current semantics: we update the score and then add the reactant to
		// the pathway.
		while (PQ.size() != 0) {
			// printPQ(PQ);
			PQNode L = PQ.remove(); // Pop from PQ.
			//System.out.println(
				//	"Explore " + L.path + " " + L.pathway + " " + L.dist + " " + L.reactantName + " " + PQ.size());
			if (L.path.size() > maxLen)
				continue;

			if (L.reactantName.contentEquals(reactantName)
					&& (answerSet.size() < K || L.dist < answerSet.peek().dist)) {
				System.out.println("Found pathway (reverse) " + L.pathway + " " + L.dist + " " + L.path);
				answerSet.add(L);
				continue; // Since we do not extend pathway from here.
			}
			if (answerSet.size() == K && L.dist > answerSet.peek().dist)
				break;

			for (int L_adj : rrn.getAdjacencyList().get(L.path.get(L.path.size() - 1))) {
				// Note : here some rules may not be applicable because of the
				// way we construct the RRN.
                                
				if (!ruleApply.isRuleApplicable(L.reactant, Index.uniqueRuleMap.get(L_adj)))
					continue;
				LGraph currentReactant = ruleApply.applyReactionRule(L.reactant, Index.uniqueRuleMap.get(L_adj));
				String currentReactantName = ruleApply.verifyValidity3(currentReactant);
                //System.out.println(currentReactant);
				if (restrictTrainDatabase == false
						&& (currentReactantName == null || currentReactantName.contentEquals("null")))
					currentReactantName = Graph2Smiles.getSmilesString(MolFormatConvert.removeStereoNodes(MolFormatConvert.removeHatoms(currentReactant)));

				if (currentReactantName != null && !currentReactantName.contentEquals("null")
						&& HelperUtils.sanityCheck(currentReactantName, productName)
						&& HelperUtils.helperReactantCheck(currentReactantName, L_adj, productName)
						&& !L.pathway.contains(currentReactantName)) {
					PQNode M = new PQNode();
					M.reactantName = new String(currentReactantName);
					M.reactant = currentReactant;
					ArrayList<Integer> path = new ArrayList<Integer>(L.path);
					path.add(L_adj);
					ArrayList<String> pathway = new ArrayList<String>(L.pathway);
					M.path = path;
					M.pathway = pathway;
					if(Index.knownMolecules.containsKey(L.reactantName))
						M.computeScore2(L.dist, Index.knownMolecules.get(L.reactantName));
					else
						M.computeScore2(L.dist, L.reactant);
					
					pathway.add(new String(currentReactantName));
					M.pathway = new ArrayList<String>(pathway);
					M.nodeId = L_adj;
					if (answerSet.size() < K)
						PQ.add(M);
					else if (M.dist < answerSet.peek().dist) {
						System.out.println("Popping from answer set -- found better one");
						answerSet.remove();
						PQ.add(M);
					}
				}
			}
		}

		System.out.println("Predicted pathways : ");
		HashMap<String, Double> pathwayToString = new HashMap<>();
		while (answerSet.size() > 0) {
			PQNode node = answerSet.remove();
			ArrayList<String> pathway = new ArrayList<String>(node.pathway);
			Collections.reverse(pathway);
			// System.out.println(pathway + " " + node.dist);
			String str = pathway.toString();

			// printED(pathway);
			if (!pathwayToString.containsKey(str))
				pathwayToString.put(str, node.dist);
			// HelperUtils.printFullPathway(node);
		}
		LinkedHashMap<String, Double> result = HelperUtils.sortHashMapByValues(pathwayToString);
		for (String str : result.keySet()) {
			System.out.println(str + " " + pathwayToString.get(str));
		}
	}
	
	class KnnNode implements Comparable<KnnNode> {
		public int nodeId;
		public String molName;
		public KnnNode child,root;
		public LGraph molGraph;
		public double prodDist,delta_dist,score;
		KnnNode(){
			nodeId=0;
			child=null;
			root=null;
			molName=null;
			molGraph=null;
			delta_dist=0;
			score=Double.MAX_VALUE;
		}
		KnnNode(KnnNode another){
			this.nodeId=another.nodeId;
			this.child=another.child;
			this.root=another.root;
			this.molName=another.molName;
			this.molGraph=another.molGraph;
		}
		KnnNode(KnnNode parent,int id,LGraph thisGraph,LGraph productGraph){
			this.nodeId=id;
			parent.child=this;
			this.root=parent;
			this.molGraph=thisGraph;
			this.delta_dist=parent.delta_dist+graphDistanceWrapper(this.molGraph,parent.molGraph);
			this.prodDist=graphDistanceWrapper(this.molGraph,productGraph);
			this.score=delta_dist;//-prodDist;
		}
		public int compareTo(KnnNode another) {
			if(another.score>this.score)
				return -1;
			if(another.score>this.score)
				return 1;
			else
				return 0;
		}
		
	}
	
	void knn(String productName, int R) // (productName, stepRadius, PQ_size) 
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		this.reactantName = null;
		this.productName = productName;
		//reactantGraph = Index.knownMolecules.get(reactantName);
		reactantGraph=null;
		productGraph = Index.knownMolecules.get(productName);
		this.rrn.driver();
		//this.topkPaths(k);
		Set<Integer> sourceNodes = new TreeSet<Integer>();
		for (String reactionSignatureString : rrn.getApplicableReactionSignatures().get(productName)) {
			//System.out.println("Sig: "+reactionSignatureString);
			if (rrn.getReactionSignatureRuleMapping().containsKey(reactionSignatureString))
				sourceNodes.addAll(rrn.getReactionSignatureRuleMapping().get(reactionSignatureString));
		}
		//System.out.println(sourceNodes.toString());
		// Not much use in finding destination nodes.
		KnnNode productNode=new KnnNode();
		productNode.molName=productName;
		productNode.molGraph=productGraph;
        Queue<KnnNode> pathQueue=new PriorityQueue<KnnNode>();
		// Initialize PQ with source nodes.
        System.out.println("Sourcenodes len = "+sourceNodes.size());
        System.out.println("Searching Neighbours at ");
        System.out.println("1st level....");
        
        float totalmols=0,unknownmols=0;
        
        for(int v : sourceNodes) {
        	LGraph reactant = ruleApply.applyReactionRule(productNode.molGraph, Index.uniqueRuleMap.get(v));
			String currentReactantName = ruleApply.verifyValidity3(reactant);
			if (restrictTrainDatabase == false
					&& (currentReactantName == null || currentReactantName.contentEquals("null"))) {
				currentReactantName = Graph2Smiles.getSmilesString(MolFormatConvert.removeStereoNodes(MolFormatConvert.removeHatoms(reactant)));
		        unknownmols++;	
			}
            //System.out.println(currentReactantName);
            //System.out.println(reactantGraph+"\n"+productGraph);
			if (currentReactantName != null && !currentReactantName.contentEquals("null")) {
	        	KnnNode reacNode=new KnnNode(productNode,v,reactant,productGraph);
				//reacNode.molGraph=reactant;
				reacNode.molName=currentReactantName;
				//reacNode.nodeId=v;
				pathQueue.add(reacNode);
				totalmols++;
			}
        }
		
        for(int level=1;level<R;++level) {
			Queue<KnnNode> tempQueue=new PriorityQueue<KnnNode>();
			ArrayList<KnnNode> candMols=new ArrayList<KnnNode>();
			Set<String> molSet=new HashSet<String>();
			for(int icount=0;icount<10 && pathQueue.size()>0;icount++) {
				candMols.add(pathQueue.poll());
			}
			System.out.println("At level "+(level+1)+" ....");
	        for (KnnNode previous : candMols) {
		        for (int Ladj : rrn.getAdjacencyList().get(previous.nodeId)){
		        	if (!ruleApply.isRuleApplicable(previous.molGraph, Index.uniqueRuleMap.get(Ladj)))
						continue;
		        	
		        	//System.out.println(previous.molName+" "+previous.molGraph+"\n"+Ladj);
					LGraph reactant = ruleApply.applyReactionRule(previous.molGraph, Index.uniqueRuleMap.get(Ladj));
					String currentReactantName = ruleApply.verifyValidity3(reactant);
					if (restrictTrainDatabase == false
							&& (currentReactantName == null || currentReactantName.contentEquals("null"))) {
						currentReactantName = Graph2Smiles.getSmilesString(MolFormatConvert.removeStereoNodes(MolFormatConvert.removeHatoms(reactant)));
						unknownmols++;
					}
		            //System.out.println(currentReactantName);
		            //System.out.println(reactantGraph+"\n"+productGraph);
					if (currentReactantName != null && !currentReactantName.contentEquals("null")) {
						KnnNode reacNode=new KnnNode(previous,Ladj,reactant,productGraph);
						//reacNode.molGraph=reactant;
						reacNode.molName=currentReactantName;
						//reacNode.nodeId=Ladj;
						molSet.add(currentReactantName);
						tempQueue.add(reacNode);
						totalmols++;
					}
		        }
		        System.out.format(" Total:List= "+ totalmols+" Set= "+molSet.size()+" Unknown= "+unknownmols+"  %3.2f percent \n",unknownmols/totalmols*100.0);
		    }
		    pathQueue=tempQueue;
		}
		ArrayList<KnnNode> candMols=new ArrayList<KnnNode>();
        for(int icount=0;icount<10 && pathQueue.size()>0;icount++) {
			candMols.add(pathQueue.poll());
		}		
		System.out.println("Nearest Neighbors are ");
		for(KnnNode nod:candMols ) {
			KnnNode iter;
			for(iter=nod;iter.root!=null;iter=iter.root){
			    System.out.print(iter.molName+" -> ");
				//nod=nod.root;
			}
		    System.out.println(iter.molName);
		}
	}
	
	void driver(String reactantName, String productName, int k)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		this.reactantName = reactantName;
		this.productName = productName;
		reactantGraph = Index.knownMolecules.get(reactantName);
		productGraph = Index.knownMolecules.get(productName);
		this.rrn.driver();
		this.topkPaths(k);
	}
	
	

	void driver(String organismFile, String reactantName, String productName, int k)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		ArrayList<String> reactionIds = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(organismFile));
		String line = "";
		while ((line = br.readLine()) != null)
			reactionIds.add(line);
		br.close();
		this.reactantName = reactantName;
		this.productName = productName;
		reactantGraph = Index.knownMolecules.get(reactantName);
		productGraph = Index.knownMolecules.get(productName);
		this.rrn.driver(reactionIds);
		this.topkPaths(k);
	}

	void printED(ArrayList<String> pathway) {
		double sc = 0.0;
		for (int i = 1; i < pathway.size(); i++)
			sc += graphDistanceWrapper(Index.knownMolecules.get(pathway.get(i - 1)),
					Index.knownMolecules.get(pathway.get(i)));
		System.out.println("Score of pathway - " + pathway + " " + sc);
	}

	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		Heuristic_new heuristic = new Heuristic_new();
		//heuristic.driver("C00118", "C00022", 5);
		heuristic.knn(args[0], 3);
		// heuristic.driver("data/Organism_Dataset/kegg/BMID000000142681.kegg", "C00267", "C00118", 10);
	}
}
