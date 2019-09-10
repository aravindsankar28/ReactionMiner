///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: RuleOf5Filter.java,v $
//  Purpose:  Filter to avoid molecules with a 'poor absorption or permeability'.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg Kurt Wegner
//  Version:  $Revision: 1.9 $
//            $Date: 2005/02/24 16:58:58 $
//            $Author: wegner $
//
// Copyright OELIB:          OpenEye Scientific Software, Santa Fe,
//                           U.S.A., 1999,2000,2001
// Copyright JOELIB/JOELib2: Dept. Computer Architecture, University of
//                           Tuebingen, Germany, 2001,2002,2003,2004,2005
// Copyright JOELIB/JOELib2: ALTANA PHARMA AG, Konstanz, Germany,
//                           2003,2004,2005
///////////////////////////////////////////////////////////////////////////////
package openBabel;

import java.io.IOException;

import joelib2.feature.Feature;
import joelib2.feature.FeatureException;
import joelib2.feature.FeatureFactory;
import joelib2.feature.FeatureResult;

import joelib2.feature.result.DoubleResult;
import joelib2.feature.result.IntResult;

import joelib2.feature.types.MolarRefractivity;
import joelib2.feature.types.MolecularWeight;
import joelib2.feature.types.PolarSurfaceArea;
import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;
import joelib2.io.MoleculeIOException;

import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Molecule;
import joelib2.process.filter.Filter;
import joelib2.process.filter.FilterInfo;

import org.apache.log4j.Category;


/**
 * Filter to avoid molecules with a 'poor absorption or permeability'.
 * This is only a 'soft' filter and can be used to get an idea about lead/drug-likeness.
 *
 * @.author     wegnerj
 * @.wikipedia Lipinski's Rule of Five
 * @.wikipedia ADME
 * @.wikipedia Drug
 * @.wikipedia QSAR
 * @.wikipedia Data mining
 * @.license    GPL
 * @.cvsversion    $Revision: 1.9 $, $Date: 2005/02/24 16:58:58 $
 * @.cite lldf01
 * @.cite odtl01
 * @see joelib2.feature.types.HBD1
 * @see joelib2.feature.types.HBD2
 * @see joelib2.feature.types.HBA1
 * @see joelib2.feature.types.HBD2
 * @see joelib2.feature.types.AtomInDonor
 * @see joelib2.feature.types.AtomInAcceptor
 * @see joelib2.feature.types.AtomInDonAcc
 */
public class Lipinski 
{
    //~ Static fields/initializers /////////////////////////////////////////////

    // Obtain a suitable logger.
    private static Category logger = Category.getInstance(
            "joelib2.process.filter.RuleOf5Filter");

    //~ Instance fields ////////////////////////////////////////////////////////

