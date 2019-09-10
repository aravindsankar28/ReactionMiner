package Dictionary;

import java.util.Comparator;

public class atomComparator implements Comparator{

	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		Atom a1=(Atom)o1;
		Atom a2=(Atom)o2;
		if (a1.freq<a2.freq)
			return 1;
		else
			if((a1.freq>a2.freq))
				return -1;
			else
				return 0;
	}


	

}
