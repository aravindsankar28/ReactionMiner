package ruleMining.RPM;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author aravind 
 * Class defining a reaction class. List of reactant names,
 * product names and reaction ids.
 */
public class Reaction implements Serializable {
	ArrayList<String> reactants;
	ArrayList<String> products;
	String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<String> getReactants() {
		return reactants;
	}

	public ArrayList<String> getProducts() {
		return products;
	}

	public Reaction() {
		this.reactants = new ArrayList<String>();
		this.products = new ArrayList<String>();
	}

	public void setReactants(ArrayList<String> reactants) {
		this.reactants = reactants;
	}

	public void setProducts(ArrayList<String> products) {
		this.products = products;
	}

	public String toString() {
		return this.id + " " + reactants.toString() + " ==" + products.toString();
	}

	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this)
			return true;

		if (!(o instanceof Reaction))
			return false;

		Reaction c = (Reaction) o;
		if (c.reactants.size() != reactants.size())
			return false;

		if (c.products.size() != products.size())
			return false;

		for (int i = 0; i < c.reactants.size(); i++) {
			String r1 = c.reactants.get(i);
			if (!reactants.contains(r1))
				return false;
		}
		
		if (c.products.size() != products.size())
			return false;

		for (int i = 0; i < c.products.size(); i++) {
			String r1 = c.products.get(i);
			if (!products.contains(r1))
				return false;
		}
		return true;
	}
}
