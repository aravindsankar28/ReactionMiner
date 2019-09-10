package ruleMining.ReactionRule;

import java.io.Serializable;
import java.util.ArrayList;

import ctree.graph.Edge;

public class RuleMetaData implements Serializable{
	/**
	 * Connecting edges from subgraphRemoved (to RCs) and inter RC edge changes.
	 */
	private static final long serialVersionUID = 7899834036751546342L;
	private ArrayList<Edge> connectingEdges;
	ArrayList<InterRCEdge> interRCEdges;
	
	RuleMetaData() {
		setConnectingEdges(new ArrayList<Edge>());
		interRCEdges = new ArrayList<InterRCEdge>();	
	}
	public ArrayList<Edge> getConnectingEdges() {
		return connectingEdges;
	}

	public ArrayList<InterRCEdge> getInterRCEdges() {
		return interRCEdges;
	}

	public void setConnectingEdges(ArrayList<Edge> bondsToAttach) {
		this.connectingEdges = bondsToAttach;
	}

}