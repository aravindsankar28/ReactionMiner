package ruleMining.ReactionRule;

import java.io.Serializable;

public class InterRCEdge implements Serializable {
	private static final long serialVersionUID = -8324248191463088054L;
	/**
	 * Inter RC edge changes defintion.
	 */

	int v1;
	int v2;
	int w;
	String stereo;

	char type; // Remove/Join

	public int getV1() {
		return v1;
	}

	public int getV2() {
		return v2;
	}

	public int getW() {
		return w;
	}

	public char getType() {
		// Join/Remove
		return type;
	}

	public String getStereo() {
		return stereo;
	}

	public InterRCEdge(int v1, int v2, int w, String stereo, char type) {
		super();
		this.v1 = v1;
		this.v2 = v2;
		this.w = w;
		this.stereo = stereo;
		this.type = type;
	}

	public String toString() {
		return this.v1 + " " + this.v2 + " " + this.w + " " + this.type;
	}
}
