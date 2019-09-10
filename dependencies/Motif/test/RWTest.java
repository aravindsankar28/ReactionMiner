package test;

import grank.transform.Hist;
import graph.Graph;

import java.io.FileNotFoundException;
import java.util.Vector;

import tool.BuildGraph;
import tool.RandomWalkBySet;
import tool.RandomWalkV3;
import Dictionary.argumentsDictionary;

public class RWTest {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		argumentsDictionary.set("chemrw.txt","chemrw.txt",0.25,0.001,1);
		Graph[] graphdb=BuildGraph.loadGraphs("chemrw.txt");
		//vertexDictionary.print();
		Vector<Hist> buf = new Vector<Hist> ();
		double time=System.currentTimeMillis();
		RandomWalkV3 r=new RandomWalkV3(argumentsDictionary.restart);
		r.getHistsBySet(graphdb);
	}

}
