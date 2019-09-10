package openBabel;

import java.io.FileNotFoundException;
import java.io.IOException;

import tool.FileUtils;
import Dictionary.vertexDictionary;
import graph.Graph;
import joelib2.feature.types.LogP;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.MoleculeIOException;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Molecule;

public class MoleculeLoader{

	public static Molecule[] moldb;

	public static Molecule[] load(Graph[] graphdb) throws IOException, MoleculeIOException {
		// TODO Auto-generated method stub
		moldb=new Molecule[graphdb.length];
		StringBuffer smartsPattern=new StringBuffer("");
		String fileName="smiledb.smiles";
		for(int i=0;i<graphdb.length;i++)
		{
			if(!vertexDictionary.labels.get(graphdb[i].V[0].label).equals("a"))
				smartsPattern.append(graphdb[i].getSmiles().getSmileRep()+" "+graphdb[i].id+"\n");
		}
		String smilesFileName=fileName.substring(0,fileName.lastIndexOf("."))+".smiles";
		
		FileUtils.writeToFile(smilesFileName, smartsPattern.toString());

		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SMILES");
		Molecule mol = new BasicConformerMolecule(inType, inType); 
		BasicReader reader=new BasicReader(smilesFileName);
		int i=0;
		while(reader.readNext(mol))
		{
			
			moldb[i++]=(Molecule)mol.clone();			
		}

		//FileUtils.deleteFile(fileName);
		return moldb;
	}
}
