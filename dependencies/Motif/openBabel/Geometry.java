package openBabel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import joelib2.feature.types.atomlabel.AtomInAcceptor;
import joelib2.feature.types.atomlabel.AtomInDonor;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.MoleculeIOException;
import joelib2.math.BasicVector3D;
import joelib2.math.Vector3D;
import joelib2.molecule.Atom;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Bond;
import joelib2.molecule.Molecule;
import joelib2.ring.Ring;
import joelib2.util.iterator.AtomIterator;
import joelib2.util.iterator.BondIterator;
import joelib2.util.iterator.RingIterator;
import tool.FileUtils;

public class Geometry {

	public static void main(String[] args) throws IOException, MoleculeIOException {
		// TODO Auto-generated method stub
		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("ASN");
		Molecule mol = new BasicConformerMolecule(inType, inType);
		
		BasicReader reader=new BasicReader(args[0]);
		int i=0;
		int numMol=0;
		Vector failed=new Vector<Integer>();
		StringBuffer s=new StringBuffer("");
		boolean more=true;
		while(more)
		{
			try
			{
				if(reader.readNext(mol))
				{
					numMol++;
					int numAtoms=0;
					int numEdges=0;
					StringBuffer allAtoms=new StringBuffer(""); // for 3D coordinates
					StringBuffer atomsRep=new StringBuffer("");	// for 3D coordinates of only pharmacophoric atoms
					AtomIterator at=mol.atomIterator();
					RingIterator rt=mol.getRingIterator();
					while (rt.hasNext())
					{
						Ring r=rt.nextRing();
						if (r.isAromatic())
						{
							BasicVector3D center=new BasicVector3D();
							r.findCenterAndNormal(center, new BasicVector3D(),
							        new BasicVector3D());
							
							atomsRep.append("ARO "+center.x3D+","+center.y3D+","+center.z3D+" "+r.toString()+"\n");
						}
					}
					boolean[] donors=(boolean[])new AtomInDonor().getAtomPropertiesArray(mol);
					boolean[] acceptors=(boolean[])new AtomInAcceptor().getAtomPropertiesArray(mol);
					int j=0;
					while(at.hasNext())
					{
						Atom a=at.nextAtom();
						allAtoms.append(a.get3Dx()+","+a.get3Dy()+","+a.get3Dz()+"\n");
						if (acceptors[j])
							atomsRep.append("HBA "+a.get3Dx()+","+a.get3Dy()+","+a.get3Dz()+" "+a.toString()+"\n");

						if (donors[j])
							atomsRep.append("HBD "+a.get3Dx()+","+a.get3Dy()+","+a.get3Dz()+" "+a.toString()+"\n");
						j++;
					}

					System.out.println("#"+mol.getTitle()+"\n"+allAtoms.toString()+"\n");
				}
				else
					
					more=false;
			}
			catch (Exception e) {
				failed.add(numMol);
			}
		}

		FileUtils.writeToFile(args[0].replace(".sdf", ".txt"), s.toString());
		if (failed.size()>0)
			System.out.println("Failed to decode: "+failed.size()+" : "+failed.toString());
	}

	
	
}
