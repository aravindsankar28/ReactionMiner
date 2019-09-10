package ruleMining.RPM;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author aravind
 * Stores the RPM - RPAIR + mapping information along with a unique id for the RPM.
 * 
 */
public class RPM implements Serializable{
	RPAIR rpair;
	ArrayList<Integer> mapping;
	Integer id;
	
	public ArrayList<Integer> getMapping() {
		return mapping;
	}
	
	public RPM(RPAIR reactantProductPair) {
		this.rpair = reactantProductPair;
		this.mapping = new ArrayList<Integer>();
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setMapping(ArrayList<Integer> mapping) {
		this.mapping = mapping;
	}
	public RPAIR getRpair() {
		return rpair;
	}
}