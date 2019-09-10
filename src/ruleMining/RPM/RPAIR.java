package ruleMining.RPM;

import java.io.Serializable;

/**
 * 
 * @author aravind Class for storing the RPAIR information - reactant, product,
 *         reaction that it was extracted from. Reactant size, product size.
 *         Finally, a distance attribute which is used during RPM computation -
 *         this is either sed or ed.
 */
public class RPAIR implements Serializable {

	private static final long serialVersionUID = 1L;
	String reactant;
	String product;
	Reaction reaction;

	int reactantSize;
	int productSize;

	double distance;

	public String getReactant() {
		return reactant;
	}

	public int getReactantSize() {
		return reactantSize;
	}

	public String getProduct() {
		return product;
	}

	public Reaction getReaction() {
		return reaction;
	}

	public double getDistance() {
		return distance;
	}

	public int getProductSize() {
		return productSize;
	}

	public String toString() {
		return reactant + " " + product + " " + reaction;
	}

	public RPAIR(String reactant, String product, Reaction reaction) {
		this.reactant = reactant;
		this.product = product;
		this.reaction = reaction;
		this.distance = Double.MAX_VALUE;
		this.reactantSize = 0;
		this.productSize = 0;
	}

	public RPAIR() {
		this.reactant = "";
		this.product = "";
		this.reaction = null;
		this.distance = Double.MAX_VALUE;
		this.reactantSize = 0;
		this.productSize = 0;
	}
}