    private FilterInfo info;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     *  Constructor for the DescriptorFilter object
     */
    public Lipinski()
    {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException, MoleculeIOException
    {
		BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SDF");
		Molecule mol = new BasicConformerMolecule(inType, inType); 
		BasicReader reader=new BasicReader(args[0]);
		int i=0;
		int numMol=0;
		StringBuffer s=new StringBuffer("");
		while(reader.readNext(mol))
		{
			numMol++;
			s.append(numMol+" "+mol.getTitle()+" "+score(mol)+"\n");
			
		}
		
    }
    
    
    /**
     * Returns only <tt>true</tt> if less than two of the four rules fullfills the rules.
     * Otherwise a 'poor absorption or permeability' is possible and <tt>false</tt> will be returned.
     *
     * @param  mol  Description of the Parameter
     * @return      Description of the Return Value
     * @.cite lldf01
     */
    public static String score(Molecule mol)
    {
        //    if (false)
        //    {
        //      logger.warn("" + this.getClass().getName() + ".");
        //      return false;
        //    }
        // gets lipinski parameters
        double mw = MolecularWeight.getMolecularWeight(mol);
        double mr= new MolarRefractivity().getDoubleValue(mol);
        //double logP = getLogP(mol, "XlogP");
        double logP = getLogP(mol, "LogP");
        int hba = getNumberHBA(mol, "HBA1");
        int hbd = getNumberHBD(mol, "HBD1");
        int na=mol.getAtomsSize();
        int rb=mol.getRotorsSize();
        double psa=new PolarSurfaceArea().getDoubleValue(mol);
        // check lipinski parameters
         double score = 0;

        if(psa<=90)
        	score+=(2-psa/90);
        if (rb<=5)
        	score+=2-rb/5.0;
        if(mr>=40 && mr<130)
        	score++;
        if (na>=20 && na<=70)
        	score+=(2-(na-20)/50);
        if (mw <= 500)
        {
        	score+=(2-mw/500.0);
        }

        if (logP <= 5.6 && logP>=-0.4)
        {
        	score+=(2-(logP+0.4)/6);
            
        }

        if (hba <= 10)
        {
            score++;
            if (hba<8)
            	score++;
        }

        if (hbd <= 5)
        {
            score+=(2-hbd/5.0);
        }

        // decide lipinski criteria

        return new String(score+" "+hba+" "+hbd+" "+logP+" "+mw+" "+mr+" "+psa+" "+rb+" "+na);
    }

    /**
     *  Gets the processInfo attribute of the DescriptorFilter object
     *
     * @return    The processInfo value
     */
    public FilterInfo getFilterInfo()
    {
        return info;
    }

    /**
     *  Sets the filterInfo attribute of the DescriptorFilter object
     *
     * @param  _info  The new filterInfo value
     */
    public void setFilterInfo(FilterInfo _info)
    {
        info = _info;
    }

    private static double getLogP(Molecule mol, String name)
    {
        // call external logP calculation program

        /*      External ext =null;
              boolean success=false;
              Hashtable props = new Hashtable();
              try
              {
                ext= ExternalFactory.instance().getExternal(extName);
                success=ext.process(mol, props);
              }
              catch (ExternalException ex)
              {
                ex.printStackTrace();
                return Double.NaN;
              }
              catch (JOEProcessException ex)
              {
                ex.printStackTrace();
                return Double.NaN;
              }

              Double value=(Double)props.get("XLOGP");
              return value.doubleValue();
        */
        Feature logP = null;
        FeatureResult logPResult = null;

        try
        {
            logP = FeatureFactory.getFeature(name);

            if (logP == null)
            {
                logger.error("Descriptor " + logP + " can't be loaded.");

                return -1;
            }

            logP.clear();
            logPResult = logP.calculate(mol);

            // has something weird happen
            if (logPResult == null)
            {
                return -1;
            }
        }
        catch (FeatureException ex)
        {
            logger.error(ex.toString());

            return -1;
        }

        return ((DoubleResult) logPResult).value;
    }

    private static int getNumberHBA(Molecule mol, String hbaName)
    {
        Feature hba = null;
        FeatureResult hbaResult = null;

        try
        {
            hba = FeatureFactory.getFeature(hbaName);

            if (hba == null)
            {
                logger.error("Descriptor " + hba + " can't be loaded.");

                return -1;
            }

            hba.clear();
            hbaResult = hba.calculate(mol);

            // has something weird happen
            if (hbaResult == null)
            {
                return -1;
            }
        }
        catch (FeatureException ex)
        {
            logger.error(ex.toString());

            return -1;
        }

        return ((IntResult) hbaResult).getInt();
    }

    private static int getNumberHBD(Molecule mol, String hbdName)
    {
        Feature hbd = null;
        FeatureResult hbdResult = null;

        try
        {
            hbd = FeatureFactory.getFeature(hbdName);

            if (hbd == null)
            {
                logger.error("Descriptor " + hbd + " can't be loaded.");

                return -1;
            }

            hbd.clear();
            hbdResult = hbd.calculate(mol);

            // has something weird happen
            if (hbdResult == null)
            {
                return -1;
            }
        }
        catch (FeatureException ex)
        {
            logger.error(ex.toString());

            return -1;
        }

        return ((IntResult) hbdResult).getInt();
    }
}

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
