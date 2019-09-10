package Harvard;

import java.util.Comparator;

public class RankedDecision implements Comparable{

	String id;
	float score;
	
	public RankedDecision(String id, float score)
	{
		this.id=id;
		this.score=score;
	}


	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		RankedDecision a1=(RankedDecision)arg0;
		
		if (this.score>a1.score)
			return -1;
		else if (this.score<a1.score)
			return 1;
		else
			return id.compareTo(a1.id);
	}
}
