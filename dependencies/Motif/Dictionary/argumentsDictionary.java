package Dictionary;

import java.io.File;

public class argumentsDictionary {

	static public String graph;
	static public String subHistDir;
	static public double restart;
	static public int topAtoms;
	static public int radius;
	static public String ids;
	public static double delta;
	public static String loadTopAtoms="";
	public static void set(String graph1,String ids1,double restart1,double delta1, int topAtoms1)
	{
		graph=graph1;
		ids=ids1;
		subHistDir=ids.substring(0,ids.lastIndexOf("."))+"/subHist/";
		System.out.println("Making dir: "+new File(subHistDir+"fp").mkdirs());
		restart=restart1;
		radius=(int) (3/restart);
		topAtoms=topAtoms1;
		delta=delta1;
		System.out.println(getSummary());
	}

	private static String getSummary() {
		// TODO Auto-generated method stub
		return(
		"restart: "+restart+
		"\nradius: "+radius+
		"\ngraph: "+graph+
		"\nsubHistDir: "+subHistDir+
		"\nID List: "+ids+
		"\nNumber of Top Atoms: "+topAtoms);
	}
	
}
