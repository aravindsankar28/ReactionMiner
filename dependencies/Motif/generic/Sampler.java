package generic;

import graph.Graph;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

import ctree.util.Opt;

import tool.BuildGraph;
import tool.FileUtils;

public class Sampler {

	public static Vector<String> sample(String[] ids,int size)
	{
		Vector<String> sampled=new Vector<String>();
		double ratio=(1.0*size/ids.length);
		for (String id:ids)
		{
			double r=Math.random();
			if (r<=ratio)
				sampled.add(id);
		}
		return sampled;
	}
	public static Vector<Graph> selectGraphs(Graph[] graphDB,Vector<String> posIDS,Vector<String> negIDS)
	{
		HashMap<String,Integer> id=new HashMap();
		Vector<Graph> selected=new Vector<Graph>();
		for (String s:posIDS)
			id.put(s.replace("-", "_"),1);
		for (String s:negIDS)
			id.put(s.replace("-", "_"),1);
		for (Graph g:graphDB)
		{
			//System.out.println(g.id);
			if(id.get(g.id)!=null)
				selected.add(g);
		}
		return selected;
		
	}
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		String graphFile=args[0];
		String posFile=args[1];
		String negFile=args[2];
		Opt opt=new Opt(args);
		double testRatio=opt.getDouble("testingRatio",0.5);
		double trainRatio=opt.getDouble("trainingRatio",0.5);
		int trainingSize=opt.getInt("trainingSize");
		int testingSize=opt.getInt("testingSize");
		Vector<String> trainPosIDS=sample(FileUtils.getFileContents(posFile).trim().split("\n"),(int)(trainRatio*trainingSize));
		Vector<String> trainNegIDS=sample(FileUtils.getFileContents(negFile).trim().split("\n"),(int)((1-trainRatio)*trainingSize));
		Vector<String> testPosIDS=sample(FileUtils.getFileContents(posFile).trim().split("\n"),(int)(testRatio*testingSize));
		Vector<String> testNegIDS=sample(FileUtils.getFileContents(negFile).trim().split("\n"),(int)((1-testRatio)*testingSize));
		
		Graph[] graphDB=BuildGraph.loadGraphs(graphFile);
		Vector<Graph> trainingGraphs=selectGraphs(graphDB, trainPosIDS,trainNegIDS);
		Vector<Graph> testingGraphs=selectGraphs(graphDB, testPosIDS,testNegIDS);
		StringBuffer training=new StringBuffer("");
		for(Graph g:trainingGraphs)
		{
			training.append(g.toString());
		}
		StringBuffer testing=new StringBuffer("");
		for(Graph g:testingGraphs)
		{
			testing.append(g.toString());
		}
		StringBuffer trainingPosIDS=new StringBuffer("");
		for(String s:trainPosIDS)
		{
			trainingPosIDS.append(s+"\n");
		}
		StringBuffer trainingNegIDS=new StringBuffer("");
		for(String s:trainNegIDS)
		{
			trainingNegIDS.append(s+"\n");
		}
		StringBuffer testingPosIDS=new StringBuffer("");
		for(String s:testPosIDS)
		{
			testingPosIDS.append(s+"\n");
		}
		StringBuffer testingNegIDS=new StringBuffer("");
		for(String s:testNegIDS)
		{
			testingNegIDS.append(s+"\n");
		}				
		String path=args[0].substring(0,args[0].lastIndexOf("."));
		FileUtils.writeToFile(path+"_train.txt", training.toString());
		FileUtils.writeToFile(path+"_test.txt", testing.toString());
		FileUtils.writeToFile(path+"_trainActive.txt", trainingPosIDS.toString());
		FileUtils.writeToFile(path+"_trainInactive.txt", trainingNegIDS.toString());
		FileUtils.writeToFile(path+"_testActive.txt", testingPosIDS.toString());
		FileUtils.writeToFile(path+"_testInactive.txt", testingNegIDS.toString());
	}
}
