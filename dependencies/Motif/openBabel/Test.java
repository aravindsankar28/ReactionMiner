package openBabel;

import java.io.IOException;
import java.util.Vector;

import tool.FileUtils;
import Dictionary.vertexDictionary;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.IOType;
import joelib2.io.MoleculeIOException;
import joelib2.molecule.*;
import joelib2.smarts.SMARTSParser;
import joelib2.smiles.SMILESParser;
import joelib2.util.iterator.AtomIterator;
import joelib2.util.iterator.BondIterator;


public class Test {

	/**
	 * @param args
	 * @throws MoleculeIOException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, MoleculeIOException {
		// TODO Auto-generated method stub
		 /*
		 String smartsPattern="c1ccccc1";
		 Molecule mol=null;
		 BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SDF");
		 mol.setInputType(inType);
		 String smiles="c1cc(OH)cc1";
		 mol.setTitle("something");
		 //mol.
	        if (!SMILESParser.smiles2molecule(mol, smiles, "Name:" + smiles))
	        {
	            System.err.println("Molecule could not be generated from \"" +
	                smiles + "\".");
	        }
		 System.out.println(mol.toString());
		*/

		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SDF");
		Molecule mol = new BasicConformerMolecule(inType, inType); 
		BasicReader reader=new BasicReader("part1.sdf");
		int i=0;
		int numMol=0;
		while(reader.readNext(mol))
		{
			numMol++;
			int numAtoms=0;
			int numEdges=0;
			StringBuffer atomsRep=new StringBuffer("");
			StringBuffer edgeRep=new StringBuffer("");

			AtomIterator at=mol.atomIterator();
			BondIterator bt=mol.bondIterator();
			//Vector atoms=new Vector<String>();
			while(at.hasNext())
			{
				Atom a=at.nextAtom();
				atomsRep.append(a.toString()+"\n");
				numAtoms++;
			}
			
			while(bt.hasNext())
			{
				Bond b=bt.nextBond();
				int bi=b.getBeginIndex();
				int ei=b.getEndIndex();
				int bo=b.getBondOrder();
				edgeRep.append(bi+" "+ei+" "+bo+"\n");
				numEdges++;
			}
			//for (mol.)
			//System.out.println("read");
			System.out.println("#"+numMol+"\n"+numAtoms+"\n"+atomsRep.toString()+numEdges+"\n"+edgeRep.toString()+"------------\n");
			
		}

		//FileUtils.deleteFile(fileName);
		//return moldb;
	}

}
