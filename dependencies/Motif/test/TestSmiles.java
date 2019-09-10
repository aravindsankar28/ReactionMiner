package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import joelib2.feature.FeatureException;
import joelib2.feature.result.BitResult;
import joelib2.feature.types.SSKey3DS;
import joelib2.feature.util.SMARTSDescriptors;
import joelib2.gui.render2D.MoleculeViewer2D;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.molecule.Molecule;
import joelib2.smiles.SMILESParser;

import tool.BuildGraph;
import tool.FileUtils;
import Dictionary.argumentsDictionary;
import graph.Graph;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.IOType;
import joelib2.io.MoleculeIOException;
import joelib2.molecule.*;
import joelib2.smarts.SMARTSParser;
import joelib2.smiles.SMILESParser;
import joelib2.util.cdk.CDKTools;
import joelib2.util.iterator.AtomIterator;

public class TestSmiles {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MoleculeIOException 
	 * @throws FeatureException 
	 */
	public static void main(String[] args) throws IOException, MoleculeIOException, FeatureException {
		// TODO Auto-generated method stub
		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SMILES");
		
		argumentsDictionary.set("ca.txt","ca.txt",0.25,0.001,1);
		
		long time=System.currentTimeMillis();
		Molecule mol = new BasicConformerMolecule(inType, inType); 
		//Graph[] graphdb=BuildGraph.loadGraphs("aido99_all.txt");
		/*StringBuffer smartsPattern=new StringBuffer("");
		for(int i=0;i<graphdb.length;i++)
		{
			smartsPattern.append(BuildGraph.graphdb[i].getSmiles()+"\n");
		}
		FileUtils.writeToFile("aido99_all.smiles", smartsPattern.toString());*/
		/*BasicReader reader=new BasicReader("ca.smiles");
		while(reader.readNext(mol))
		{
			int a=1;
			BitResult fingerprint = (BitResult) new SSKey3DS().calculate(mol);
			System.out.println(fingerprint.toString());
			//MoleculeViewer2D.display(mol);
		}
		System.err.println("Total Time: "+(System.currentTimeMillis()-time));*/
		MoleculeViewer2D viewer=new MoleculeViewer2D();
		SMILESParser.smiles2molecule(mol, "c1ccccc1", "Benzene");
		//viewer.display(mol);
		CDKTools.generate2D(mol);
		BitResult fingerprint = (BitResult) new SSKey3DS().calculate(mol);
		System.out.println(fingerprint.toString());
	}

}
