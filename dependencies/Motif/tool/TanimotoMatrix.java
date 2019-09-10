package tool;



import grank.transform.Hist;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import ctree.util.Opt;

import joelib2.feature.FeatureException;
import joelib2.io.MoleculeIOException;
import openBabel.Fingerprints;
import Dictionary.argumentsDictionary;

public class TanimotoMatrix {

	/**
	 * @param args
	 * @throws MoleculeIOException 
	 * @throws IOException 
	 * @throws FeatureException 
	 */
	public static void main(String[] args) throws FeatureException, IOException, MoleculeIOException {
		// TODO Auto-generated method stub
	    Opt opt = new Opt(args);

	    argumentsDictionary.set(opt.getArg(0),opt.getArg(0),-1,-1,-1);
		Hist[] background=Fingerprints.loadHistsFromFingerPrintsAsBits(argumentsDictionary.graph);
	    String fingerprint_fileName=argumentsDictionary.graph.substring(0,argumentsDictionary.graph.lastIndexOf("."))+".fpt";
	    //Hist[] background=Fingerprints.readFingerPrints(argumentsDictionary.graph);
		int size=new Double(0.3*FileUtils.getFileContents(opt.getArg(1)).trim().split("\n").length).intValue();
		Hist[][] classIDs=new Hist[opt.args()-1][];
		for(int i=1;i<opt.args();i++)
		{
			argumentsDictionary.set(opt.getArg(0),opt.getArg(i),-1,-1,-1);
			classIDs[i-1] = getSubset(background,argumentsDictionary.ids,size);
		}
		int minSize=Integer.MAX_VALUE;
		for(int i=0;i<classIDs.length;i++)
		{
			minSize=Math.min(minSize,classIDs[i].length );
			System.out.println(i+": "+classIDs[i].length);
		}
		Hist[] DB=new Hist[minSize*classIDs.length];
		int c=0;
		for(int i=0;i<minSize;i++)
		{
			for(int j=0;j<classIDs.length;j++)
				DB[c++]=classIDs[j][i];
		}
		/*List l=Arrays.asList(DB);
		Collections.shuffle(l);
		DB=(Hist[])l.toArray(new Hist[1]);&/
		/*double[][] matrix=new double[DB.length][DB.length];
		for(int i=0;i<DB.length;i++)
		{
			for(int j=0;j<DB.length;j++)
			{
			//	double sim=tanimotoDistance(DB[i].hist,DB[j].hist);
			//	double sim=L1(DB[i].hist,DB[j].hist);
				double sim=linear(DB[i].hist,DB[j].hist);
				double dist=0;
				if(sim==0)
					dist=54;
				else
					dist=Math.min(1/sim,54);
				
				matrix[i][j]=sim;
			}
		}
		*/
		StringBuffer fp=new StringBuffer("");
		for(int i=0;i<DB.length;i++)
		{
			fp.append(""+i%classIDs.length+" ");
			for(int j=0;j<DB[i].hist.length;j++)
				fp.append((j+1)+":"+DB[i].hist[j]+" ");
			fp.append("\n");
		}
		FileUtils.writeToFile( opt.getArg(0).substring(0,opt.getArg(0).lastIndexOf('/')+1)+"fingerprint.txt",fp.toString());
		/*StringBuffer s=new StringBuffer("");
		for(int i=0;i<matrix.length;i++)
		{
			s.append(""+(i%2==0?1:-1)+" ");
			for(int j=0;j<matrix.length;j++)
				s.append(j+":"+matrix[i][j]+" ");
			s.append("\n");
		}
		FileUtils.writeToFile( opt.getArg(0).substring(0,opt.getArg(0).lastIndexOf('/')+1)+"tanimotoMatrix.txt",s.toString());
		*/
	}

	static double tanimotoSimilarity(int[] hist1, int[] hist2) {
		// TODO Auto-generated method stub
		double min=0;
		double max=0;
		for(int i=0;i<hist1.length;i++)
		{
			min+=Math.min(hist1[i], hist2[i]);
			max+=Math.max(hist1[i], hist2[i]);
			
		}
		return max==0?0:min/max;
		//return min/max;
	}
	
	public static double tanimotoSimilarity(boolean[] hist1, boolean[] hist2) {
		// TODO Auto-generated method stub
		int min=0;
		int max=0;
		for(int i=0;i<hist1.length;i++)
		{
			min+=(hist1[i] && hist2[i])?1:0;
			max+=(hist1[i] || hist2[i])?1:0;
			
		}
		return max==0?0:min/max;
		//return min/max;
	}
	private static double L1(int[] hist1, int[] hist2) {
		// TODO Auto-generated method stub
		double dist=0;
		
		for(int i=0;i<hist1.length;i++)
		{
			dist+=Math.abs(hist1[i]- hist2[i]);
			
		}
		//return min==0?1:1-min/max;
		return dist;
	}
	private static double linear(int[] hist1, int[] hist2) {
		// TODO Auto-generated method stub
		double dist=0;
		
		for(int i=0;i<hist1.length;i++)
		{
			dist+=(hist1[i]* hist2[i]);
			
		}
		//return min==0?1:1-min/max;
		return dist;
	}

	private static Hist[] getSubset(Hist[] background, String ids, int size) {
		// TODO Auto-generated method stub
		String[] list=FileUtils.getFileContents(ids).trim().split("\n");
		List l=Arrays.asList(list);
		//Collections.shuffle(l);
		list=(String[])l.toArray(new String[1]);
	
		Vector<Hist> DB=new Vector<Hist>();
		HashMap m=new HashMap();
		for(int i=0;i<size;i++)
		{
			m.put(list[i].trim(), 1);
		}

		for(int i=0;i<background.length;i++)
		{
			if(m.containsKey(background[i].id.trim().split("-")[0]))
			{
					//System.out.println(i);
					DB.add(background[i]);
			}
		}
		return (Hist[]) DB.toArray(new Hist[0]);
	}

	public static double tanimotoSimilarity(int[] hist1, double[] hist2) {
		// TODO Auto-generated method stub
		double min=0;
		double max=0;
		for(int i=0;i<hist1.length;i++)
		{
			min+=Math.min(hist1[i],hist2[i]);
			max+=Math.max(hist1[i],hist2[i]);
			
		}
		return max==0?0:min/max;
		//return min/max;
	}

	public static double tanimotoDistance(int[] hist1, double[] hist2) {
		// TODO Auto-generated method stub
		return 1-tanimotoSimilarity(hist1, hist2);
	}

	public static double tanimotoDistance(int[] hist1, int[] hist2) {
		// TODO Auto-generated method stub
		return 1-tanimotoSimilarity(hist1, hist2);
	}

	public static double tanimotoDistance(double[] hist1, double[] hist2) {
		// TODO Auto-generated method stub
		return 1-tanimotoSimilarity(hist1, hist2);
		
	}

	public static double tanimotoSimilarity(double[] hist1, double[] hist2) {
		// TODO Auto-generated method stub
		double min=0;
		double max=0;
		for(int i=0;i<hist1.length;i++)
		{
			min+=Math.min(hist1[i],hist2[i]);
			max+=Math.max(hist1[i],hist2[i]);
			
		}
		//System.out.println("Min:"+ (min));
		//System.out.println("Max:"+ (max));
		//System.out.println("Tan:"+ (max==0?0:min/max));
		return max==0?0:min/max;
	}
	

}
