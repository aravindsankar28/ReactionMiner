package graph;

import java.util.Arrays;
import java.util.HashMap;

import openBabel.MoleculeLoader;

import joelib2.feature.types.atomlabel.AtomType;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.molecule.Atom;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Molecule;
import joelib2.smiles.SMILESParser;
import joelib2.util.iterator.AtomIterator;

import Dictionary.vertexDictionary;

public class Smile {

	static HashMap organicSubset=null;

	static AtomType at=new AtomType();
	int cycle;
	int pos;
	StringBuffer[] smiles;
	int[] found;
	boolean[] traversed;
	Graph g;
	
	public Smile(Graph g)
	{
		if(organicSubset==null)
		{
			organicSubset=new HashMap();
			organicSubset.put("B", 1);
			organicSubset.put("C", 1);
			organicSubset.put("N", 1);
			organicSubset.put("O", 1);
			organicSubset.put("P", 1);
			organicSubset.put("S", 1);
			organicSubset.put("F", 1);
			organicSubset.put("Cl", 1);
			organicSubset.put("Br", 1);
			organicSubset.put("I", 1);
		}
		this.g=g;
		cycle=1;
		found=new int[g.V.length];
		Arrays.fill(found, -1);
		this.smiles=new StringBuffer[g.V.length];
		traversed=new boolean[g.E.length];
		explore(0, -1, -1);
		for (int i=1;i<g.V.length;i++)
		{
			if(found[i]==-1)
			{
				//System.out.println("Index: "+i);
				smiles[pos-1].append(".");
				explore(i, -1, -1);
				
			}
		}		
	}
	
	/*
	 * Depth first exploration of the graph to build the Smiles representation
	 */
	int explore(int index, int last, int currEdge)
	{
		
		Vertex v=g.V[index];
		if(currEdge!=-1)
				if(traversed[currEdge])
					return -1;
		
		
		if(found[index]==-1)
		{
			String bond;
			if(currEdge==-1 || g.E[currEdge].bond==1)
				bond="";
			else if (g.E[currEdge].bond==3)
				bond="#";
			else
				bond="=";
		
			String label=(String)vertexDictionary.labels.get(g.V[index].label);
			label=new StringBuffer(label).replace(0,1, (label.charAt(0)+"").toUpperCase()).toString();
			if(!organicSubset.containsKey(label))
				label="["+label+"]";
			
			smiles[pos]=new StringBuffer(bond+label);
			found[index]=pos++;
			
			for (int j=0;j<v.edges.size();j++)
			{
				int selected=v.edges.elementAt(j);
				int neighbor=g.E[selected].getNeighbor(index);
				if (last!=neighbor)
				{	
					int start=pos;
					if (explore(neighbor,index, selected)!=-1)	
					{
						//System.out.println(index+","+last+","+neighbor);
						smiles[start].insert(0,"(");
						smiles[pos-1].append(")");
					}
					traversed[selected]=true;
				}
			}
			return 0;
		}
		else
		{
			int position=smiles[found[index]].indexOf("(");
			if (position==-1)
				position=smiles[found[index]].length();
			smiles[found[index]].insert(position,cycle);
			position=smiles[pos-1].indexOf("(");
			if (position==-1)
				position=smiles[pos-1].length();

			smiles[pos-1].insert(position,cycle++);
			return -1;
			
		}
		
	}
	
	public String getSmileRep()
	{
		StringBuffer smileRep=new StringBuffer("");
		for(int i=0;i<smiles.length;i++)
		{
			smileRep.append(smiles[i].toString());
		}
		//smileRep.append(" "+g.id);
		return smileRep.toString();
	}
	
	public int[] getOrdering()
	{
		return found;
	}

	public void relabelAtoms() {
		// TODO Auto-generated method stub
		Molecule mol=MoleculeLoader.moldb[g.index];
		at.setAtomType(mol.getFirstAtom(), "SYB");
		if(mol.getAtomsSize()!=found.length)
		{
			System.err.println("-----Exception----------");
			System.err.println(this.getSmileRep());
			System.err.println(g.id);
		}
		//System.out.println(this.getSmileRep());
		//System.out.println(g.id);
		else
			for(int i=0;i<mol.getAtomsSize();i++)
			{
				Atom a=mol.getAtom(found[i]+1);
				g.V[i].changeLabel(a.getType());
			}

	}
}
