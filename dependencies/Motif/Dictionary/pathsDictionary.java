package Dictionary;

import java.util.HashMap;
import java.util.Vector;

import tool.BuildGraph;

public class pathsDictionary {
	public static HashMap types=new HashMap();
	//public static int size=0;
	//static Vector freq=new Vector();
	public static int getID(String label)
	{
		Integer val1=(Integer)types.get(label);
		String rev=new StringBuffer(label).reverse().toString();
		Integer val2=(Integer)types.get(rev);
		//val2=null;
		if(val1!=null && val2!=null && rev.equals(label)==false)
		{
			System.out.println("ERROR");
			return -1;
		}
		if(val1!=null || val2!=null)
		{
			if(val1!=null)
				return val1;
			if(val2!=null)
			{
					//System.out.println("val2");
					return val2;		
			}
		}
		else
			types.put(label, types.size());
		return types.size()-1;
	}
}
