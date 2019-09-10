package Dictionary;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import tool.BuildGraph;

public class edgeDictionary {
	public static HashMap<Integer,String> types=new HashMap<Integer,String>();
	public static int size=0;
	public static Vector freq=new Vector();
	public static int addID(String label)
	{
		
		for(int i=0;i<types.size();i++)
		{
			if(label.equals((String)types.get(i)))
			{
				int currVal=(Integer)(freq.elementAt(i));
				if(BuildGraph.pass==1)	
					freq.set(i, currVal+1);
				return i;
			}
		}
		types.put(types.size(), label);
		freq.add(1);
		return types.size()-1;
	}
	
	public static void print()
	{
		System.out.println(types.toString());
		System.out.println(freq);
	}

	public static HashMap getEdgesOf(HashMap topAtoms) {
		// TODO Auto-generated method stub
		HashMap topEdges=new HashMap();
		int c=0;
		for(int i=0;i<types.size();i++)
		{
			String label=(String)types.get(i);
			String[] parts=label.split("-");
			int node1=new Integer(parts[0]);
			int node2=new Integer(parts[1]);
			if((Integer)topAtoms.get(node1)!=null && (Integer)topAtoms.get(node2)!=null)
			{
				//System.out.print(vertexDictionary.labels.get(node1));
				//System.out.print("-"+vertexDictionary.labels.get(node2));
				//System.out.println("-"+label);
				topEdges.put(i, c++);
			}
		}
		//System.out.println(topEdges.toString());
		return topEdges;
	}
}
