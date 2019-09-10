package tool;

import grank.transform.Hist;
import graph.Graph;

import java.util.HashMap;

import Dictionary.argumentsDictionary;
import Dictionary.edgeDictionary;
import Dictionary.vertexDictionary;

public class RandomWalk {
	double restart;
	public static HashMap topEdges;
	public RandomWalk(double restart)
	{
		this.restart=restart;
		topEdges=topk(argumentsDictionary.topAtoms);
	}
	private HashMap topk(int k) {
		// TODO Auto-generated method stub
		//HashMap topAtoms=vertexDictionary.getTop(k);
		HashMap topEdges=edgeDictionary.getEdgesOf(vertexDictionary.getTop(k));
		return topEdges;
	}
	public int[][] walk(Graph g)
	{
		return null;
	}

	int[][] approximate(double[][] profile, int approx) {
		// TODO Auto-generated method stub
		int[][] appProfile=new int[profile.length][profile[0].length];
		for(int i=0;i<profile.length;i++)
		{
			for(int j=0;j<profile[i].length;j++)
			{
						appProfile[i][j]=(int)Math.round(profile[i][j]*approx);
			}
		}
		return appProfile;
	}
	int[] approximate(double[] profile, int approx) {
		// TODO Auto-generated method stub
		int[] appProfile=new int[profile.length];
		for(int i=0;i<profile.length;i++)
		{
			appProfile[i]=(int)Math.round(profile[i]*approx);
			if(appProfile[i]>10)
				System.out.println(appProfile[i]);
		}
		return appProfile;
	}
	double L1Dist(double[] origProfile, double[] profile,int origSize, int currSize) {
		// TODO Auto-generated method stub
		double diff=0;
		if (origSize==0)
			return 100;
		for(int i=0;i<profile.length;i++)
			diff+=Math.abs(origProfile[i]/origSize-profile[i]/currSize);
		return diff;
	}


}
