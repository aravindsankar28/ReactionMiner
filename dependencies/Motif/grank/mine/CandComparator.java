package grank.mine;

import java.util.Comparator;

public class CandComparator implements Comparator{

	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		Cand a1=(Cand)o1;
		Cand a2=(Cand)o2;
		if (a1.c[0]>a2.c[0])
			return 1;
		else
			if((a1.c[0]<a2.c[0]))
				return -1;
			else
				return 0;
	}


	

}
