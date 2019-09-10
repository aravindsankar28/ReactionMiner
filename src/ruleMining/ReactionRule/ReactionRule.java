package ruleMining.ReactionRule;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import globals.Globals;

import ctree.graph.*;
import ctree.lgraph.*;

public class ReactionRule implements Serializable {
	private static final long serialVersionUID = -557866834072004869L;
	/**
	 * Reaction Rule class definition
	 */
	private Integer id;
	private Graph reactionSignatureAdded; // This is defined as signature +
											// subgraphAdded.
	private String reactionSignatureAddedString; // This is the canonical label.  
	private ArrayList<Integer> reactionCentersReactant;
	private ArrayList<Integer> reactionCentersProduct;
	private Graph subgraphRemoved;
	private ArrayList<Integer> subgraphAdded; // subgraphAdded - enough to store
												// the nodes.
	RuleMetaData metaData;

	private ArrayList<String> helperReactants;
	private ArrayList<String> helperProducts;

	public Graph getReactionSignatureAdded() {
		return reactionSignatureAdded;
	}

	public Graph getSubgraphRemoved() {
		return subgraphRemoved;
	}

	public ArrayList<Integer> getSubgraphAdded() {
		return subgraphAdded;
	}

	public void setSubgraphAdded(ArrayList<Integer> nodesToRemove) {
		this.subgraphAdded = nodesToRemove;
	}

	public ArrayList<String> getHelperReactants() {
		return helperReactants;
	}

	public ArrayList<String> getHelperProducts() {
		return helperProducts;
	}

	public RuleMetaData getMetaData() {
		return metaData;
	}

	public ArrayList<Integer> getReactionCentersReactant() {
		return reactionCentersReactant;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setReactionCentersReactant(ArrayList<Integer> reactionCenters) {
		this.reactionCentersReactant = reactionCenters;
	}

	public void setSubgraphRemoved(Graph reactantDiffProduct) {
		this.subgraphRemoved = reactantDiffProduct;
	}

	public void setReactionSignatureAdded(Graph reactionSignature) {
		this.reactionSignatureAdded = reactionSignature;
	}

	public void setHelperReactants(ArrayList<String> helperReactants) {
		this.helperReactants = helperReactants;
	}

	public void setHelperProducts(ArrayList<String> helperProducts) {
		this.helperProducts = helperProducts;
	}

	public ArrayList<Integer> getReactionCentersProduct() {
		return reactionCentersProduct;
	}

	public void setReactionCentersProduct(ArrayList<Integer> reactionCentersProduct) {
		this.reactionCentersProduct = reactionCentersProduct;
	}

	public ReactionRule() {
		setReactionSignatureAdded(new LGraph());
		setReactionSignatureAddedString("");
		setSubgraphRemoved(new LGraph());
		metaData = new RuleMetaData();
		setReactionCentersReactant(new ArrayList<Integer>());
		setHelperReactants(new ArrayList<String>());
		setHelperProducts(new ArrayList<String>());
		reactionCentersProduct = new ArrayList<>();
	}

	// Helper function to dump outputs.
	public void dumpToFiles() throws IOException {
		// store sign.
		LGraph[] reactionSignatureArray = new LGraph[1];
		reactionSignatureArray[0] = (LGraph) this.getReactionSignatureAdded();
		LGraphFile.saveLGraphs(reactionSignatureArray, Globals.ruleDirectory + "/" + this.getId() + "post.mol");

		// store drp
		LGraph[] diffReactantProductGraph = new LGraph[1];
		diffReactantProductGraph[0] = (LGraph) this.getSubgraphRemoved();
		LGraphFile.saveLGraphs(diffReactantProductGraph, Globals.ruleDirectory + "/" + this.getId() + "drp");

		// store meta
		FileWriter fw = new FileWriter(Globals.ruleDirectory + "/" + this.getId() + "meta");

		ArrayList<String> bondsToAttach = new ArrayList<String>();

		ArrayList<String> interRCEdges = new ArrayList<String>();

		for (Edge e : this.metaData.getConnectingEdges()) {
			String str = e.v1() + " " + e.v2() + " " + e.w();
			bondsToAttach.add(str);
		}

		for (InterRCEdge bond : this.metaData.getInterRCEdges()) {
			String str = "";
			if (bond.type == 'R') {
				str = "R " + bond.v1 + " " + bond.v2;
			}

			if (bond.type == 'J') {
				str = "J " + bond.v1 + " " + bond.v2 + " " + bond.w;
			}
			interRCEdges.add(str);
		}
		fw.write(StringUtils.join(this.getSubgraphAdded(), ",") + "\n");
		fw.write(StringUtils.join(bondsToAttach, ",") + "\n");
		fw.write(StringUtils.join(interRCEdges, ",") + "\n");

		fw.close();

	}

	public String getReactionSignatureAddedString() {
		return reactionSignatureAddedString;
	}

	public void setReactionSignatureAddedString(String reactionSignatureAddedString) {
		this.reactionSignatureAddedString = reactionSignatureAddedString;
	}

}
